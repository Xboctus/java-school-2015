import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;



public class Scheduler {
	
	/*public static void commandLineReader(String[] args){
		Scanner inn = new Scanner(System.in);
		System.out.println("Enter the command line");
		args = inn.nextLine().split(",");
		for(String val : args){
			val = val.trim();
			System.out.println(val);
		}
		inn.close();
	}*/
	
	public static void main(String[] args) {
		//User user1 = new User("Shema",TimeZone.getDefault(),true);
		//User user1 = new User("Shema",TimeZone.getTimeZone("GMT+3"),true);
		
		//List<User> users = new ArrayList<User>();
		
		//users.add(user1);
		
		//System.out.println(users.get(0).getTimeZone().getID());
		//GregorianCalendar calendar1 = new GregorianCalendar(2015,01,12,19,26);
		//GregorianCalendar calendar2 = new GregorianCalendar(2015,01,12,19,27);
		//System.out.println(calendar.getTime());
		
		//users.get(0).addEvent("Buy a car", calendar1);
		//users.get(0).addEvent("Call mama", calendar2);
		//System.out.println(users.get(0).events.get(0).getGregorianCalendar().getTime());
		//System.out.println(users.get(0).events.get(1).getGregorianCalendar().getTime());
		//Random rand = new Random();
		//System.out.println(Math.abs(rand.nextLong()));
		
		//Long randDate = calendar1.getTimeInMillis() + (Math.abs(rand.nextLong()) % (calendar2.getTimeInMillis() - calendar1.getTimeInMillis()));
		
		//GregorianCalendar calendar3 = new GregorianCalendar();
		//calendar3.setTimeInMillis(randDate);
		//System.out.println(calendar3.getTimeInMillis());
		//System.out.println(calendar3.getTime());
		//users.get(0).showInfo();
		
		//Edit edit = new Edit();
		//edit.removeEvent(users, "Shema", "Buy a car");
		//users.get(0).showInfo();
		//Scanner in = new Scanner(System.in);
		//String command = in.nextLine();
		//System.out.println();
		//in.close();
		
		List<User> users = new ArrayList<User>();
		Edit edit = new Edit();
		
		System.out.println("1.Create(name, timezone, active) ");
		System.out.println("2.Modify(name, timezone, active) ");
		System.out.println("3.AddEvent(name, text, datetime) ");
		System.out.println("4.RemoveEvent(name, text) ");
		System.out.println("5.AddRandomTimeEvent(name, text, dateFrom, dateTo) ");
		System.out.println("6.CloneEvent(name, text, nameTo)  ");
		System.out.println("7.ShowInfo(name) ");
		System.out.println("8.StartScheduling");
		
		
		Scanner in = new Scanner(System.in);
		String command = "";
		
		
		while(!command.equals("StartScheduling"))
		{
			command = in.nextLine();
			String[] arg = command.split(",");
			for( int i = 0; i < arg.length; i++)
				arg[i] = arg[i].trim();
			command = arg[0];
			
			try{
				switch(command) {
				case "Create" :
					edit.create(users, arg[1], TimeZone.getTimeZone(arg[2]), arg[3].equals("true") ? true : false);
					break;				
				case "Modify" :
					edit.modify(users, arg[1], TimeZone.getTimeZone(arg[2]), arg[3].equals("true") ? true : false);
					break;
				case "AddEvent" :
					SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");	
					GregorianCalendar calendar = new GregorianCalendar();
					Date d = new Date();
					try{
						d = date.parse(arg[3]);
					}
					catch (ParseException e){
						System.out.println("ERROR! ParseException");
						break;
					}
					calendar.setTime(d);
					edit.addEvent(users, arg[1], arg[2], calendar);
					break;
				case "RemoveEvent" :
					edit.removeEvent(users, arg[1], arg[2]);
					break;
				case "AddRandomTimeEvent" :
					SimpleDateFormat dateR = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");	
					GregorianCalendar calendarF = new GregorianCalendar();
					GregorianCalendar calendarT = new GregorianCalendar();
					Date dF = new Date();
					Date dT = new Date();
					try{
						dF = dateR.parse(arg[3]);
						dT = dateR.parse(arg[4]);
					}
					catch (ParseException e){
						System.out.println("ERROR! ParseException");
						break;
					}
					calendarF.setTime(dF);
					calendarT.setTime(dT);
					edit.addRandomTimeEvent(users, arg[1], arg[2], calendarF, calendarT);
					break;
				case "CloneEvent":
					edit.cloneEvent(users, arg[1], arg[2], arg[3]);
					break;
				case "ShowInfo" :
					for (User u : users)
						u.showInfo();
					break;
				case "StartScheduling":
					System.out.println("Scheduling mode is on");
					for (User u : users) {
						if(u.getStatus()){
							u.startEvents();
						}
					}
					break;
				default : 
					System.out.println("Unknown Command");
				}
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("ERROR! ArrayIndexOutOfBoundsException");
			}
			catch (NullPointerException e) {
				System.out.println("ERROR! NullPointerException");
			}
		}
		in.close();
	}

}
//Create, Shema, GMT+3, true
//Europe/Moscow
//Modify, Shemali, GMT+2,false
//ShowInfo
//AddEvent, Shemali, Hello my Darling, 04.12.1994-07:20:12
//ShowInfo
//Create, Shemali, GMT+3, true
//RemoveEvent,Shemali, Hello
//Create, Shema, GMT+3, false
//CloneEvent, Shemali,Darling,Shema
//ShowInfo
