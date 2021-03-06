import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.servlet.ServletContext;

/**
 * Created by ImmortalWolf on 19.02.2015.
 */
public class Test {

    public enum Error {
        NO_ERROR,
        USER_ALREADY_EXISTS,
        NO_SUCH_USER,
        INVALID_DATE_FORMAT,
        EVENT_ALREADY_EXISTS,
        NO_SUCH_EVENT,
        UNKNOWN_COMMAND,
        INTERNAL_ERROR,
    }

    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public static void establishConnection(ServletContext sc) {
        //BufferedReader br = Files.newBufferedReader(Paths.get(sc.getRealPath("/WEB-INF/connection.txt")), StandardCharsets.UTF_8);
        //String conStr;
        try {
            con = DriverManager.getConnection("jdbc:odbc:Schedule");
        }
        catch (Exception e)
        {}
    }

    public static Error CreateUser(String login, String password, String timezone)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (!rs.next())
            {
                String q = "insert Users(name, pass, timezone, active) values (N'"+login+"', N'"+password+"', N'"+timezone+"', 1)";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(q);
                con.commit();
            }
            else
                return Error.USER_ALREADY_EXISTS;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error ModifyUser(String login, String password, String timezone, boolean active)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (rs.next())
            {
                String q = "UPDATE Users SET pass = N'"+password+"', timezone = N'"+timezone+"', active = N'"+active+"' WHERE name = N'"+login+"'";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(q);
                con.commit();
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error AddEvent(String login, String text, String datetime)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                rs = stmt.executeQuery("SELECT * FROM Evnts WHERE userID = "+userID+" AND msg = "+text);
                if (rs.next())
                {
                    return Error.EVENT_ALREADY_EXISTS;
                }
                else {
                    String q = "insert Evnts(dtime, msg, userID) values (N'"+datetime+"', N'"+text+"', N'"+userID+"')";
                    stmt.executeUpdate(q);
                    con.commit();
                }
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error RemoveEvent(String login, String text)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                rs = stmt.executeQuery("SELECT * FROM Evnts WHERE userID = "+userID+" AND msg = "+text);
                if (rs.next())
                {
                    String q = "DELETE FROM Evnts WHERE userID = N'"+userID+"' AND msg = N'"+text+"'";
                    stmt.executeUpdate(q);
                    con.commit();
                }
                else {
                    return Error.NO_SUCH_EVENT;
                }
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error AddRandomTimeEvent(String login, String text, String dateFrom, String dateTo)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                String timezone = rs.getString(4);
                rs = stmt.executeQuery("SELECT * FROM Evnts WHERE userID = "+userID+" AND msg = "+text);
                if (rs.next())
                {
                    return Error.EVENT_ALREADY_EXISTS;
                }
                else {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone(timezone));
                    java.util.Date date1 = formatter.parse(dateFrom);
                    java.util.Date date2 = formatter.parse(dateTo);
                    Date datetime = new Date (((long) ((date2.getTime() - date1.getTime())*Math.random())) + date1.getTime());
                    String q = "insert Evnts(dtime, msg, userID) values (N'"+formatter.format(datetime)+"', N'"+text+"', N'"+userID+"')";
                    stmt.executeUpdate(q);
                    con.commit();
                }
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error CloneEvent(String login, String text, String loginTo)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                rs = stmt.executeQuery("SELECT * FROM Evnts WHERE userID = "+userID+" AND msg = "+text);
                if (rs.next())
                {
                    String datetime = rs.getNString(2);
                    rs = stmt.executeQuery("SELECT * FROM Users WHERE name = " + loginTo);
                    if (rs.next())
                    {
                        userID = rs.getInt(1);
                        String q = "insert Evnts(dtime, msg, userID) values (N'"+datetime+"', N'"+text+"', N'"+userID+"')";
                        stmt.executeUpdate(q);
                        con.commit();
                    }
                    else
                        return Error.NO_SUCH_USER;
                }
                else
                    return Error.NO_SUCH_EVENT;
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static User ShowInfo(String login)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT userID, timezone, active FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                String timezone = rs.getNString(2);
                boolean active = rs.getBoolean(3);
                User currentUser = new User(login, TimeZone.getTimeZone(timezone), active);
                rs = stmt.executeQuery("SELECT dtime, msg FROM Evnts WHERE userID = "+userID);
                while (rs.next())
                {
                    String dtime = rs.getNString(1);
                    String msg = rs.getNString(2);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                    java.util.Date datetime = formatter.parse(dtime);
                    currentUser.AddEvent(msg, datetime, false);
                }
                return currentUser;
            }
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        User empty = new User();
        return empty;
    }


    public static ArrayList<User> users = new ArrayList<User>();

    public static Error LoadingUserInfo(String login)
    {
        try
        {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT userID, timezone, active FROM Users WHERE name = " + login);
            if (rs.next())
            {
                int userID = rs.getInt(1);
                String timezone = rs.getNString(2);
                boolean active = rs.getBoolean(3);
                User currentUser = new User(login, TimeZone.getTimeZone(timezone), active);
                users.add(currentUser);
                rs = stmt.executeQuery("SELECT dtime, msg FROM Evnts WHERE userID = "+userID);
                while (rs.next())
                {
                    String dtime = rs.getNString(1);
                    String msg = rs.getNString(2);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                    java.util.Date datetime = formatter.parse(dtime);
                    currentUser.AddEvent(msg, datetime, true);
                }
            }
            else
                return Error.NO_SUCH_USER;
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }


    public static Error ClearUserInfo(String login)
    {
        try
        {
            for (int i = 0; i < users.size(); i++)
                if (login.equals(users.get(i).getName())) {
                    users.get(i).RemoveEvent();
                    break;
                }
        }
        catch (Exception e)
        {
            ;
        }
        finally {
            try
            {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.err.println("Error: " + ex.getMessage());
            }
        }
        return Error.NO_ERROR;
    }
}