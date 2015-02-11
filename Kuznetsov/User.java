/**
 * Created by Pavel on 09.02.2015.
 */
import com.sun.javafx.collections.transformation.SortedList;

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
    public void Modify(TimeZone zone, boolean active)
    {
        this.zone = zone;
        this.active = active;
    }
    public void AddEvent(Date date, String text)
    {
        events.add(new Event(date, text, name));
    }
    public void ShowInfo()
    {
        System.out.print(name + " " + zone.getID()+" ");
        if (active)
            System.out.println("active");
        else
            System.out.println("not active");
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

        if (o.getDate().after(o2.getDate()))
            return 1;
        if (o.getDate().before(o2.getDate()))
            return -1;
        return 0;
    }
}