import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Created by Pavel on 17.02.2015.
 */
public class Sender {
    public static int message(int nsct, JTextArea t) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        String prm = "action=test&login=Pavel";
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
        Scanner sc = new Scanner(in);
        String res = sc.next();
        //String id = in.readLine();
        String soc = sc.next();
        Socket sock = new Socket(InetAddress.getByName("localhost"),Integer.parseInt(soc));
        PrintWriter w = new PrintWriter(sock.getOutputStream());
        String s = con.getHeaderField("Set-Cookie");
        System.out.print(s.substring(11, 11+32));
        w.print(s.substring(11, 11+32)+ "\n");
        w.flush();

        Listener l = new Listener(sock);
        /*while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();*/
        System.out.println(res);
        System.out.println(soc);
        //System.out.println(response.toString());
        return responseCode;
    }
    public static int create(int nsct, String login, String pass, String zone) throws Exception
    {
        URL url = new URL("http://localhost:8080/Server/hello");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("PUT");
        String prm = "action=create_user&login="+login+"&password="+pass+"&timezone="+zone;
        System.out.println(nsct);
        con.setDoOutput(true);
        DataOutputStream os = new DataOutputStream(con.getOutputStream());
        os.writeBytes(prm);
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        if(responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Scanner sc = new Scanner(in);
            //String res = sc.next();
            //String id = in.readLine();
            String soc = sc.next();
            Socket sock = new Socket(InetAddress.getByName("localhost"), Integer.parseInt(soc));
            PrintWriter w = new PrintWriter(sock.getOutputStream());
            String s = con.getHeaderField("Set-Cookie");
            System.out.print(s.substring(11, 11 + 32));
            w.print(s.substring(11, 11 + 32) + "\n");
            w.flush();

            Listener l = new Listener(sock);
        }

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
