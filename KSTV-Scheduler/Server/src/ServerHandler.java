import java.io.*;
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

	public static class TestResult {
		public Error error;
		public boolean exists;

		public TestResult(Error error, boolean exists) {
			this.error = error;
			this.exists = exists;
		}
	}

	public static class UserInfoResult {
		public TimeZone timeZone;
		public boolean active;
		public HashMap<java.util.Date, String> events;
		public Error error;
	}

	public static UserInfoResult userInfo(String loginFrom) {
		return new UserInfoResult();
	}

	private static Connection con;
	private static PreparedStatement testStmnt;

	// post: throws or
	//       doesn't throw and !con.isClosed() and !testStmnt.isClosed()
	public static void establishConnection(ServletContext sc) throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get(sc.getRealPath("/WEB-INF/connection.txt")), StandardCharsets.UTF_8);
		String conStr;
		try {
			conStr = br.readLine();
		} finally {
			br.close();
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

	// pre: con != null and !con.isClosed()
	// pre: login is valid
	// pre: listenPort is valid
	// TODO: add event
	public static TestResult test(String login, int listenPort) {
		ResultSet rs;
		try {
			assert con != null && !con.isClosed();
			testStmnt.setString(1, login);
			rs = testStmnt.executeQuery();
		} catch (SQLException e) {
			return new TestResult(Error.INTERNAL_ERROR, false);
		}
		boolean notEmpty;
		try {
			notEmpty = rs.next();
		} catch (SQLException e1) {
			try {
				rs.close();
			} catch (SQLException e) {
				;
			}
			return new TestResult(Error.INTERNAL_ERROR, false);
		}
/*		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// ??
			}
		}, 15*1000);*/
		if (notEmpty) {
			return new TestResult(Error.NO_ERROR, notEmpty);
		} else {
			return new TestResult(Error.NO_SUCH_USER, notEmpty);
		}

	}
}
