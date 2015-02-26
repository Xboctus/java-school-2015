import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public final class ServerHandler {
	public enum HandlingError {
		NO_ERROR,
		NO_SUCH_USER,
		USER_EXISTS,
		INTERNAL_ERROR,
	}

	private static Timer timer;

	public static void startTimer() {
		timer = new Timer();
	}

	public static void stopTimer() {
		timer.cancel();
	}
/*
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
			Socket socket = SocketInterface.clientSockets.get(sessionId);
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
			pw.print("result=" + (notEmpty ? "yes" : "no") + "\n");
			pw.flush();
		}
	}

	public static TestResult test(String login, String sessionId) {
		assert SyntaxChecker.checkLogin(login);

		try {
			DbConnector.ActionStatement.TEST.statement.setString(1, login);
		} catch (SQLException e) {
			return new TestResult(HandlingError.INTERNAL_ERROR, false);
		}

		boolean notEmpty;
		try (ResultSet rs = DbConnector.ActionStatement.TEST.statement.executeQuery()) {
			notEmpty = rs.next();
		} catch (SQLException e) {
			return new TestResult(HandlingError.INTERNAL_ERROR, false);
		}

		timer.schedule(new TestTask(sessionId, notEmpty), 10*1000);

		HandlingError error = notEmpty ? HandlingError.NO_ERROR : HandlingError.NO_SUCH_USER;
		TestResult res = new TestResult(error, notEmpty);
		res.serverPort = SocketInterface.getEventPort();
		return res;
	}
*/
	public static class CreateUserResult {
		public HandlingError error;
		public int serverPort;

		public CreateUserResult(HandlingError error) {
			this.error = error;
		}
	}

	public static CreateUserResult createUser(String login, String password, TimeZone timeZone, boolean active) {
		try {
			DbConnector.ActionStatement.TEST.statement.setString(1, login);
		} catch (SQLException e) {
			return new CreateUserResult(HandlingError.INTERNAL_ERROR);
		}

		boolean notEmpty;
		try (ResultSet rs = DbConnector.ActionStatement.TEST.statement.executeQuery()) {
			notEmpty = rs.next();
		} catch (SQLException e) {
			return new CreateUserResult(HandlingError.INTERNAL_ERROR);
		}

		if (notEmpty) {
			return new CreateUserResult(HandlingError.USER_EXISTS);
		}

		try {
			DbConnector.ActionStatement.CREATE_USER.statement.setString(1, login);
			DbConnector.ActionStatement.CREATE_USER.statement.setString(2, password);
			DbConnector.ActionStatement.CREATE_USER.statement.setString(3, timeZone.getDisplayName());
			DbConnector.ActionStatement.CREATE_USER.statement.setBoolean(4, active);
		} catch (SQLException e) {
			return new CreateUserResult(HandlingError.INTERNAL_ERROR);
		}

		CreateUserResult res = new CreateUserResult(HandlingError.NO_ERROR);
		res.serverPort = SocketInterface.getEventPort();
		return res;
	}
}
