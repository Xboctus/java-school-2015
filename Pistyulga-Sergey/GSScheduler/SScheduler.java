
import java.util.*;
import java.text.*;

class SchUser {
	private String name;
	private boolean active;
	private TimeZone timeZone;
	
	SchUser(String userName, boolean isActive, String tzStr) {
		name = userName;
		modify(isActive,tzStr);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isactive() {
		return active;
	}
	
	public TimeZone getTimeZone() {
		return timeZone;
	}
	
	public void modify(boolean isActive, String tzStr) {
		active = isActive;
		timeZone = TimeZone.getTimeZone(tzStr);
	}
}

class SchEvent extends TimerTask implements Comparable<SchEvent> {
	private Date timeInstant;
	private String info;
	private SchUser owner;
	private SimpleDateFormat dFormat;
	private static LogHolder logHolder;
	private static Timer timer = new Timer();
	
	SchEvent(String formattedDate, String eventInfo,
				SchUser user, SimpleDateFormat dateFormat)
			throws ParseException
	{
		this((Date)null,eventInfo,user,dateFormat);
		timeInstant = dFormat.parse(formattedDate);
		timer.schedule(this, getDate());
	}
	
	SchEvent(Date date, String eventInfo,
			SchUser user, SimpleDateFormat dateFormat)
		throws ParseException
	{
		info = eventInfo;
		owner = user;
		dFormat = (SimpleDateFormat)dateFormat.clone();
		dFormat.setTimeZone(owner.getTimeZone());
		if (date!=null) {
			String defaultDate = dateFormat.format(date);
			timeInstant = dFormat.parse(defaultDate);
			timer.schedule(this, getDate());
		}
	}
	
	public static LogHolder getLogHolder() {
		return logHolder;
	}
	
	public static void setLogHolder(LogHolder log) {
		logHolder = log;
	}
	
	public String getTime() {
		SimpleDateFormat tempFormat = (SimpleDateFormat)dFormat.clone();
		tempFormat.setTimeZone(TimeZone.getDefault());
		return tempFormat.format(timeInstant);
	}
	
	public Date getDate() {
		return timeInstant;
	}
	
	public String getInfo() {
		return info;
	}
	
	public SchUser getOwner() {
		return owner;
	}
	
	public void setOwner(SchUser newOwner) {
		owner = newOwner;
	}
	
	public void setDate(Date date) {
		timeInstant = date;
	}
	
	public void setInfo(String text) {
		info = text;
	}
	
	public int compareTo(SchEvent e2) {
		int usernameDiff = getOwner().getName().compareTo(e2.getOwner().getName());
		if (usernameDiff!=0) return -usernameDiff;
		return -getInfo().compareTo(e2.getInfo());
	}
	
	public Object clone()
	{
		try {
			return new SchEvent((Date)timeInstant.clone(), info.substring(0),
					owner, SScheduler.dateFormat);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public boolean equals(Object o2) {
		if (o2 instanceof SchEvent)
			return getOwner()==((SchEvent)o2).getOwner() && getInfo().equals(((SchEvent) o2).getInfo());
		return false;
	}

	public void run() {
		if (getOwner().isactive())
			logHolder.print(getOwner().getName()+" "
				+getInfo() +" at "+ getTime());
	}
}

public class SScheduler {
	
	private static final String
				DONEMSG = "Success!",
				UEXISTMSG = "User already exists!",
				UNOTFOUND = "User not found!",
				EEXISTMSG = "Event info is not unique!",
				ENOTFOUND = "No event with specified params!";
	
	public static void parseCmd(String cmdStr)
	{
		try {
		String[] args = cmdStr.split(" ");
		switch(args[0]) {
		case "Create":
			editUser(args,true);
			break;
		case "Modify":
			editUser(args,false);
			break;
		case "AddEvent":
			addEvent(args);
			break;
		case "RemoveEvent":
			removeEvent(args);
			break;
		case "AddRandomTimeEvent":
			addRandomEvent(args);
			break;
		case "CloneEvent":
			copyEvent(args);
			break;
		case "ShowInfo":
			showUserInfo(args);
			break;
		default:
			putmsg("Unknown command!");
		}
		}
		catch(ParseException e) {
			putmsg("Time string should be as dd.MM.yyyy-hh:mm:ss");
		}
	}
	
	public static SchUser findUser(String name) {
		SchUser existingUser = null;
		for (SchUser u : users)
			if (u.getName().equals(name)) {
				existingUser = u;
				break;
			}
		return existingUser;
	}
	
	public static SchEvent findEvent(String text, SchUser user) {
		SchEvent existingEvent = null;
		for(SchEvent e : events)
			if (e.getInfo().equals(text) && e.getOwner()==user) {
				existingEvent = e;
				break;
			}
		return existingEvent;
	}
	
	private static void editUser(String[] args, boolean isNew) {
		//if (args.length==4) {
			String name = args[1], tzStr = args[2],
					isActiveStr = args[3];
			boolean isGMT = false;
			if (tzStr.startsWith("GMT")) {
				isGMT = true;
				try {
					Integer.parseInt(tzStr.substring(3));
				}
				catch(NumberFormatException e) {
					isGMT = false;
				}
			}
			if (!isGMT) {
				putmsg("Timezone should be as GMT<number>");
				return;
			}
			boolean isActive = isActiveStr.equals("active");
			if (isActive || isActiveStr.equals("disabled")) {
				SchUser existingUser = findUser(name);
				if (isNew) {
					if (existingUser!=null) {
						putmsg(UEXISTMSG);
						return;
					}
					else {
						SchUser newUser = new SchUser(name, isActive, tzStr);
						users.add(newUser);
					}
				}
				else {
					if (existingUser!=null)
						existingUser.modify(isActive,tzStr);
					else {
						putmsg(UNOTFOUND);
						return;
					}
				}
				putmsg(DONEMSG);
				return;
			}
		//}
		//putmsg("Usage: "+args[0]+" <name> <timezone> <active|disabled>",true);
	}
	
	private static void addEvent(String[] args)
			throws ParseException
	{
		//if (args.length==4) {
			String name = args[1], text = args[2],
					time = args[3];
			SchUser existingUser = findUser(name);
			if (existingUser!=null) {
				SchEvent existingEvent = findEvent(text,existingUser);
				if (existingEvent==null) {
					events.add(new SchEvent(time, text, existingUser, dateFormat));
					putmsg(DONEMSG);
				}
				else putmsg(EEXISTMSG);
			}
			else putmsg(UNOTFOUND);
		//}
		//else putmsg("Usage: "+args[0]+" <name> <text> <datetime>",true);
	}
	
	private static void removeEvent(String[] args) {
		//if (args.length==3) {
			String name = args[1], text = args[2];
			SchUser existingUser = findUser(name);
			if (existingUser!=null) {
				SchEvent existingEvent = findEvent(text,existingUser);
				if (existingEvent!=null) {
					existingEvent.cancel();
					events.remove(existingEvent);
					putmsg(DONEMSG);
				}
				else putmsg(ENOTFOUND);
			}
			else putmsg(UNOTFOUND);
		//}
		//else putmsg("Usage: "+args[0]+" <name> <text>",true);
	}
	
	private static void addRandomEvent(String[] args)
				throws ParseException
	{
		//if (args.length==5) {
			String name = args[1], text = args[2], start = args[3],
					end = args[4];
			SchUser existingUser = findUser(name);
			if (existingUser!=null) {
				long startDate = dateFormat.parse(start).getTime(),
						endDate = dateFormat.parse(end).getTime();
				if (startDate<=endDate) {
					Random rnd = new Random();
					long dateNum = Math.abs((long)((endDate-startDate)*
							rnd.nextDouble())) + startDate;
					events.add(new SchEvent(new Date(dateNum), text, existingUser, dateFormat));
				}
				else putmsg("Left bound is greater than right!");
			}
			else putmsg(UNOTFOUND);
		//}
		//else putmsg("Usage: "+args[0]+" <name> <text> <start_date> <end_date>",true);
	}
	
	private static void copyEvent(String[] args) {
		//if (args.length==4) {
			String text = args[1], name1 = args[2], name2 = args[3];
			SchUser u1 = findUser(name1), u2 = findUser(name2);
			if (u1!=null && u2!=null) {
				SchEvent e = findEvent(text,u1);
				if (e!=null) {
					if (findEvent(text,u2)==null) {
						SchEvent newEvent = (SchEvent)e.clone();
						newEvent.setOwner(u2);
					}
					else putmsg(EEXISTMSG);
				}
				else putmsg(ENOTFOUND);
			}
			else putmsg(UNOTFOUND);
		//}
		//else putmsg("Usage: "+args[0]+" <text> <src_user> <dst_user>",true);
	}
	
	private static void showUserInfo(String[] args) {
		//if (args.length==2) {
			String name = args[1];
			SchUser u = findUser(name);
			if (u!=null) {
				String msg = ("\nUser: "+name+" "+u.getTimeZone()+" "+
								(u.isactive() ? "active" : "disabled")+"\n\n");
				TreeMap<Date, String> eventsMap = new TreeMap<Date, String>();
				for(SchEvent e : events)
					if (e.getOwner()==u) {
						String info = "\n\t"+e.getInfo();
						String texts = eventsMap.get(e.getDate());
						eventsMap.put(e.getDate(),
								((texts==null) ? (e.getTime()+":"+info) :
									eventsMap.get(e.getDate())+info));
					}
				int i = 1;
				Set<Date> dates = eventsMap.keySet();
				for (Date date : dates)
					msg += (i++ +". "+eventsMap.get(date)+"\n");
				putmsg(msg);
			}
			else putmsg(UNOTFOUND);
		//}
		//else putmsg("Usage: "+args[0]+" <name>",true);
	}
	
	private static void putmsg(String message) {
		SchEvent.getLogHolder().print(message);
	}
	
	private static ArrayList<SchUser> users = new ArrayList<SchUser>();
	public static ArrayList<SchEvent> events = new ArrayList<SchEvent>();
	public static SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	
	public static ArrayList<SchUser> getUsers() {
		return users;
	}
	
	/*public static void main(String[] args) {
		System.out.print("\n>");
		Console con = System.console();
		if (con!=null) {
			try(BufferedReader rd = new BufferedReader(new FileReader("cmds.txt"))
					//new BufferedReader(con.reader())
			) {
				String cmdStr = rd.readLine();
				while(!cmdStr.equals("exit")) {
					parseCmd(cmdStr);
					System.out.print("\n>");
					cmdStr = rd.readLine();
				}
			}
			catch(IOException e) {
				System.out.println(e+": "+e.getMessage());
			}
			catch(ParseException e) {
				System.out.println("Time string should be as dd.MM.yyyy-hh:mm:ss");
			}
		}
		else System.out.println("Failed to assign console!");
	}*/
}
