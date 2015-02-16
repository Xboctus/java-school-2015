import java.io.PrintWriter;
import java.util.*;
import java.text.SimpleDateFormat;

public class Coordinator {
	public static PrintWriter out;

	public static HashMap<String, User> users = new HashMap<String, User>();
	private static SimpleDateFormat localDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss z");

	public static class ScheduleTask extends TimerTask {
		private Date date;
		private TreeMap<String /*name*/, ArrayList<String /*text*/>> user_sch;

		static Timer timer;
		static int n_tasks = 0;

		ScheduleTask(Date date, TreeMap<String /*name*/, ArrayList<String /*text*/>> user_sch) {
			this.date = date;
			this.user_sch = user_sch;
			++n_tasks;
		}

		@Override
		public void run() {
			SimpleDateFormat userDateFormat = (SimpleDateFormat)localDateFormat.clone();
			for (Map.Entry<String /*name*/, ArrayList<String /*text*/>> e: user_sch.entrySet()) {
				userDateFormat.setTimeZone(users.get(e.getKey()).getTimeZone());
				for (String text: e.getValue()) {
					out.format(
						"%s (user %s), %s, %s\n",
						localDateFormat.format(date),
						userDateFormat.format(date),
						e.getKey(), text
					);
				}
			}
			--n_tasks;
			if (n_tasks == 0) {
				timer.cancel();
			}
		}
	}

	public enum Command {
		UADD("Create user", "User created!"),
//		UREM("", "User removed!"),
		UMOD("Modify user", "User modified!"),
		UINFO("User info", null),
		EADD("Add event", "Event added!"),
		EREM("Remove event", "Event removed!"),
		ECLONE("Clone event", "Event cloned!"),
		ERAND("Add random time event", "Random time event added!"),
		START("Start scheduling", "Scheduling started!"),
		QUIT("Quit", null);

		public final String title;
		public final String r; // response

		private Command(String title, String r) {
			this.title = title;
			this.r = r;
		}
	}

	public enum Error {
		NO_ERROR(null),
		USER_ALREADY_EXISTS("This name is already used!"),
		NO_SUCH_USER("No user with this name!"),
		INVALID_DATE_FORMAT("Invalid date format!"),
		EVENT_ALREADY_EXISTS("Such event already exists!"),
		NO_SUCH_EVENT("No such event!"),
		UNKNOWN_COMMAND("Unknown command!");

		public String msg;

		private Error(String msg) {
			this.msg = msg;
		}
	}

	public static Error lastError;

	public static void createUser(String name, TimeZone timeZone, boolean active) {
		lastError = Error.NO_ERROR;
		if (users.get(name) != null) {
			lastError = Error.USER_ALREADY_EXISTS;
		}
		users.put(name, new User(name, timeZone, active));
	}

	public static void modifyUser(String name, TimeZone timeZone, boolean active) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		user.setActive(active);
		user.setTimeZone(timeZone);
	}

	public static void showUserInfo(String name, PrintWriter out) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		HashMap<String, Event> events = user.getEvents();
		out.format(
			"%s, %s, %s, %d event%s\n",
			name, user.getTimeZone().getDisplayName(),
			(user.getActive() ? "active" : "passive"),
			events.size(), (events.size() == 1 ? "" : "s")
		);
		SimpleDateFormat userDateFormat = (SimpleDateFormat)localDateFormat.clone();
		userDateFormat.setTimeZone(user.getTimeZone());
		for (Event e: events.values()) {
			out.format(
				"%s (%s), %s\n",
				localDateFormat.format(e.getDate()),
				userDateFormat.format(e.getDate()),
				e.getText()
			);
		}
	}

	// date is local (relative to GMT)
	public static void addEvent(String name, String text, Date date) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		user.addEvent(new Date(date.getTime() - user.getTimeZone().getRawOffset()), text);
	}

	public static void addGlobalEvent(String name, String text, Date date) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		user.addEvent(date, text);
	}

	public static void removeEvent(String name, String text) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		user.getEvents().remove(text);
	}

	// dateFrom and dateTo are local (relative to GMT)
	public static void addRandomTimeEvent(String name, String text, Date dateFrom, Date dateTo) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		if (user.getEvents().get(text) != null) {
			lastError = Error.EVENT_ALREADY_EXISTS;
		}
		long diff = dateTo.getTime() - dateFrom.getTime();
		Date date = new Date(dateFrom.getTime() + (long)((diff + 1)*Math.random()));
		user.addEvent(new Date(date.getTime() - user.getTimeZone().getRawOffset()), text);
	}

	public static void cloneEvent(String name, String text, String nameTo) {
		lastError = Error.NO_ERROR;
		User user = users.get(name);
		if (user == null) {
			lastError = Error.NO_SUCH_USER;
		}
		User userTo = users.get(nameTo);
		if (userTo == null) {
			lastError = Error.NO_SUCH_USER;
		}
		Event event = user.getEvents().get(text);
		if (event == null) {
			lastError = Error.NO_SUCH_EVENT;
		}
		if (userTo.getEvents().get(text) != null) {
			lastError = Error.EVENT_ALREADY_EXISTS;
		}
		userTo.addEvent(event.getDate(), event.getText());
	}
}
