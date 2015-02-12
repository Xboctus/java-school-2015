import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ImmortalWolf on 10.02.2015.
 */
public class Event {
    private Date date;
    private String text;
    public Event (String t, Date d)
    {
        text = t;
        date = d;
    }

    public void ShowEvent()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        System.out.println(formatter.format(date) + " " + text);
    }

    public String getText()
    {
        return text;
    }

    public Date getDate()
    {
        return date;
    }

    public Event getEvent()
    {
        return this;
    }
}
