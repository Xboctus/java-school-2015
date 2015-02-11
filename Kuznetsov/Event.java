/**
 * Created by Pavel on 09.02.2015.
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Event extends TimerTask {
    private String text;
    private Date time;
    private Timer timer;
    private String name;
    public Event(Date time, String text,String name)
    {
        this.time = time;
        this.text = text;
        this.name = name;
        timer = new Timer();
        timer.schedule(this, time);

    }
    public void stop()
    {
        timer.cancel();
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
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY-HH:mm:ss");
        System.out.println(df.format(new Date()));
        System.out.println(name);
        System.out.println(text);
    }
}

