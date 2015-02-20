package sschedulerweb.ejb;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import javax.ejb.Timer;

// Компонент, содержащий серверные действия:
// добавить юзера или событие, удалить событие и т.д.
@Stateless
public class SchedulerBean implements SchedulerBeanRemote {

    // Объект, взаимодействующий с БД
    @PersistenceContext(unitName = "scheduler")
    private EntityManager db;

    @Resource
    private SessionContext context;

    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Лист сообщений
    private static final ArrayList<String> msgs = new ArrayList<String>();

    private static final String
            DONEMSG = "Success!",
            UEXISTMSG = "User already exists!",
            UNOTFOUND = "User not found!",
            EEXISTMSG = "Event info is not unique!",
            ENOTFOUND = "No event with specified params!",
            TZMSG = "Timezone should be like 'GMT<number>'",
            TFORMAT = "Possible time format - yyyy-MM-dd HH:mm:ss";

    public void createTimer(Date d, int id) {
        context.getTimerService().createTimer(d,id);
    }

    @Timeout
    public void timeout(Timer timer) {
        int id = (int) timer.getInfo();
        List<SchEvent> list = db.createQuery(
                "select e from SchEvent e where e.id = "+id
        ).getResultList();
        if (list.size()>0) {
            SchEvent e = list.get(0);
            if (e.getOwner().isactive())
                putmsg(e.getOwner().getName()+" "+e.getInfo());
            db.remove(e);
        }
    }

    private boolean checkTimezoneFormat(String tzStr) {
        boolean timezoneIsCorrect = false;
        if (tzStr.startsWith("GMT")) {
            timezoneIsCorrect = true;
            try {
                Integer.parseInt(tzStr.substring(3));
            }
            catch (NumberFormatException e) {
                timezoneIsCorrect = false;
            }
        }
        return timezoneIsCorrect;
    }

    private void putmsg(String msg) {
        //msgs.add(new Date()+": "+msg);
        try (PrintStream stream = new PrintStream(new FileOutputStream(
                "/home/sergey/practice2015/SSchedulerWeb/target/SSchedulerWeb/events.log",true
        ))) {
            stream.println(new Date()+": "+msg);
            stream.flush();
        }
        catch (Exception e) {}
    }

    public void init() {
        List<SchEvent> events = db.createQuery(
                "select e from SchEvent e"
        ).getResultList();
        for (SchEvent e : events) {
            SimpleDateFormat tempFormat = (SimpleDateFormat)dateFormat.clone();
            tempFormat.setTimeZone(TimeZone.getTimeZone(e.getOwner().getTimeZone()));
            Date d = null;
            try {
                d = tempFormat.parse(e.getTime());
            }
            catch (ParseException exc) {}
            createTimer(d,e.getId());
        }
    }

    public void addUser(String username, String password, boolean active, String timeZone) {
        if (checkTimezoneFormat(timeZone)) {
            List<SchUser> list = db.createQuery(
                    "select u from SchUser u where u.username like '"+username+"'"
            ).getResultList();
            if (list.size()>0) {
                putmsg(UEXISTMSG);
                return;
            }
            db.persist(new SchUser(username, password, active, timeZone));
            putmsg(DONEMSG);
        }
        else putmsg(TZMSG);
    }

    public void modifyUser(String username, String password, boolean active, String timeZone) {
        if (checkTimezoneFormat(timeZone)) {
            List<SchUser> list = db.createQuery(
                    "select u from SchUser u where u.username like '"+username+"'"
            ).getResultList();
            if (list.size() > 0) {
                list.get(0).modify(password, active, timeZone);
                putmsg(DONEMSG);
            }
            else putmsg(UNOTFOUND);
        }
        else putmsg(TZMSG);
    }

    public void addEvent(String ownername, String date, String text) {
        List<SchUser> list = db.createQuery(
                "select u from SchUser u where u.username like '"+ownername+"'"
        ).getResultList();
        if (list.size() > 0) {
            SchUser u = list.get(0);
            SimpleDateFormat tempFormat = (SimpleDateFormat)dateFormat.clone();
            tempFormat.setTimeZone(TimeZone.getTimeZone(u.getTimeZone()));
            Date d = null;
            try {
                d = tempFormat.parse(date);
            }
            catch (ParseException e) {
                putmsg(TFORMAT);
                return;
            }
            List<SchEvent> list2 = db.createQuery(
                    "select e from SchEvent e where e.owner.username like '"+ownername+"'"+
                            " AND e.info like '"+text+"'"
            ).getResultList();
            if (list2.size()==0) {
                SchEvent e = new SchEvent(date, text, u);
                db.persist(e);
                createTimer(d,e.getId());
                putmsg(DONEMSG);
            }
            else putmsg(EEXISTMSG);
        }
        else putmsg(UNOTFOUND);
    }

    public void removeEvent(String ownername, String text) {
        List<SchEvent> list = db.createQuery(
                "select e from SchEvent e where e.owner.username like '"+ownername+"'"+
                        " AND e.info like '"+text+"'"
        ).getResultList();
        if (list.size()>0) {
            db.remove(list.get(0));
            putmsg(DONEMSG);
        }
        else putmsg(ENOTFOUND);
    }

    public void copyEvent(String srcuser, String destuser, String text) {
        List<SchUser> list1 = db.createQuery(
                "select u from SchUser u where u.username like '"+srcuser+"'"
        ).getResultList(),
                list2 = db.createQuery(
                        "select u from SchUser u where u.username like '"+destuser+"'"
                ).getResultList();
        if (list1.size()>0 && list2.size()>0) {
            List<SchEvent> list3 = db.createQuery(
                    "select e from SchEvent e where e.owner.username like '"+srcuser+"'"+
                            " AND e.info like '"+text+"'"
            ).getResultList(),
                    list4 = db.createQuery(
                            "select e from SchEvent e where e.owner.username like '"+destuser+"'"+
                                    " AND e.info like '"+text+"'"
                    ).getResultList();
            if (list3.size()>0) {
                if (list4.size()==0) {
                    SchEvent e = list3.get(0);
                    SimpleDateFormat tempFormat = (SimpleDateFormat)dateFormat.clone();
                    tempFormat.setTimeZone(TimeZone.getTimeZone(list1.get(0).getTimeZone()));
                    try {
                        Date date = tempFormat.parse(e.getTime());
                        tempFormat.setTimeZone(TimeZone.getTimeZone(list2.get(0).getTimeZone()));
                        SchEvent e2 = new SchEvent(tempFormat.format(date),text,list2.get(0));
                        db.persist(e2);
                        createTimer(date,e2.getId());
                        putmsg(DONEMSG);
                    }
                    catch (Exception exc) {
                        putmsg(exc.toString());
                    }
                }
                else putmsg(EEXISTMSG);
            }
            else putmsg(ENOTFOUND);
        }
        else putmsg(UNOTFOUND);
    }

    public SchUser getUser(String username) {
        List<SchUser> list = db.createQuery(
                "select u from SchUser u where u.username like '"+username+"'"
        ).getResultList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<SchEvent> getUserEvents(String username) {
        return db.createQuery(
                "select e from SchEvent e where e.owner.username like '"+username+"'"
        ).getResultList();
    }

    public void modifyEvent(int id, String date, String text) {
        List<SchEvent> list = db.createQuery(
                "select e from SchEvent e where e.id = "+id
        ).getResultList();
        if (list.size()>0) {
            SchEvent e = list.get(0);
            SimpleDateFormat tempFormat = (SimpleDateFormat)dateFormat.clone();
            tempFormat.setTimeZone(TimeZone.getTimeZone(e.getOwner().getTimeZone()));
            Date d = null;
            try {
                d = tempFormat.parse(date);
            }
            catch (ParseException exc) {
                putmsg(TFORMAT);
                return;
            }
            List<SchEvent> list2 = db.createQuery(
                    "select e from SchEvent e where e.owner.username like '"+e.getOwner().getName()+"'"+
                            " AND e.info like '"+text+"' AND e.id<>"+id
            ).getResultList();
            if (list2.size()==0) {
                Collection<Timer> timers = context.getTimerService().getTimers();
                for (Timer t : timers)
                    if (((int)t.getInfo())==id) {
                        t.cancel();
                        break;
                    }
                e.setTime(date);
                e.setInfo(text);
                createTimer(d,id);
            }
            else putmsg(EEXISTMSG);
        }
    }

    public ArrayList<String> getMessages() {
        return msgs;
    }
}