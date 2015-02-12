import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ImmortalWolf on 10.02.2015.
 */
public class Event extends TimerTask {
    private Date date;
    private String text;
    private Timer timer;
    private User user;
    public static boolean show = false;

    public Event (String t, Date d, User u)
    {
        text = t;
        date = d;
        user = u;
        timer = new Timer();
        timer.schedule(this, date);
    }

    public void run()
    {
        if (user.getStatus() && show)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            System.out.println(formatter.format(date) + " " + user.getName() + " " + text);
        }
        Thread.currentThread().stop();
    }

    public void ShowEvent()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+3"));
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

    public void deleteTimer()
    {
        timer.cancel();
    }
}
