/**
 * Created by Pavel on 09.02.2015.
 */
import com.sun.javafx.collections.transformation.SortedList;

import java.text.SimpleDateFormat;
import java.util.*;

public class User {
    private String name;
    private TimeZone zone;
    private boolean active;
    private TreeSet<Event> events;
    public User(String name, TimeZone zone, boolean active)
    {
        this.name = name;
        this.zone = zone;
        this.active = active;
        events = new TreeSet<Event>(new Compare());
    }
    public String getName()
    {
        return this.name;
    }
    public Boolean isActive()
    {
        return active;
    }
    public TreeSet<Event> getEvents()
    {
        return events;
    }
    public TimeZone getTimezone()
    {
        return zone;
    }
    public Event getEvent(String text)
    {
        for (Event e: events)
        {
            if (e.getText().equals(text))
            {
                return e;
            }
        }
        return null;
    }
    public void Modify(TimeZone zone, boolean active)
    {
        this.zone = zone;
        this.active = active;
    }
    public void AddEvent(Date date, String text)
    {
        int l = events.size();
        events.add(new Event(date, text, this));
        if (events.size() == l)
            System.out.println("Нельзя создать два события с одним текстом");
        else
            System.out.println("Команда успешно выполнена");
    }
    public void ShowInfo()
    {
        System.out.print(name + " " + zone.getID()+" ");
        if (active)
            System.out.println("active");
        else
            System.out.println("not active");
        for (Event e: events)
        {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
            System.out.print(df.format(e.getDate())+" ");
            System.out.println(e.getText());
        }
    }
    public void RemoveEvent(String text)
    {
        for (Event e : events)
        {
            if (e.getText().equals(text))
            {
                e.stop();
                break;
            }
        }
        System.out.println("Нет такого события");
    }
}
class Compare implements Comparator<Event>
{
    @Override
    public int compare(Event o, Event o2) {
        int c = o.getText().compareTo(o2.getText());
        if (c == 0) return 0;
        if (o.getDate().after(o2.getDate()))
            return 1;
        if (o.getDate().before(o2.getDate()))
            return -1;
        return c;
    }
}