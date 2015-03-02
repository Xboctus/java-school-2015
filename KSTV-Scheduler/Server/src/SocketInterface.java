import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;
import java.net.*;
import java.sql.*;

public final class SocketInterface {
	private static ServerSocket serverSocket;
	private static AtomicBoolean consAcceptable;
	private static ArrayList<ConnectionHandler> conHandlers;
	private static ConnectionDispatcher conDispatcher;
	private static boolean initialized = false;

	private static class ConnectionHandler extends Thread {
		private Socket socket;
		private InputStream inputStream;
		private Scanner scanner;
		private OutputStream outputStream;
		public PrintWriter printWriter;
		public String name = null;
		public AtomicBoolean timeout = new AtomicBoolean(false);
		boolean running = true;

		private ConnectionHandler(Socket socket) {
			super("Connection handler for " + socket.toString());
			this.socket = socket;
			try {
				this.inputStream = this.socket.getInputStream();
			} catch (IOException e) {
				; // TODO
			}
			this.scanner = new Scanner(this.inputStream);
			this.scanner.useDelimiter("\u001E");
			try {
				this.outputStream = this.socket.getOutputStream();
			} catch (IOException e) {
				; // TODO
			}
			this.printWriter = new PrintWriter(this.outputStream);
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
					statement.setString(1, parts[0]);
					statement.setString(2, parts[1]);
					try (ResultSet rs = statement.executeQuery()) {
						result = rs.next() ? "login_valid" : "login_invalid";
					} catch (SQLException e) {
						Server.servletContext.log("Result set related exception occured", e);
					}
				} catch (SQLException e) {
					Server.servletContext.log("Statement related exception occured", e);
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
				running = false;
				// TODO
				return new String[] {"disconnect_ack"};
			}
		}

		private class ProlongateHandler extends CommandHandler {
			{ verb = "prolongate"; argCount = 0; }

			@Override
			public String[] handle(String[] parts) {
				// TODO
				return new String[] {"prolongate_ack"};
			}
		}

		private class InvalidateHandler extends CommandHandler {
			{ verb = "invalidate"; argCount = 0; }

			@Override
			public String[] handle(String[] parts) {
				if (name != null) {
					for (ConnectionHandler ch: conHandlers) {
						if (name.equals(ch.name)) {
							; // TODO
						}
					}
				}
				// TODO
				return new String[] {"invalidate_ack"};
			}
		}

		private final CommandHandler[] commandHandlers = {
			new LoginHandler(),
			new LogoutHandler(),
			new DisconnectHandler(),
			new ProlongateHandler(),
			new InvalidateHandler(),
		};

		private static final String[] respPartsUnknownCommand = new String[] {"unknown_command"};
		private static final String[] respPartsInvalidArgsCount = new String[] {"invalid_args_count"};

		@Override
		public void run() {
			while (running) {
				// FIXME: if first character is message terminator, it is skipped
				String[] parts = scanner.next().split("\u001F", -1);

				timeout.set(false);

				String[] respParts = null;

				if (parts.length == 0) {
					respParts = respPartsUnknownCommand;
				} else {
					boolean served = false;
					for (CommandHandler ch: commandHandlers) {
						if (!parts[0].equals(ch.verb)) {
							continue;
						}
						if (parts.length - 1 != ch.argCount) {
							respParts = respPartsInvalidArgsCount;
							break;
						}
						respParts = ch.handle(parts);
						served = true;
						break;
					}
					if (!served) {
						respParts = respPartsUnknownCommand;
					}
				}

				try {
					Shared.putParts(respParts, printWriter);
				} catch (IOException e) {
					running = false;
				}
			}
			scanner.close(); // -> inputStream.close() -> causes socket.close()
		}
	}

	private static class ConnectionDispatcher extends Thread {
		public ConnectionDispatcher() {
			super("Connection dispatcher");
		}

		@Override
		public void run() {
			while (consAcceptable.get()) {
				try {
					ConnectionHandler ch = new ConnectionHandler(serverSocket.accept());
					conHandlers.add(ch);
					ch.start();
				} catch (IOException e) {
					continue;
				}
			}
/*			for (ConnectionHandler ch: conHandlers) {
				;
			}
*/		}
	}

	private static final int CLIENT_TIMEOUT = 30; // in mins

	private static Timer timeoutCleaner;

	private static class TimeoutCleanupTask extends TimerTask {
		@Override
		public void run() {
			for (ConnectionHandler ch: conHandlers) {
				if (ch.timeout.get()) {
					; // TODO
				} else {
					ch.timeout.set(true);
				}
			}
		}
	}

	public static void init() throws IOException {
		serverSocket = new ServerSocket(0);
		consAcceptable = new AtomicBoolean(true);
		conHandlers = new ArrayList<>();
		conDispatcher = new ConnectionDispatcher();
		conDispatcher.start();
		timeoutCleaner = new Timer(); // TODO: don't start timer until first connection
		timeoutCleaner.schedule(new TimeoutCleanupTask(), 0, (CLIENT_TIMEOUT + 5)*60*1000);
		initialized = true;
	}

	public static void destroy() throws IOException, InterruptedException {
		if (!initialized) {
			return;
		}
		timeoutCleaner.cancel();
		consAcceptable.set(false);
		IOException exc = null;
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
			throw e;
		}
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
