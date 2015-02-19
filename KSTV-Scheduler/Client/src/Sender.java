import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Pavel on 17.02.2015.
 */
public class Sender {
    public static void message(int nsct) throws Exception
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
    }
    public static boolean create() throws Exception
    {
        return true;
    }
    public static boolean addEvent() throws Exception
    {
        return true;
    }
    public static void showInfo() throws Exception
    {

    }
}
