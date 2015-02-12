import java.util.*;

public class User {
	private String name;
	private TimeZone timeZone;
	private boolean active;
	private HashMap<String, Event> events;

	public User(String name, TimeZone timeZone, boolean active) {
		this.name = name;
		this.timeZone = timeZone;
		this.active = active;
		this.events = new HashMap<String, Event>();
	}

	public String getName() {
		return name;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void addEvent(Date date, String text) {
		events.put(text, new Event(date, text));
	}

	public HashMap<String, Event> getEvents() {
		return events;
	}
}
