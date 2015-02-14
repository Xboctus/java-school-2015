import java.util.GregorianCalendar;
import java.util.TimerTask;
import java.util.Timer;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class Event extends TimerTask{
	private String information;
	private GregorianCalendar calendar;
	private Timer timer;
	private String userName;
	
	@Override
	public void run(){
		SimpleDateFormat form = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
		form.setTimeZone(TimeZone.getTimeZone("GMT+3"));
		System.out.println(form.format(calendar.getTime()) + " " + userName + " " + information);
		Thread.currentThread().stop();
	}
	
	public Event (String text, GregorianCalendar date, String name){
		information = text;
		calendar = date;
		this.userName = name;
	}
	
	public void start () {
		timer = new Timer();
		timer.schedule(this, calendar.getTime());
	}
	
	public void setInformation (String val) {
		information = val;
	}
	
	public void setGregorianCalendar (GregorianCalendar val) {
		calendar = val;
	}
	
	public String getInformation () {
		return information;
	}
	
	public GregorianCalendar getGregorianCalendar () {
		return calendar;
	}
}
