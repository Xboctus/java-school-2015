import java.util.*;

public class Event {
	private Date date;
	private String text;

	public Event(Date date, String text) {
		this.date = date;
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public String getText() {
		return text;
	}
}
