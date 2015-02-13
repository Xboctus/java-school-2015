import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Random;
import java.util.TimerTask;


class Edit {	
	public int create (List<User> users,String name,TimeZone timezone,Boolean status) {
		//Create user;
		User u = new User(name,timezone,status);
		users.add(u);
		return 1;
	}
	public int modify (List<User> users,String name,TimeZone timezone,Boolean status){
		//Modify user
		
		for (int i = 0; i < users.size(); i++){
			if (name.equals(users.get(i).getName()))
				{
					users.get(i).setStatus(status);
					users.get(i).setTimeZone(timezone);
					return 1;
				}
		}
		return 0;
	}
	public int addEvent (List<User> users, String name, String text, GregorianCalendar date) {
		//Add modify
		
		for (int i = 0; i < users.size(); i++){
			if (name.equals(users.get(i).getName()))
				{
					users.get(i).addEvent(text, date);
					return 1;
				}
		}
		return 0;
	}
	public int removeEvent(List<User> users, String name, String text){
		//Remove event
		
		for (int i = 0; i < users.size(); i++){
			if (name.equals(users.get(i).getName()))
				{
					users.get(i).removeEvent(text);
					return 1;
				}
		}
		return 0;
	}
	public int addRandomTimeEvent(List<User> users, String name, String text, GregorianCalendar dateFrom, GregorianCalendar dateTo){
		//Add a new random-time event
		
		Random rand = new Random();
		Long randDate = dateFrom.getTimeInMillis() + Math.abs((rand.nextLong()) % (dateTo.getTimeInMillis() - dateFrom.getTimeInMillis()));
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(randDate);
		for (int i = 0; i < users.size(); i++){
			if (name.equals(users.get(i).getName()))
				{
					users.get(i).addEvent(text, calendar);
					return 1;
				}
		}
		return 0;
	}
	public int cloneEvent (List<User> users, String name, String text, String nameTo){
		//Clone Event
		
		int indexName = -1, indexNameTo = -1;
		
		for (int i = 0; i < users.size(); i++){
			if (name.equals(users.get(i).getName())){
					indexName = i;
			}
			if (nameTo.equals(users.get(i).getName())){
					indexNameTo = i;
			}
		}
		
		users.get(indexNameTo).addEvent(text, users.get(indexName).getCalendarOfEvent(text));
		
		return 0;
	}
	
	//public void startScheduling
	
}

