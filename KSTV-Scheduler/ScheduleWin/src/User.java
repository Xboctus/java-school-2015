/**
 * Created by Pavel on 09.02.2015.
 */
import com.sun.javafx.collections.transformation.SortedList;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class User {
    private String name;
    private TimeZone zone;
    private boolean active;
    private boolean neu;
    private TreeSet<Event> events;
    public static JTextArea ta;
    public User(String name, TimeZone zone, boolean active, boolean neu)
    {
        this.name = name;
        this.zone = zone;
        this.active = active;
        events = new TreeSet<Event>(new Compare());
        this.neu = neu;
    }
    public String getName()
    {
        return this.name;
    }
    public Boolean isActive()
    {
        return active;
    }
    public void setNonNew()
    {
        neu = false;
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
    public void AddEvent(Date date, String text, boolean neu) throws IllegalArgumentException
    {
        int l = events.size();
        events.add(new Event(date, text, this, neu));
        if (neu) {
            if (events.size() == l)
                ta.append("Нельзя создать два события с одним текстом\n");
            else
                ta.append("Команда успешно выполнена\n");
        }
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
    public boolean isNew()
    {
        return neu;
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
        ta.append("Нет такого события\n");
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