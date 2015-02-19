import javax.swing.*;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Pavel on 19.02.2015.
 */
public class Listener extends Thread{
    private Socket sct;
    private static JTextArea ta;
    public static void listen(ServerSocket srvsct, JTextArea tar)
    {
        try{
            ta = tar;
            while(true)
            {
                new Listener(srvsct.accept());
            }
        }catch(Exception e)
        {
            System.out.println(e);

        }
    }
    public Listener(Socket sct)
    {
        this.sct = sct;
        setDaemon(true);
        start();
    }
    public void run()
    {
        try{
            InputStream is = sct.getInputStream();
            byte buf[] = new byte[64*1024];
            int r = is.read(buf);
            String data = new String(buf, 0, r);
            ta.append(data+"\n");
            sct.close();

        }catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
