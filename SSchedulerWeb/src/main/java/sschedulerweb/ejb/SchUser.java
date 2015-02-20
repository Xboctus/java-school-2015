package sschedulerweb.ejb;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "users")
public class SchUser implements Serializable {
    @Id
    private String username;
    private String password;
    private boolean active;
    private String timeZone;

    public SchUser() {}

    SchUser(String userName, String password, boolean isActive, String tzStr) {
        username = userName;
        modify(password, isActive, tzStr);
    }

    public String getName() {
        return username;
    }

    public String getPassword() { return password; }

    public boolean isactive() {
        return active;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void modify(String pw, boolean isActive, String tzStr) {
        password = pw;
        active = isActive;
        timeZone = tzStr;
    }
}
