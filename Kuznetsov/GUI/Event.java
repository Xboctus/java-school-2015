/**
 * Created by Pavel on 09.02.2015.
 */
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class Event extends TimerTask {
    private String text;
    private Date time;
    private Timer timer;
    private User u;
    public static boolean ready = false;
    public static JTextArea area;
    public Event(Date time, String text,User u)
    {
        int m = TimeZone.getDefault().getRawOffset()-u.getTimezone().getRawOffset();
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
        if (u.isActive() && (time.getTime() >= new Date().getTime()-3600000)) {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
            area.append(df.format(new Date().getTime()-3600000)+"\n");
            area.append(u.getName()+"\n"+text+"\n");

        }
        u.getEvents().pollFirst();
        Thread.currentThread().stop();
    }
}

