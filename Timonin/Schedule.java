import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ImmortalWolf on 10.02.2015.
 */
public class Schedule {
    public static void main(String[] args)
    {
        ArrayList<User> users = new ArrayList<User>();
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        while (!s.equals("StartScheduling"))
        {
            int ind = s.indexOf('(');
            if (ind > 0) {
                String command = s.substring(0, ind);
                System.out.println(s);
                System.out.println(command);
                String name, timezone, status, text, datetime, nameTo, dateFrom, dateTo;
                boolean stat;
                switch (command) {
                    case "Create":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        timezone = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(')');
                        status = s.substring(0, ind);
                        if (status.equals("active"))
                            stat = true;
                        else
                            stat = false;
                        users.add(new User(name, TimeZone.getTimeZone(timezone), stat));
                        //users.add(new User("name", TimeZone.getTimeZone("Europe/Moscow"), true));
                        break;

                    case "Modify":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        timezone = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(')');
                        status = s.substring(0, ind);
                        if (status.equals("active"))
                            stat = true;
                        else
                            stat = false;
                        for (int i = 0; i < users.size(); i++)
                            if (name.equals(users.get(i).getName())) {
                                users.get(i).setTimeAndStat(TimeZone.getTimeZone(timezone), stat);
                                break;
                            }
                        break;

                    case "AddEvent":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        text = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(')');
                        datetime = s.substring(0, ind);

                        try {
                            for (int i = 0; i < users.size(); i++)
                                if (name.equals(users.get(i).getName()))
                                {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                    formatter.setTimeZone(users.get(i).getTimezone());
                                    Date date = formatter.parse(datetime);
                                    users.get(i).AddEvent(text, date);
                                    break;
                                }
                        } catch (ParseException e) {
                            System.out.println(e.getMessage());
                        }
                        break;

                    case "ShowInfo":
                        s = s.substring(ind + 1);
                        name = s.substring(0, s.length() - 1);
                        for (int i = 0; i < users.size(); i++)
                            if (name.equals(users.get(i).getName())) {
                                users.get(i).ShowUser();
                                break;
                            }
                        break;

                    case "RemoveEvent":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        text = s.substring(0, s.length() - 1);
                        for (int i = 0; i < users.size(); i++)
                            if (name.equals(users.get(i).getName())) {
                                users.get(i).RemoveEvent(text);
                                break;
                            }
                        break;

                    case "CloneEvent":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        text = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(')');
                        nameTo = s.substring(0, ind);
                        int fromUser = 0, toUser = 0;
                        int count = 0;
                        for (int i = 0; i < users.size(); i++)
                        {
                            if (name.equals(users.get(i).getName())) {
                                fromUser = i;
                                count++;
                                if (count == 2)
                                    break;
                            }
                            if (nameTo.equals(users.get(i).getName())) {
                                toUser = i;
                                count++;
                                if (count == 2)
                                    break;
                            }
                        }
                        Event temp = users.get(fromUser).getEvent(text);
                        users.get(toUser).AddEvent(temp.getText(), temp.getDate());
                        break;

                    case "AddRandomTimeEvent":
                        s = s.substring(ind + 1);
                        ind = s.indexOf(',');
                        name = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        text = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        ind = s.indexOf(',');
                        dateFrom = s.substring(0, ind);
                        s = s.substring(ind + 2);
                        dateTo = s.substring(0, s.length() - 1);
                        try
                        {
                            for (int i = 0; i < users.size(); i++)
                                if (name.equals(users.get(i).getName()))
                                {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                    formatter.setTimeZone(users.get(i).getTimezone());
                                    Date date1 = formatter.parse(dateFrom);
                                    Date date2 = formatter.parse(dateTo);
                                    Date date3 = new Date (((long) ((date2.getTime() - date1.getTime())*Math.random())) + date1.getTime());
                                    users.get(i).AddEvent(text, date3);
                                    break;
                                }
                        }
                        catch (ParseException e) {
                            System.out.println(e.getMessage());
                        }
                }
            }
            else
            {
                System.out.println("Error");
            }
            s = in.nextLine();
        }
        Event.show = true;
    }
}