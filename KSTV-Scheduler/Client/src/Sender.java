import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by Pavel on 17.02.2015.
 */
public class Sender {
    public static int create(String login, String pass, String zone, boolean active, StringBuilder cookies) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello/users");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        //String act = (active)?"active":"passive";
        //String prm = login+"\u001F"+pass+"\u001F"+zone+"\u001F"+act+"\u001E";
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        PrintWriter w = new PrintWriter(os);
        //os.writeBytes(prm);
        //os.flush();
        //os.close();
        String[] mas = new String[4];
        mas[0] = login; mas[1] = pass; mas[2] = zone; mas[3] = (active)?"true":"false";
        Shared.putParts(mas,w);
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);

        return responseCode;

    }
    public static int modify(String pass, String zone, Boolean active) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=change_user&password="+pass+"&timezone="+zone+"&active"+((active)?"true":"false");
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        return responseCode;

    }
    public static int login(String login, String pass, StringBuilder cookies) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello/login");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = login+"\u001F"+pass+"\u001F\u001E";
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        String headerName=null;
        for (int i=1; (headerName = con.getHeaderFieldKey(i))!=null; i++) {
                if (headerName.equals("Set-Cookie")) {
                    cookies.append(con.getHeaderField(i));
                }
        }
        System.out.println(responseCode);
        return responseCode;

    }
    public static int addEvent(String text, String date) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=add_event"+"&text="+text+"&datetime="+date;
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        return responseCode;
    }
    public static int addRanEvent(String text, String date, String date1) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=add_random_event"+"&text="+text+"&datetime_from="+date+"&datetime_to="+date1;
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        return responseCode;
    }
    public static JTable showInfo(JTextArea tp,StringBuilder cookie) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello/users/:me/events");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Cookie", cookie.toString());
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        Shared.GetPartsResult pr = Shared.getParts(in);
        int n = Integer.parseInt(pr.parts[0]);
        Object[] cn = {"Time", "Text"};
        Object[][] cs = new Object[n][2];
        for (int i = 1; i <= n; i++)
        {
            cs[i-1][0] = pr.parts[i*2-1];
            cs[i-1][1] = pr.parts[i*2];
        }
        in.close();
        return new JTable(cs,cn);
    }
    public static void stop() throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=stop_session";
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
    }
}
