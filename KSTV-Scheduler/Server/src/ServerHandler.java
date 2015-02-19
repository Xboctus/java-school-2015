import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletContext;

public class ServerHandler {
	public enum Error {
		NO_ERROR,
		USER_ALREADY_EXISTS,
		NO_SUCH_USER,
		INVALID_DATE_FORMAT,
		EVENT_ALREADY_EXISTS,
		NO_SUCH_EVENT,
		UNKNOWN_COMMAND,
		INTERNAL_ERROR,
	}

/*	public static class UserInfoResult {
		public TimeZone timeZone;
		public boolean active;
		public HashMap<java.util.Date, String> events;
		public Error error;
	}

	public static UserInfoResult userInfo(String loginFrom) {
		return new UserInfoResult();
	}*/

	private static Connection con;
	private static PreparedStatement testStmnt;

	// post: throws or
	//       doesn't throw and !con.isClosed() and !testStmnt.isClosed()
	public static void establishConnection(ServletContext sc) throws Exception {
		Path conFile = Paths.get(sc.getRealPath("/WEB-INF/connection.txt"));
		String conStr;
		try (BufferedReader br = Files.newBufferedReader(conFile, StandardCharsets.UTF_8)) {
			conStr = br.readLine();
		} catch (IOException e) {
			throw e;
		}

		con = DriverManager.getConnection("jdbc:" + conStr);
		assert con != null && !con.isClosed();

		try {
			testStmnt = con.prepareStatement("SELECT * FROM Users WHERE name = ?");
			assert testStmnt != null && !testStmnt.isClosed();
		} catch (SQLException e) {
			con.close();
			throw e;
		}
	}

	// pre: con != null and !con.isClosed()
	public static void closeConnection() {
		try {
			assert con != null && !con.isClosed();
			con.close();
		} catch (SQLException e) {
			;
		}
	}

	private static Timer timer;

	public static void startTimer() {
		timer = new Timer();
	}

	public static void stopTimer() {
		timer.cancel();
	}

	private static ServerSocket serverSocket;

	public static void openSocket() throws Exception {
		serverSocket = new ServerSocket(0);
	}

	public static void closeSocket() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			;
		}
	}

	public static class TestResult {
		public Error error;
		public boolean exists;
		public int serverSocket;

		public TestResult(Error error, boolean exists) {
			this.error = error;
			this.exists = exists;
		}
	}

	// pre: con != null and !con.isClosed()
	// pre: login is valid
	// pre: listenPort is valid
	// TODO: add event
	public static TestResult test(String login, int listenPort) {
		try {
			assert con != null && !con.isClosed();
			testStmnt.setString(1, login);
		} catch (SQLException e) {
			return new TestResult(Error.INTERNAL_ERROR, false);
		}

		boolean notEmpty;
		try (ResultSet rs = testStmnt.executeQuery()) {
			notEmpty = rs.next();
		} catch (SQLException e) {
			return new TestResult(Error.INTERNAL_ERROR, false);
		}

/*		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// ??
			}
		}, 15*1000);*/

		Error error = notEmpty ? Error.NO_ERROR : Error.NO_SUCH_USER;
		TestResult res = new TestResult(error, notEmpty);;
		res.serverSocket = serverSocket.getLocalPort();
		return res;
	}
}
