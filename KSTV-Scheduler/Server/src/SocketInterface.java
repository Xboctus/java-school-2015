import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.sql.*;

// TODO: use NIO for scheme "multiple connections - single handler"
public final class SocketInterface {
	private static ServerSocket serverSocket = null;
	private static Vector<ConnectionHandler> conHandlers; // TODO: use thread pool
	private static ConnectionDispatcher conDispatcher;
	private static boolean initialized = false;

	private static class ConnectionHandler extends Thread {
		enum InterruptCause {
			SHUTDOWN,
			ASYNC_FAIL,
			DISCONNECT,
		}

		private Socket socket;
		private BufferedReader reader;
		public BufferedWriter writer;
		public volatile String name = null;
		public volatile InterruptCause interruptCause = InterruptCause.SHUTDOWN;

		public static volatile boolean allowSelfRemoval = true;

		private ConnectionHandler(Socket socket, InputStream ins, OutputStream outs) {
			super("Connection handler for " + socket.getRemoteSocketAddress().toString());
			this.socket = socket;
			this.reader = new BufferedReader(new InputStreamReader(ins));
			this.writer = new BufferedWriter(new OutputStreamWriter(outs));
		}

		private abstract class CommandHandler {
			String verb;
			int argCount;
			abstract String[] handle(String[] parts);
		}

		private class LoginHandler extends CommandHandler {
			{ verb = "login"; argCount = 2; }

			@Override
			public String[] handle(String[] parts) {
				String result = "internal_error";
				try (
					PreparedStatement statement = DbConnector.createStatement(DbConnector.ActionStatement.AUTHENTICATE);
				) {
					statement.setString(1, parts[1]);
					statement.setString(2, parts[2]);
					try (ResultSet rs = statement.executeQuery()) {
						result = rs.next() ? "login_valid" : "login_invalid";
					} catch (SQLException e) {
						Server.log_exc("Result set related exception occured", e);
					}
				} catch (SQLException e) {
					Server.log_exc("Statement related exception occured", e);
				}
				if (result.equals("login_valid")) {
					name = parts[1];
				}
				return new String[] {result};
			}
		}

		private class LogoutHandler extends CommandHandler {
			{ verb = "logout"; argCount = 0; }

			@Override
			public String[] handle(String[] parts) {
				name = null;
				return new String[] {"logout_ack"};
			}
		}

		private class DisconnectHandler extends CommandHandler {
			{ verb = "disconnect"; argCount = 0; }

			@Override
			public String[] handle(String[] parts) {
				interruptCause = InterruptCause.DISCONNECT;
				Thread.currentThread().interrupt();
				return new String[] {"disconnect_ack"};
			}
		}

		private class ProlongateHandler extends CommandHandler {
			{ verb = "prolongate"; argCount = 0; }

			@Override
			public String[] handle(String[] parts) {
				// no-op - receiving this command is enough to reset timeout
				return new String[] {"prolongate_ack"};
			}
		}

		private class InvalidateHandler extends CommandHandler {
			{ verb = "invalidate"; argCount = 0; }

			private final ConnectionHandler parent;

			public InvalidateHandler(ConnectionHandler parent) {
				this.parent = parent;
			}

			@Override
			public String[] handle(String[] parts) {
				if (name == null) {
					return new String[] {"invalidate_ack"};
				}
				synchronized (conHandlers) {
					for (ConnectionHandler ch: conHandlers) {
						String curName = ch.name;
						if (ch == parent || curName == null || !curName.equals(name)) {
							continue;
						}
						ch.name = null;
						try {
							synchronized (ch.writer) {
								Shared.putParts(new String[] {"invalidated"}, ch.writer);
							}
						} catch (IOException e) {
							Server.log_exc("Sending response failed", e);
							ch.interruptCause = InterruptCause.ASYNC_FAIL;
							ch.interrupt();
						}
					}
				}
				return new String[] {"invalidate_ack"};
			}
		}

		private final CommandHandler[] commandHandlers = {
			new LoginHandler(),
			new LogoutHandler(),
			new DisconnectHandler(),
			new ProlongateHandler(),
			new InvalidateHandler(this),
		};

		private static final String[] respPartsUnknownCommand = new String[] {"unknown_command"};
		private static final String[] respPartsInvalidArgsCount = new String[] {"invalid_args_count"};

		@Override
		public void run() {
			try {
				synchronized (writer) {
					Shared.putParts(new String[] {"connect_ack"}, writer);
				}
			} catch (IOException e) {
				Server.log_exc("Sending connect_ack failed", e);
				Thread.currentThread().interrupt();
			}

			boolean interrupted;
			while (!(interrupted = Thread.interrupted())) {
				Shared.GetPartsResult r = Shared.getParts2(reader);

				if (r.ioe instanceof SocketTimeoutException) {
					try {
						synchronized (writer) {
							Shared.putParts(new String[] {"timeout"}, writer);
						}
					} catch (IOException e) {
						Server.log_exc("Sending error failed", e);
					}
					break;
				}
				if (r.ioe instanceof ClosedByInterruptException) {
					interrupted = true;
					break;
				}
				if (r.ioe != null || r.endOfStream) {
					try {
						synchronized (writer) {
							Shared.putParts(new String[] {"io_error"}, writer);
						}
					} catch (IOException e) {
						Server.log_exc("Sending error failed", e);
					}
					break;
				}

				String[] respParts = null;

				if (r.parts.length == 0) {
					respParts = respPartsUnknownCommand;
				} else {
					for (CommandHandler ch: commandHandlers) {
						if (r.parts[0].equals(ch.verb)) {
							if (r.parts.length - 1 != ch.argCount) {
								respParts = respPartsInvalidArgsCount;
							} else {
								respParts = ch.handle(r.parts);
							}
							break;
						}
					}
					if (respParts == null) {
						respParts = respPartsUnknownCommand;
					}
				}

				try {
					synchronized (writer) {
						Shared.putParts(respParts, writer);
					}
				} catch (IOException e) {
					Server.log_exc("Writing reponse parts failed", e);
				}
			}

			if (interrupted) {
				try {
					synchronized (writer) {
						switch (interruptCause) {
						case SHUTDOWN:
							Shared.putParts(new String[] {"shutdown"}, writer);
							break;
						case ASYNC_FAIL:
							Shared.putParts(new String[] {"io_error"}, writer);
							break;
						case DISCONNECT:
							// no-op - "disconnect_ack" was already responded
							break;
						}
					}
				} catch (IOException e) {
					Server.log_exc("Writing reponse parts failed", e);
				}
			}

			try {
				socket.close();
			} catch (IOException e) {
				Server.log_exc("socket.close() failed", e);
			}

			if (allowSelfRemoval) {
				synchronized (conHandlers) {
					conHandlers.remove(this); // uses Object.equals() for comparison
				}
			}
		}
	}

	private static final int CLIENT_TIMEOUT = 30; // in mins

	private static class ConnectionDispatcher extends Thread {
		public ConnectionDispatcher() {
			super("Connection dispatcher");
		}

		@Override
		public void run() {
			conHandlers = new Vector<>();

			while (!Thread.interrupted()) {
				Socket socket;
				try {
					socket = serverSocket.accept(); // socket is closed by ConnectionHandler.run()
				} catch (ClosedByInterruptException e) { // from conDispatcher.interrupt(), seems to not work
					break;
				} catch (SocketException e) { // from serverSocket.close()
					break;
				} catch (IOException e) { // from serverSocket.accept()
					Server.log_exc("serverSocket.accept() failed", e);
					continue;
				}

				OutputStream outs = null;
				InputStream ins;

				try {
					// streams are automatically closed by socket.close() in ConnectionHandler.run()
					outs = socket.getOutputStream();
					ins = socket.getInputStream();
					socket.setSoTimeout((CLIENT_TIMEOUT + 5)*60*1000);
				} catch (IOException e) {
					Server.log_exc("Socket initialization failed", e);
					if (outs != null) {
						try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(outs))) {
							Shared.putParts(new String[] {"io_error"}, pw);
						} catch (IOException e2) {
							Server.log_exc("Socket output stream related exception occured", e2);
						}
					}
					try {
						socket.close();
					} catch (IOException e2) {
						Server.log_exc("Closing socket failed", e);
					}
					continue;
				}

				ConnectionHandler ch = new ConnectionHandler(socket, ins, outs);
				conHandlers.add(ch);
				ch.start();
			}

			ConnectionHandler.allowSelfRemoval = false;

			for (ConnectionHandler ch: conHandlers) {
				ch.interrupt();
				try {
					ch.join();
				} catch (InterruptedException e) {
					Server.log_exc("ch.join() was interrupted", e);
				}
			}

			conHandlers = null;
		}
	}

	public static void init() throws IOException {
		serverSocket = new ServerSocket(0);
		conDispatcher = new ConnectionDispatcher();
		conDispatcher.start();
		initialized = true;
	}

	public static void destroy() throws Exception {
		if (!initialized) {
			return;
		}

		Exception exc = null;

		conDispatcher.interrupt();
		try {
			serverSocket.close();
		} catch (IOException e) {
			exc = e;
		}
		try {
			conDispatcher.join();
		} catch (InterruptedException e) {
			if (exc != null) {
				e.addSuppressed(exc);
			}
			exc = e;
		}

		conDispatcher = null;
		serverSocket = null;

		if (exc != null) {
			throw exc;
		}
	}

	public static int getEventPort() {
		return serverSocket != null ? serverSocket.getLocalPort() : 0;
	}

/*	public static void sendEvent(String name, String[] parts) {
		;
	}
*/}
