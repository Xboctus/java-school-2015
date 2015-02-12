import java.io.PrintWriter;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Coordinator {
	private static HashMap<String, User> users = new HashMap<String, User>();
	private static Scanner in = new Scanner(System.in);
	private static PrintWriter out = new PrintWriter(System.out, true);
	private static PrintWriter err = new PrintWriter(System.err, true);
//	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static class ScheduleTask extends TimerTask {
		private Date date;
		private TreeMap<String /*name*/, ArrayList<String /*text*/>> user_sch;

		static Timer timer;
		static int n_tasks;

		ScheduleTask(Date date, TreeMap<String /*name*/, ArrayList<String /*text*/>> user_sch) {
			this.date = date;
			this.user_sch = user_sch;
		}

		@Override
		public void run() {
			for (Map.Entry<String /*name*/, ArrayList<String /*text*/>> e: user_sch.entrySet()) {
				for (String text: e.getValue()) {
					out.format("%s, %s, %s\n", date.toString(), e.getKey(), text);
				}
			}
			--n_tasks;
			if (n_tasks == 0) {
				timer.cancel();
			}
		}
	}

	private enum Command {
		QUIT(null),
		UADD("User created!"),
//		UREM("User removed!"),
		UMOD("User modified!"),
		UINFO(null),
		EADD("Event added!"),
		EREM("Event removed!"),
		ECLONE("Event cloned!"),
		ERAND("Random time event added!"),
		START("Scheduling started!");

		public final String r; // response

		private Command(String r) {
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

	private static Error create_user(String name, TimeZone timeZone, boolean active) {
		if (users.get(name) != null) {
			return Error.USER_ALREADY_EXISTS;
		}
		users.put(name, new User(name, timeZone, active));
		return Error.NO_ERROR;
	}

	private static Error modify_user(String name, TimeZone timeZone, boolean active) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		user.setActive(active);
		user.setTimeZone(timeZone);
		return Error.NO_ERROR;
	}

	private static Error show_user_info(String name) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		HashMap<String, Event> events = user.getEvents();
		out.format(
			"%s, %s, %s, %d event%s\n",
			name,
			user.getTimeZone().getDisplayName(),
			(user.getActive() ? "active" : "passive"),
			events.size(),
			(events.size() == 1 ? "" : "s")
		);
		for (Event e: events.values()) {
			out.format("%s, %s\n", e.getDate(), e.getText());
		}
		return Error.NO_ERROR;
	}

	private static Error add_event(String name, String text, Date date) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		user.addEvent(date, text);
		return Error.NO_ERROR;
	}

	private static Error remove_event(String name, String text) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		user.getEvents().remove(text);
		return Error.NO_ERROR;
	}

	private static Error add_random_time_event(String name, String text, Date dateFrom, Date dateTo) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		if (user.getEvents().get(text) != null) {
			return Error.EVENT_ALREADY_EXISTS;
		}
		long diff = dateTo.getTime() - dateFrom.getTime();
		Date date = new Date(dateFrom.getTime() + (long)((diff + 1)*Math.random()));
		user.addEvent(date, text);
		return Error.NO_ERROR;
	}

	private static Error clone_event(String name, String text, String nameTo) {
		User user = users.get(name);
		if (user == null) {
			return Error.NO_SUCH_USER;
		}
		User userTo = users.get(nameTo);
		if (userTo == null) {
			return Error.NO_SUCH_USER;
		}
		Event event = user.getEvents().get(text);
		if (event == null) {
			return Error.NO_SUCH_EVENT;
		}
		if (userTo.getEvents().get(text) != null) {
			return Error.EVENT_ALREADY_EXISTS;
		}
		userTo.addEvent(event.getDate(), event.getText());
		return Error.NO_ERROR;
	}

	public static void main(String[] args) {
		Date now = new Date();

		dateFormat.setLenient(false);

		create_user("user", TimeZone.getTimeZone("GMT+3"), true);
		add_event("user", "fastEvent", new Date(now.getTime() + 1000*15));
		add_event("user", "slowEvent", new Date(now.getTime() + 1000*30));

		boolean interactive = true;
		while (interactive) {
			out.print(">: ");
			out.flush();

			String[] words = in.nextLine().trim().split("\\s+");

			Error error = null;

			Command c;

			try {
				c = Command.valueOf(words[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				c = null;
			}

			if (c == null) {
				error = Error.UNKNOWN_COMMAND;
			} else {
				switch (c) {
					case UADD: {
						String name = words[1];
						String timeZone = words[2];
						boolean active = Boolean.parseBoolean(words[3]);
						error = create_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UMOD: {
						String name = words[1];
						String timeZone = words[2];
						boolean active = Boolean.parseBoolean(words[3]);
						error = modify_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UINFO: {
						String name = words[1];
						error = show_user_info(name);
						break;
					}
					case EADD: {
						String name = words[1];
						String text = words[2];
						String date = words[3];
						try {
							error = add_event(name, text, dateFormat.parse(date));
						} catch (ParseException e) {
							error = Error.INVALID_DATE_FORMAT;
						}
						break;
					}
					case EREM: {
						String name = words[1];
						String text = words[2];
						error = remove_event(name, text);
						break;
					}
					case ERAND: {
						String name = words[1];
						String text = words[2];
						String dateFrom = words[3];
						String dateTo = words[4];
						try {
							Date dFrom = dateFormat.parse(dateFrom);
							Date dTo = dateFormat.parse(dateTo);
							error = add_random_time_event(name, text, dFrom, dTo);
						} catch (ParseException e) {
							error = Error.INVALID_DATE_FORMAT;
						}
						break;
					}
					case ECLONE: {
						String name = words[1];
						String text = words[2];
						String nameTo = words[3];
						error = clone_event(name, text, nameTo);
						break;
					}
					case START: {
						interactive = false;
						error = Error.NO_ERROR;
						break;
					}
					case QUIT: {
						return;
					}
				}
			}

			if (error == Error.NO_ERROR) {
				if (c.r != null) {
					out.println(c.r);
				}
			} else {
				err.println(error.msg);
			}
		}

		HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> schedule = new HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>>();
		now = new Date();
		for (User user: users.values()) {
			if (!user.getActive()) {
				continue;
			}
			for (Event event: user.getEvents().values()) {
				if (event.getDate().compareTo(now) < 0) {
					continue;
				}
				Date date = new Date(event.getDate().getTime() - user.getTimeZone().getRawOffset());
				TreeMap<String /*name*/, ArrayList<String /*text*/>> date_sch = schedule.get(date);
				if (date_sch == null) {
					schedule.put(event.getDate(), new TreeMap<String /*name*/, ArrayList<String /*text*/>>());
					date_sch = schedule.get(date);
				}
				ArrayList<String /*text*/> user_sch = date_sch.get(user.getName());
				if (user_sch == null) {
					date_sch.put(user.getName(), new ArrayList<String /*text*/>());
					user_sch = date_sch.get(user.getName());
				}
				user_sch.add(event.getText());
			}
		}

		for (TreeMap<String /*name*/, ArrayList<String /*text*/>> date_sch: schedule.values()) {
			for (ArrayList<String /*text*/> user_sch: date_sch.values()) {
				Collections.sort(user_sch);
			}
		}

		// FIXME: consider user timezones
		ScheduleTask.timer = new Timer();
		ScheduleTask.n_tasks = schedule.size();
		for (Map.Entry<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> e: schedule.entrySet()) {
			ScheduleTask.timer.schedule(new ScheduleTask(e.getKey(), e.getValue()), e.getKey());
		}
	}
}
