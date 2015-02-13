/**
 * Created by Pavel on 09.02.2015.
 */
import java.text.SimpleDateFormat;
import java.util.*;

public class Event extends TimerTask {
    private String text;
    private Date time;
    private Timer timer;
    private User u;
    public static boolean ready = false;
    public Event(Date time, String text,User u)
    {
        int m = 4*3600000-u.getTimezone().getRawOffset();
        Date d = new Date(time.getTime()+m);
        this.time = d;
        this.text = text;
        this.u = u;
        timer = new Timer();
        timer.schedule(this, d);

    }
    public void stop()
    {
        timer.cancel();
        u.getEvents().pollFirst();
    }
    public Date getDate()
    {
        return time;
    }
    public String getText()
    {
        return text;
    }
    public void run()
    {
        if (u.isActive() && ready) {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
            System.out.println(df.format(new Date().getTime()-3600000));
            System.out.println(u.getName());
            System.out.println(text);
        }
        u.getEvents().pollFirst();
        Thread.currentThread().stop();
    }
}

