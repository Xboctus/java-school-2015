import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerHandler {
	private enum ActionStatement {
		OWN_INFO		("SELECT timezone, active FROM Users WHERE Users.name = ?"),
//		GET_USED_ID		("SELECT userID FROM Users WHERE name = ?"),
		EVENTS			("SELECT 'X' AS dtime, '' AS msg FROM Users WHERE name = ?" +
						" UNION" +
						" SELECT dtime, msg FROM Evnts WHERE userID IN" +
						"(SELECT userID FROM Users WHERE name = ?)"),
		AUTHENTICATE	("SELECT 1 FROM Users WHERE name = ? AND pass = ?"),
		CHANGE_PASSWORD	("UPDATE Users SET pass = ? WHERE name = ? AND pass = ?"),
		CHANGE_TIMEZONE	("UPDATE Users SET timezone = ? WHERE name = ?"),
		CHANGE_ACTIVE	("UPDATE Users SET active = ? WHERE name = ?");

		final String str;
		ActionStatement(String str) { this.str = str; }
	}

	public enum HandlingError {
		NO_ERROR,
		UNAUTHORIZED,
		NO_SUCH_USER,
	}

	public static class OwnInfo {
		public TimeZone timeZone;
		public boolean active;
		public int eventCount;
		public HandlingError error;
	}

	/**
	 * <p> result.error:
	 * <p> - NO_ERROR
	 * <p> - NO_SUCH_USER
	 */
	public static OwnInfo getOwnInfo(String name) throws SQLException {
		OwnInfo result = new OwnInfo();
		try (
			PreparedStatement st = DbConnector.createStatement(ActionStatement.OWN_INFO.str);
		) {
			st.setString(1, name);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					result.timeZone = TimeZone.getTimeZone(rs.getString("timezone"));
					result.active = rs.getBoolean("active");
					result.eventCount = 0;
					result.error = HandlingError.NO_ERROR;
				} else {
					result.error = HandlingError.NO_SUCH_USER;
				}
			}
		}
		return result;
	}

	public static class LocalDateTime {
		public int year;
		public int month;
		public int day;
		public int hour;
		public int minute;
		public int second;

		private static final Pattern DATE_TIME_PATTERN =
			Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})");

		public LocalDateTime(String s) throws ParseException {
			Matcher m = DATE_TIME_PATTERN.matcher(s);
			if (!m.matches()) {
				throw new ParseException("Invalid date-time format", -1);
			}
			this.year	= Integer.parseInt(m.group(1));
			this.month	= Integer.parseInt(m.group(2));
			this.day	= Integer.parseInt(m.group(3));
			this.hour	= Integer.parseInt(m.group(4));
			this.minute	= Integer.parseInt(m.group(5));
			this.second	= Integer.parseInt(m.group(6));
		}

		@Override
		public String toString() {
			return String.format("%d-%d-%d %d:%d:%d", year, month, day, hour, minute, second);
		}
	}

	public static class Event {
		public LocalDateTime dateTime;
		public String text;
	}

	public static class OwnEvents {
		Event[] events;
		HandlingError error;
	}

	// FIXME: wrap in transaction
	/**
	 * <p> result.error:
	 * <p> - NO_ERROR
	 * <p> - NO_SUCH_USER
	 */
	public static OwnEvents getOwnEvents(String name) throws SQLException, ParseException {
		OwnEvents result = new OwnEvents();
/*		int userId;
		try (
			PreparedStatement st = DbConnector.createStatement(ActionStatement.GET_USED_ID);
		) {
			st.setString(1, name);
			try (ResultSet rs = st.executeQuery()) {
				if (!rs.next()) {
					result.error = HandlingError.NO_SUCH_USER;
					return result;
				}
				userId = rs.getInt("userID");
			}
		}
*/		ArrayList<Event> events = new ArrayList<>();
		boolean userExists = false;
		try (
			PreparedStatement st = DbConnector.createStatement(ActionStatement.EVENTS.str);
		) {
			st.setString(1, name);
			st.setString(2, name);
			try (ResultSet rs = st.executeQuery()) {
				while (rs.next()) {
					String dateTimeStr = rs.getString("dtime");
					if (dateTimeStr.equals("X")) {
						userExists = true;
						continue;
					}
					Event event = new Event();
					event.dateTime = new LocalDateTime(dateTimeStr);
					event.text = rs.getString("msg");
					events.add(event);
				}
			}
		}
		if (!userExists) {
			result.error = HandlingError.NO_SUCH_USER;
		} else {
			result.events = events.toArray(new Event[events.size()]);
			result.error = HandlingError.NO_ERROR;
		}
		return result;
	}

	/**
	 * <p> result.error:
	 * <p> - NO_ERROR
	 * <p> - UNAUTHORIZED
	 */
	public static HandlingError login(String name, String password) throws SQLException {
		try (
			PreparedStatement st = DbConnector.createStatement(ActionStatement.AUTHENTICATE.str);
		) {
			st.setString(1, name);
			st.setString(2, password);
			try (ResultSet rs = st.executeQuery()) {
				return rs.next() ? HandlingError.NO_ERROR : HandlingError.UNAUTHORIZED;
			}
		}
	}

	// FIXME: wrap in transaction
	/**
	 * <p> result.error:
	 * <p> - NO_ERROR
	 * <p> - UNAUTHORIZED
	 * <p> - NO_SUCH_USER
	 */
	public static HandlingError changeOwnInfo(
		String name, String oldPassword, String newPassword, TimeZone newTimeZone, Boolean newActive
	) throws SQLException {
		if (newPassword != null) {
			try (
				PreparedStatement st = DbConnector.createStatement(ActionStatement.CHANGE_PASSWORD.str);
			) {
				st.setString(1, newPassword);
				st.setString(2, name);
				st.setString(3, oldPassword);
				if (st.executeUpdate() == 0) {
					return HandlingError.UNAUTHORIZED;
				}
			}
		}
		if (newTimeZone != null) {
			try (
				PreparedStatement st = DbConnector.createStatement(ActionStatement.CHANGE_TIMEZONE.str);
			) {
				st.setString(1, newTimeZone.getID());
				st.setString(2, name);
				if (st.executeUpdate() == 0) {
					return HandlingError.NO_SUCH_USER;
				}
			}
		}
		if (newActive != null) {
			try (
				PreparedStatement st = DbConnector.createStatement(ActionStatement.CHANGE_ACTIVE.str);
			) {
				st.setBoolean(1, newActive.booleanValue());
				st.setString(2, name);
				if (st.executeUpdate() == 0) {
					return HandlingError.NO_SUCH_USER;
				}
			}
		}
		return HandlingError.NO_ERROR;
	}
}
