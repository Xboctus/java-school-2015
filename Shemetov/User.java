import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

class User {
	private String name;
	private TimeZone timezone;
	private Boolean status;
	private List<Event> events;
	
	public User (){
		
	}
	
	public void startEvents(){
		for (Event e : events)
			e.start();
	}
	
	public User (String names,TimeZone timezones,Boolean statuss){
		name = names;
		timezone = timezones;
		status = statuss;
		events = new ArrayList<Event>();
	}
	
	public int getEventsListSize(){
		return events.size();
	}
	
	public GregorianCalendar getCalendarOfEvent(String text){
		int pt = 0;
		for (int i = 0; i < events.size(); i++){
			if (text.equals(events.get(i).getInformation())){
				pt = i;
				break;
				//return events.get(i).getGregorianCalendar();
			}
		}
		return events.get(pt).getGregorianCalendar();
	}
	
	public void addEvent (String text, GregorianCalendar date){
		Event event = new Event(text, date, this.name);
		events.add(event);
	}
	
	public void removeEvent (String text){
		for (int i = 0; i < events.size(); i++){
			if ( text.equals(events.get(i).getInformation()))
				events.remove(i);
		}
	}
	
	public void setName (String val) {
		name = val;
	}
	
	public void setTimeZone (TimeZone val) {
		timezone = val;
	}
	
	public void setStatus (Boolean val) {
		status = val;
	}
	
	public String getName () {
		return name;
	}
	
	public TimeZone getTimeZone () {
		return timezone;
	}
	
	public Boolean getStatus () {
		return status;
	}
	
	public void showInfo() {
		System.out.println(name + " " + timezone.getID() + " " + status);
		for (Event event : events) {
			System.out.println(event.getInformation() + " " + event.getGregorianCalendar().getTime());
		}
	}
	
}