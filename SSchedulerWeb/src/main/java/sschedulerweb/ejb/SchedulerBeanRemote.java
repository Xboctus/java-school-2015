package sschedulerweb.ejb;

import javax.ejb.Remote;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Implemented by SchedulerBean
@Remote
public interface SchedulerBeanRemote {
    void addUser(String username, String password, boolean active, String timeZone);
    void modifyUser(String username, String password, boolean active, String timeZone);
    void addEvent(String ownername, String date, String text);
    void modifyEvent(int id, String date, String text);
    void removeEvent(String ownername, String text);
    void copyEvent(String srcuser, String destuser, String text);
    SchUser getUser(String username);
    List<SchEvent> getUserEvents(String username);
    ArrayList<String> getMessages();
    void createTimer(Date d, int id);
    void init();
}
