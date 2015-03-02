import java.util.*;
import java.sql.*;

public final class ServerHandler {
	public enum HandlingError {
		NO_ERROR,
		UNAUTHORIZED,
	}

	private static Timer timer;

	public static void startTimer() {
		timer = new Timer();
	}

	public static void stopTimer() {
		timer.cancel();
	}

	public static HandlingError login(String name, String password) throws SQLException {
		try (
			PreparedStatement st = DbConnector.createStatement(DbConnector.ActionStatement.AUTHENTICATE);
		) {
			st.setString(1, name);
			st.setString(2, password);
			try (ResultSet rs = st.executeQuery()) {
				return rs.next() ? HandlingError.NO_ERROR : HandlingError.UNAUTHORIZED;
			}
		}
	}
}
