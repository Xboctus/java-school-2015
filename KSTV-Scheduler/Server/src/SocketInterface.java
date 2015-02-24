import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketInterface {
	private static ServerSocket serverSocket;
	private static AtomicBoolean connectionsAcceptable;
	@Deprecated
	public static HashMap<String /*name*/, Socket> clientSockets; // TODO: make private
	private static HashMap<Socket, String /*name*/> namePerSocket;
	private static ConnectionDispatcher connectionHandler;

	private static class ConnectionHandler extends Thread {
		private Socket socket;
		private InputStream inputStream;
		private Scanner scanner;
		private OutputStream outputStream;
		private PrintWriter printWriter;

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

		@Override
		public void run() {
			boolean running = true;
			while (running) {
				String[] parts = scanner.next().split("\u001F"); // FIXME: empty last arguments are discarded
				if (parts.length == 0) {
					printWriter.print("unknown_command");
					printWriter.flush();
					continue;
				}

				String response = null; // TODO: remove null after implementing all branches

				switch (parts[0]) {
				case "login":
					if (parts.length != 3) {
						response = "invalid_args_count";
					} else {
						// TODO
					}
					break;
				case "logout":
					if (parts.length != 1) {
						response = "invalid_args_count";
					} else {
						namePerSocket.put(socket, null);
						response = "logout_ack";
					}
					break;
				case "disconnect":
					if (parts.length != 1) {
						response = "invalid_args_count";
					} else {
						running = false;
						// TODO
					}
					break;
				case "prolongate":
					if (parts.length != 1) {
						response = "invalid_args_count";
					} else {
						// TODO
					}
					break;
				case "invalidate":
					if (parts.length != 1) {
						response = "invalid_args_count";
					} else {
						String name = namePerSocket.get(socket);
						if (name != null) {
							for (Map.Entry<Socket, String /*name*/> e: namePerSocket.entrySet()) {
								if (e.getValue().equals(name)) {
									;
								}
							}
						}
						// TODO
					}
					break;
				default:
					response = "unknown_command";
				}
				printWriter.print(response);
				printWriter.print("\u001E");
				printWriter.flush();
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
			while (connectionsAcceptable.get()) {
				try {
					new ConnectionHandler(serverSocket.accept()).start();
				} catch (IOException e) {
					continue;
				}
			}
		}
	}

	public static void openServerSocket() throws IOException {
		serverSocket = new ServerSocket(0);
		connectionsAcceptable = new AtomicBoolean(true);
		clientSockets = new HashMap<>();
		namePerSocket = new HashMap<>();
		connectionHandler = new ConnectionDispatcher();
		connectionHandler.start();
	}

	public static void closeServerSocket() throws IOException, InterruptedException {
		connectionsAcceptable.set(false);
		try {
			serverSocket.close();
		} finally {
			connectionHandler.join(); // FIXME: if throws, previously thrown exception is lost
		}
	}

	public static int getEventPort() {
		return serverSocket.getLocalPort();
	}
}
