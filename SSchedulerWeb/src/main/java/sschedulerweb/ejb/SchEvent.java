package sschedulerweb.ejb;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "events")
public class SchEvent implements Serializable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String timeInstant;
    private String info;
    @ManyToOne
    @JoinColumn(name = "username")
    private SchUser owner;

    public SchEvent() {}

    SchEvent(String date, String eventInfo,
             SchUser user)
    {
        timeInstant = date;
        info = eventInfo;
        owner = user;
    }

    public String getTime() {
        return timeInstant;
    }

    public String getInfo() {
        return info;
    }

    public SchUser getOwner() {
        return owner;
    }

    public int getId() { return id; }

    public void setTime(String date) {
        timeInstant = date;
    }

    public void setInfo(String text) { info = text; }
}