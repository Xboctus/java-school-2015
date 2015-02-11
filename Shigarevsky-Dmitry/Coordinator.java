import java.io.PrintWriter;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;

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

		ScheduleTask(Date date, TreeMap<String /*name*/, ArrayList<String /*text*/>> user_sch) {
			this.date = date;
			this.user_sch = user_sch;
		}

		public void run() {
			for (Map.Entry<String /*name*/, ArrayList<String /*text*/>> e: user_sch.entrySet()) {
				for (String text: e.getValue()) {
					out.format("%s, %s, %s\n", date.toString(), e.getKey(), text);
				}
			}
		}
	}

	private enum Command {
		QUIT(null),
		UADD("User created!"),
		UREM("User removed!"),
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
			(user.getActive() ? "active" : "not active"),
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
		create_user("user", TimeZone.getTimeZone("GMT+3"), true);
		add_event("user", "hello", new Date(0));

		boolean interactive = true;
		while (interactive) {
			out.print(">: ");
			out.flush();

			// FIXME: read entire line to prevent arguments to unknown command from being subsequently interpreted as unknown commands themselves
			String command = in.next();

			Error error = null;

			Command c;

			try {
				c = Command.valueOf(command.toUpperCase());
			} catch (IllegalArgumentException e) {
				c = null;
			}

			if (c == null) {
				error = Error.UNKNOWN_COMMAND;
			} else {
				switch (c) {
					case UADD: {
						String name = in.next();
						String timeZone = in.next();
						boolean active = in.nextBoolean();
						error = create_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UMOD: {
						String name = in.next();
						String timeZone = in.next();
						boolean active = in.nextBoolean();
						error = modify_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UINFO: {
						String name = in.next();
						error = show_user_info(name);
						break;
					}
					case EADD: {
						String name = in.next();
						String text = in.next();
						String date = in.next();
						try {
							error = add_event(name, text, dateFormat.parse(date));
						} catch (ParseException e) {
							error = Error.INVALID_DATE_FORMAT;
						}
						break;
					}
					case EREM: {
						String name = in.next();
						String text = in.next();
						error = remove_event(name, text);
						break;
					}
					case ERAND: {
						String name = in.next();
						String text = in.next();
						String dateFrom = in.next();
						String dateTo = in.next();
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
						String name = in.next();
						String text = in.next();
						String nameTo = in.next();
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
		Date now = new Date();
		for (User user: users.values()) {
			if (!user.getActive()) {
				continue;
			}
			for (Event event: user.getEvents().values()) {
				if (event.getDate().compareTo(now) < 0) {
					continue;
				}
				TreeMap<String /*name*/, ArrayList<String /*text*/>> date_sch = schedule.get(event.getDate());
				if (date_sch == null) {
					schedule.put(event.getDate(), new TreeMap<String /*name*/, ArrayList<String /*text*/>>());
					date_sch = schedule.get(event.getDate());
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
		Timer timer = new Timer();
		for (Map.Entry<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> e: schedule.entrySet()) {
			timer.schedule(new ScheduleTask(e.getKey(), e.getValue()), e.getKey());
		}
	}
}
