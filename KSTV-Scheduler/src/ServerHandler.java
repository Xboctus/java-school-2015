import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ServerHandler {
	public enum Error {
		NO_ERROR,
		USER_ALREADY_EXISTS,
		NO_SUCH_USER,
		INVALID_DATE_FORMAT,
		EVENT_ALREADY_EXISTS,
		NO_SUCH_EVENT,
		UNKNOWN_COMMAND,
	}

	public static class UserInfoResult {
		public TimeZone timeZone;
		public boolean active;
		public HashMap<Date, String> events;
		Error error;
	}

	public static UserInfoResult userInfo(String loginFrom) {
		return new UserInfoResult();
	}
}
