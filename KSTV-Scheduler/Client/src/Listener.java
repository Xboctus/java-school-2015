import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Pavel on 19.02.2015.
 */
public class Listener extends Thread{
    private Socket sct;
    private static JTextArea ta;
    public Listener(Socket sct)
    {
        this.sct = sct;
        ta = BaseForm.tp;
        setDaemon(true);
        start();
    }
    public void run()
    {
        try{
            /*InputStream is = sct.getInputStream();
            byte buf[] = new byte[64*1024];
            int r = is.read(buf);
            String data = new String(buf, 0, r);
            ta.append(data+"\n");
            sct.close();*/
            BufferedReader in = new BufferedReader(new InputStreamReader(sct.getInputStream()));
            Scanner sc = new Scanner(in);
            while (true)
            {
                String s = sc.nextLine();
                ta.append(s+"\n");
                System.out.print(s+"\n");
            }

        }catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
