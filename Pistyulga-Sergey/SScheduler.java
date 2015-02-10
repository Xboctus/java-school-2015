import java.util.*;
import java.text.*;
import java.io.*;

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

class SchEvent implements Comparable<SchEvent> {
	private Date timeInstant;
	private String info;
	private SchUser owner;
	private SimpleDateFormat dFormat;
	
	SchEvent(String formattedDate, String eventInfo,
				SchUser user, SimpleDateFormat dateFormat)
			throws ParseException
	{
		this((Date)null,eventInfo,user,dateFormat);
		timeInstant = dFormat.parse(formattedDate);
	}
	
	SchEvent(Date date, String eventInfo,
			SchUser user, SimpleDateFormat dateFormat)
		throws ParseException
	{
		info = eventInfo;
		owner = user;
		dFormat = (SimpleDateFormat)SScheduler.dateFormat.clone();
		dFormat.setTimeZone(owner.getTimeZone());
		if (date!=null) {
			String defaultDate = SScheduler.dateFormat.format(date);
			timeInstant = dFormat.parse(defaultDate);
		}
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
			return getDate().equals(((SchEvent) o2).getDate());
		return false;
	}
}

public class SScheduler {
	
	private static void parseCmd(String cmdStr)
			throws ParseException
	{
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
		case "StartScheduling":
			if (args.length==1)
				runTasks();
			break;
		default:
			System.out.println("Unknown command!");
		}
	}
	
	private static SchUser findUser(String name) {
		SchUser existingUser = null;
		for (SchUser u : users)
			if (u.getName().equals(name)) {
				existingUser = u;
				break;
			}
		return existingUser;
	}
	
	private static SchEvent findEvent(String text, SchUser user) {
		SchEvent existingEvent = null;
		for(SchEvent e : events)
			if (e.getInfo().equals(text) && e.getOwner()==user) {
				existingEvent = e;
				break;
			}
		return existingEvent;
	}
	
	private static void editUser(String[] args, boolean isNew) {
		if (args.length==4) {
			String name = args[1], tzStr = args[2],
					isActiveStr = args[3];
			boolean isActive = isActiveStr.equals("active");
			if (isActive || isActiveStr.equals("disabled")) {
				SchUser existingUser = findUser(name);
				if (isNew) {
					if (existingUser!=null) {
						System.out.println("User "+name+" already exists!");
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
						System.out.println("User "+name+" not found!");
						return;
					}
				}
				System.out.println("Success!");
				return;
			}
		}
		System.out.println("Usage: "+args[0]+" <name> <timezone> <active|disabled>");
	}
	
	private static void addEvent(String[] args)
			throws ParseException
	{
		if (args.length==4) {
			String name = args[1], text = args[2],
					time = args[3];
			SchUser existingUser = findUser(name);
			if (existingUser!=null) {
				SchEvent existingEvent = findEvent(text,existingUser);
				if (existingEvent==null) {
					events.add(new SchEvent(time, text, existingUser, dateFormat));
					System.out.println("Success!");
				}
				else System.out.println("Event info is not unique!");
			}
			else System.out.println("User "+name+" not found!");
		}
		else System.out.println("Usage: "+args[0]+" <name> <text> <datetime>");
	}
	
	private static void removeEvent(String[] args) {
		if (args.length==3) {
			String name = args[1], text = args[2];
			SchUser existingUser = findUser(name);
			if (existingUser!=null) {
				SchEvent existingEvent = findEvent(text,existingUser);
				if (existingEvent!=null) {
					events.remove(existingEvent);
					System.out.println("Success!");
				}
				else System.out.println("No event with specified params!");
			}
			else System.out.println("User "+name+" not found!");
		}
		else System.out.println("Usage: "+args[0]+" <name> <text>");
	}
	
	private static void addRandomEvent(String[] args)
				throws ParseException
	{
		if (args.length==5) {
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
				else System.out.println("Left bound is greater than right!");
			}
			else System.out.println("User "+name+" not found!");
		}
		else System.out.println("Usage: "+args[0]+" <name> <text> <start_date> <end_date>");
	}
	
	private static void copyEvent(String[] args) {
		if (args.length==4) {
			String text = args[1], name1 = args[2], name2 = args[3];
			SchUser u1 = findUser(name1), u2 = findUser(name2);
			if (u1!=null && u2!=null) {
				SchEvent e = findEvent(text,u1);
				if (e!=null) {
					if (findEvent(text,u2)==null) {
						SchEvent newEvent = (SchEvent)e.clone();
						newEvent.setOwner(u2);
					}
					else System.out.println("Destination user has same event!");
				}
				else System.out.println("No event with specified params!");
			}
			else System.out.println("Users not found!");
		}
		else System.out.println("Usage: "+args[0]+" <text> <src_user> <dst_user>");
	}
	
	private static void showUserInfo(String[] args) {
		if (args.length==2) {
			String name = args[1];
			SchUser u = findUser(name);
			if (u!=null) {
				System.out.println("User: "+name+" "+u.getTimeZone()+" "+
								(u.isactive() ? "active" : "disabled")+"\n");
				TreeMap<Date, String> eventsMap = new TreeMap<Date, String>();
				for(SchEvent e : events)
					if (e.getOwner()==u) {
						String info = "\n\t"+e.getInfo();
						String texts = eventsMap.get(e.getDate());
						eventsMap.put(e.getDate(),
								((texts==null) ? (e.getDate()+":"+info) :
									eventsMap.get(e.getDate())+info));
					}
				int i = 1;
				Set<Date> dates = eventsMap.keySet();
				for (Date date : dates)
					System.out.println(i++ +". "+eventsMap.get(date));
				System.out.println();
			}
			else System.out.println("User "+name+" not found!");
		}
		else System.out.println("Usage: "+args[0]+" <name>");
	}
	
	private static void runTasks() {
		System.out.println("\nRunning scheduled tasks:\n---");
		final ArrayList<SchEvent> activeEvents = new ArrayList<SchEvent>();
		for(SchEvent e : events) {
			if (e.getOwner().isactive())
				activeEvents.add(e);
		}
		Collections.sort(activeEvents);
		Timer t = new Timer();
		while (!activeEvents.isEmpty()) {
				final SchEvent e = activeEvents.get(0);
				final ArrayList<SchEvent> eqEvents = new ArrayList<SchEvent>();
				for (SchEvent eachEvent : activeEvents)
					if (e.equals(eachEvent))
						eqEvents.add(eachEvent);
				TimerTask task = new TimerTask() {
					public void run() {
						for (SchEvent eachEqEvent : eqEvents)
							if (eachEqEvent.equals(e))
								System.out.println(new Date()+": "+
										eachEqEvent.getOwner().getName()+" "
										+eachEqEvent.getInfo() +" at "+ eachEqEvent.getDate());
					}
				};
				t.schedule(task, e.getDate());
				while (activeEvents.remove(e)) {};
		}
	}
	
	private static ArrayList<SchUser> users = new ArrayList<SchUser>();
	private static ArrayList<SchEvent> events = new ArrayList<SchEvent>();
	public static SimpleDateFormat dateFormat =
			new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
	
	public static void main(String[] args) {
		System.out.print("\n>");
		Console con = System.console();
		if (con!=null) {
			try {
				BufferedReader rd =  //new BufferedReader(new FileReader("cmds.txt"));
						new BufferedReader(con.reader());
				String cmdStr = rd.readLine();
				while(!cmdStr.equals("exit")) {
					parseCmd(cmdStr);
					System.out.print("\n>");
					cmdStr = rd.readLine();
				}
				rd.close();
			}
			catch(IOException e) {
				System.out.println(e+": "+e.getMessage());
			}
			catch(ParseException e) {
				System.out.println("Time string should be as dd.MM.yyyy-hh:mm:ss");
			}
		}
		else System.out.println("Failed to assign console!");
	}
}
