import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class Schedule {
    public static void main(String[] args)
    {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inp);
        ArrayList<User> users = new ArrayList<>();
        try {
            String str = reader.readLine();
            while (!str.equals("StartScheduling")) {
                String[] input = str.split(" ");
                switch (input[0])
                {
                    case "Create":
                        if (input[3].equals("active"))
                            users.add(new User(input[1],TimeZone.getTimeZone(input[2]),true));
                        if (input[3].equals("passive"))
                            users.add(new User(input[1],TimeZone.getTimeZone(input[2]),false));
                    break;
                    case "Modify":
                        for (User u: users)
                        {
                            if (u.getName().equals(input[1]))
                            {
                                if (input[3].equals("active"))
                                    u.Modify(TimeZone.getTimeZone(input[2]),true);
                                if (input[3].equals("passive"))
                                    u.Modify(TimeZone.getTimeZone(input[2]),false);
                                break;
                            }
                            System.out.println("Вы ввели некорректное имя");
                        }
                        break;
                    case "AddEvent":
                        for (User u: users)
                        {
                            if (u.getName().equals(input[1]))
                            {
                                String msg = "";
                                for (int i = 2; i < input.length-1; i++)
                                    msg += input[i] + " ";
                                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                Date d = df.parse(input[input.length-1]);
                                u.AddEvent(d, msg);
                                break;
                            }
                            System.out.println("Вы ввели некорректное имя");
                        }
                        break;
                    case "RemoveEvent":
                        for (User u: users)
                        {
                            if (u.getName().equals(input[1]))
                            {
                                String msg = "";
                                for (int i = 2; i < input.length-1; i++)
                                    msg += input[i] + " ";
                                u.RemoveEvent(msg);
                                break;
                            }
                            System.out.println("Вы ввели некорректное имя");
                        }
                        break;
                    case "AddRandomTimeEvent":
                        break;
                    case "CloneEvent":
                        break;
                }
                /*for(String v : input)
                {
                    System.out.println(v);
                }
                //GregorianCalendar dc = new GregorianCalendar(2015, 1, 10, 20, 23, 15);
                User u = new User("pasha", TimeZone.getTimeZone(("Europe/Moscow")), true);
                u.AddEvent(dc.getTime(), "Hello world!");
                u.ShowInfo();*/
                str = reader.readLine();
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println("fin");
    }
}
