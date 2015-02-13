import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Cli {
	private static Scanner in = new Scanner(System.in);
	private static PrintWriter out = new PrintWriter(System.out, true);
	private static PrintWriter err = new PrintWriter(System.err, true);
	//	private static SimpleDateFormat gmtDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	private static SimpleDateFormat gmtDateFormat = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat localDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss z");

	public static void run() {
		Coordinator.out = out;

		gmtDateFormat.setLenient(false);
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		localDateFormat.setTimeZone(TimeZone.getDefault());

		Date now = new Date();

		Coordinator.createUser("user", TimeZone.getTimeZone("GMT+5"), true);
		Coordinator.addGlobalEvent("user", "fastEvent", new Date(now.getTime() + 1000*10));
		Coordinator.addGlobalEvent("user", "slowEvent", new Date(now.getTime() + 1000*20));

		boolean interactive = true;
		while (interactive) {
			out.print(">: ");
			out.flush();

			String[] words = in.nextLine().trim().split("\\s+");

			Coordinator.Error error = null;

			Coordinator.Command c;

			try {
				c = Coordinator.Command.valueOf(words[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				c = null;
			}

			if (c == null) {
				error = Coordinator.Error.UNKNOWN_COMMAND;
			} else {
				switch (c) {
					case UADD: {
						String name = words[1];
						String timeZone = words[2];
						boolean active = Boolean.parseBoolean(words[3]);
						error = Coordinator.createUser(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UMOD: {
						String name = words[1];
						String timeZone = words[2];
						boolean active = Boolean.parseBoolean(words[3]);
						error = Coordinator.modifyUser(name, TimeZone.getTimeZone("GMT" + timeZone), active);
						break;
					}
					case UINFO: {
						String name = words[1];
						error = Coordinator.showUserInfo(name, out);
						break;
					}
					case EADD: {
						String name = words[1];
						String text = words[2];
						String date = words[3];
						try {
							error = Coordinator.addEvent(name, text, gmtDateFormat.parse(date));
						} catch (ParseException e) {
							error = Coordinator.Error.INVALID_DATE_FORMAT;
						}
						break;
					}
					case EREM: {
						String name = words[1];
						String text = words[2];
						error = Coordinator.removeEvent(name, text);
						break;
					}
					case ERAND: {
						String name = words[1];
						String text = words[2];
						String dateFrom = words[3];
						String dateTo = words[4];
						try {
							Date dFrom = gmtDateFormat.parse(dateFrom);
							Date dTo = gmtDateFormat.parse(dateTo);
							error = Coordinator.addRandomTimeEvent(name, text, dFrom, dTo);
						} catch (ParseException e) {
							error = Coordinator.Error.INVALID_DATE_FORMAT;
						}
						break;
					}
					case ECLONE: {
						String name = words[1];
						String text = words[2];
						String nameTo = words[3];
						error = Coordinator.cloneEvent(name, text, nameTo);
						break;
					}
					case START: {
						interactive = false;
						error = Coordinator.Error.NO_ERROR;
						break;
					}
					case QUIT: {
						return;
					}
				}
			}

			if (error == Coordinator.Error.NO_ERROR) {
				if (c.r != null) {
					out.println(c.r);
				}
			} else {
				err.println(error.msg);
			}
		}

		HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> schedule = new HashMap<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>>();
		now = new Date();
		for (User user: Coordinator.users.values()) {
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

		if (schedule.size() > 0) {
			Coordinator.ScheduleTask.timer = new Timer();
			for (Map.Entry<Date, TreeMap<String /*name*/, ArrayList<String /*text*/>>> e: schedule.entrySet()) {
				Coordinator.ScheduleTask.timer.schedule(new Coordinator.ScheduleTask(e.getKey(), e.getValue()), e.getKey());
			}
		}
	}
}
