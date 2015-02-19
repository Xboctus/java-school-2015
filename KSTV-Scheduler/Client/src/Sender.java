import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by Pavel on 17.02.2015.
 */
public class Sender {
    public static int message(int nsct) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        String prm = "action=test&login=Pavel&listen_port="+nsct;
        System.out.println(nsct);
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        return responseCode;
    }
    public static int create(int nsct, String login, String pass, String zone) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=create_user&login="+login+"&password="+pass+"&timezone="+zone+"&listen_port"+nsct;
        System.out.println(nsct);
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
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
    public static int login(int nsct, String login, String pass) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=start_session&login="+login+"&password="+pass+"&listen_port"+nsct;
        System.out.println(nsct);
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
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
    public static JTable showInfo(JTextArea tp, String log) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=user_info"+"&login="+log;
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        Object[] cn = {"Time", "Text"};
        Object[][] cs = new Object[100][2];
        int i = 0;
        while ((inputLine = in.readLine()) != null) {
            if (i % 2 == 0) {
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                cs[i/2][0] = df.format(df.parse(inputLine).getTime() - 3600000);
            }
            else
                cs[i/2][1] = inputLine;
            i++;
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
