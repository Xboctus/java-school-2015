import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

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
}
