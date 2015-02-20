import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;

public class ServerHandler {
	public enum HandlingError {
		NO_ERROR,
		NO_SUCH_USER,
		INTERNAL_ERROR,
	}

	private static Connection dbCon;
	private static PreparedStatement testStmnt;

	public static void establishDbConnection(ServletContext sc) throws Exception {
		Path conFile = Paths.get(sc.getRealPath("/WEB-INF/connection.txt"));
		String driverStr;
		String conStr;
		try (BufferedReader br = Files.newBufferedReader(conFile, StandardCharsets.UTF_8)) {
			driverStr = br.readLine();
			conStr = br.readLine();
		} catch (IOException e) {
			throw e;
		}

		Class.forName(driverStr);
		dbCon = DriverManager.getConnection("jdbc:" + conStr);

		try {
			testStmnt = dbCon.prepareStatement("SELECT * FROM Users WHERE name = ?");
		} catch (SQLException e) {
			dbCon.close(); // FIXME: if throws, e is lost
			throw e;
		}
	}

	public static void closeDbConnection() throws SQLException {
		assert dbCon != null && testStmnt != null;
		try {
			testStmnt.close();
		} finally {
			dbCon.close(); // FIXME: if throws, previously thrown exception is lost
		}
	}

	private static class ConnectionHandler extends Thread {
		public ConnectionHandler() {
			super("Connection handler");
		}

		@Override
		public void run() {
			while (connectionsAcceptable.get()) {
				Socket socket;
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					continue;
				}
				String sessionId;
				try (Scanner sc = new Scanner(socket.getInputStream())) {
					sessionId = sc.next();
				} catch (IOException e) {
					try {
						socket.close();
					} catch (IOException e2) {
						continue;
					}
					continue;
				}
				clientSockets.put(sessionId, socket);
			}
		}
	}

	private static ServerSocket serverSocket;
	private static AtomicBoolean connectionsAcceptable;
	private static ConnectionHandler connectionHandler;
	private static HashMap<String /*sessionId*/, Socket> clientSockets = new HashMap<>();

	public static void openServerSocket() throws IOException {
		serverSocket = new ServerSocket(0);
		connectionsAcceptable = new AtomicBoolean(true);
		connectionHandler = new ConnectionHandler();
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

	private static Timer timer;

	public static void startTimer() {
		timer = new Timer();
	}

	public static void stopTimer() {
		timer.cancel();
	}

	public static class TestResult {
		public HandlingError error;
		public boolean exists;
		public int serverPort;

		public TestResult(HandlingError error, boolean exists) {
			this.error = error;
			this.exists = exists;
		}
	}

	private static class TestTask extends TimerTask {
		String sessionId;
		boolean notEmpty;

		public TestTask(String sessionId, boolean notEmpty) {
			this.sessionId = sessionId;
			this.notEmpty = notEmpty;
		}

		@Override
		public void run() {
			Socket socket = clientSockets.get(sessionId);
			if (socket == null) {
				return;
			}
			OutputStream outs;
			try {
				outs = socket.getOutputStream();
			} catch (IOException e) {
				return;
			}
			PrintWriter pw = new PrintWriter(outs);
			pw.print("result=" + (notEmpty ? "yes" : "no"));
			pw.flush();
		}
	}

	public static TestResult test(String login, String sessionId) {
		assert SyntaxChecker.checkLogin(login);
		assert dbCon != null;

		try {
			testStmnt.setString(1, login);
		} catch (SQLException e) {
			return new TestResult(HandlingError.INTERNAL_ERROR, false);
		}

		boolean notEmpty;
		try (ResultSet rs = testStmnt.executeQuery()) {
			notEmpty = rs.next();
		} catch (SQLException e) {
			return new TestResult(HandlingError.INTERNAL_ERROR, false);
		}

		timer.schedule(new TestTask(sessionId, notEmpty), 10*1000);

		HandlingError error = notEmpty ? HandlingError.NO_ERROR : HandlingError.NO_SUCH_USER;
		TestResult res = new TestResult(error, notEmpty);
		res.serverPort = serverSocket.getLocalPort();
		return res;
	}
}
