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

	final private static String C_QUIT = "quit";
	final private static String C_UADD = "uadd";
	final private static String C_UREM = "urem";
	final private static String C_UMOD = "umod";
	final private static String C_UINFO = "uinfo";
	final private static String C_EADD = "eadd";
	final private static String C_EREM = "erem";
	final private static String C_ECLONE = "eclone";
	final private static String C_ERAND = "erand";
	final private static String C_START = "start";

	public enum Error {
		NO_ERROR,
		USER_ALREADY_EXISTS,
		NO_SUCH_USER,
		INVALID_DATE_FORMAT,
		EVENT_ALREADY_EXISTS,
		NO_SUCH_EVENT,
		UNKNOWN_COMMAND,
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
		out.format("%s, %s, %b, %d event(s)\n", name, user.getTimeZone().getDisplayName(), user.getActive(), events.size()); // TODO: improve formatting (active/not active, singular/plural event(s))
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

	static String succesful_response(String command) {
		if (command.equals(C_UADD)) {
			return "User created!";
		} else if (command.equals(C_UMOD)) {
			return "User modified!";
		} else if (command.equals(C_EADD)) {
			return "Event added!";
		} else if (command.equals(C_EREM)) {
			return "Event removed!";
		} else if (command.equals(C_ERAND)) {
			return "Random time event added!";
		} else if (command.equals(C_ECLONE)) {
			return "Event cloned!";
		} else {
			return "";
		}
	}

	static String error_response(Error error) {
		switch (error) {
		case USER_ALREADY_EXISTS:
			return "This name is already used!";
		case NO_SUCH_USER:
			return "No user with this name!";
		case EVENT_ALREADY_EXISTS:
			return "Such event already exists!";
		case NO_SUCH_EVENT:
			return "No such event!";
		case INVALID_DATE_FORMAT:
			return "Invalid date format!";
		case UNKNOWN_COMMAND:
			return "Unknown command!";
		default:
			return "";
		}
	}

	public static void main(String[] args) {
		while (true) {
			out.print(">: ");
			out.flush();
			String command = in.next();

			Error error;

			if (command.equals(C_UADD)) {
				String name = in.next();
				String timeZone = in.next();
				boolean active = in.nextBoolean();
				error = create_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
			} else if (command.equals(C_UMOD)) {
				String name = in.next();
				String timeZone = in.next();
				boolean active = in.nextBoolean();
				error = modify_user(name, TimeZone.getTimeZone("GMT" + timeZone), active);
			} else if (command.equals(C_UINFO)) {
				String name = in.next();
				error = show_user_info(name);
			} else if (command.equals(C_EADD)) {
				String name = in.next();
				String text = in.next();
				String date = in.next();
				try {
					error = add_event(name, text, dateFormat.parse(date));
				} catch (ParseException e) {
					error = Error.INVALID_DATE_FORMAT;
				}
			} else if (command.equals(C_EREM)) {
				String name = in.next();
				String text = in.next();
				error = remove_event(name, text);
			} else if (command.equals(C_ERAND)) {
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
			} else if (command.equals(C_ECLONE)) {
				String name = in.next();
				String text = in.next();
				String nameTo = in.next();
				error = clone_event(name, text, nameTo);
			} else if (command.equals(C_QUIT)) {
				return;
			} else if (command.equals(C_START)) {
				break;
			} else {
				error = Error.UNKNOWN_COMMAND;
			}

			if (error == Error.NO_ERROR) {
				out.println(succesful_response(command));
			} else {
				err.println(error_response(error));
			}
		}

		out.println("Scheduling started!");

		HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> schedule = new HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>>();
		for (User user: users.values()) {
			for (Event event: user.getEvents().values()) {
				TreeMap<String /*name*/, ArrayList<String /*text*/>> date_sch = schedule.get(event.getDate());
				if (date_sch == null) {
					date_sch = schedule.put(event.getDate(), new TreeMap<String /*name*/, ArrayList<String /*text*/>>());
				}
				ArrayList<String /*text*/> user_sch = date_sch.get(user.getName());
				if (user_sch == null) {
					user_sch = date_sch.put(user.getName(), new ArrayList<String /*text*/>());
				}
				user_sch.add(event.getText());
			}
		}

		for (TreeMap<String /*name*/, ArrayList<String /*text*/>> date_sch: schedule.values()) {
			for (ArrayList<String /*text*/> user_sch: date_sch.values()) {
				Collections.sort(user_sch);
			}
		}

		Timer timer = new Timer();
		for (Map.Entry<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> e: schedule.entrySet()) {
			timer.schedule(new ScheduleTask(e.getKey(), e.getValue()), e.getKey());
		}
	}
}
