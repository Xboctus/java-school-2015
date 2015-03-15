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
    private static JFrame jf;
    public Listener(Socket sct, JFrame jf)
    {
        this.sct = sct;
        ta = BaseForm.tp;
        this.jf = jf;
        setDaemon(true);
        start();
    }
    public void run()
    {
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(sct.getInputStream()));
            while (true)
            {
                Shared.GetPartsResult gp = Shared.getParts2(in);
                if (gp.parts[0].equals("event"))
                    for (String s:gp.parts)
                    {
                        ta.append(s+"\n");
                    }
                if (gp.parts[0].equals("invalidated"))
                    JOptionPane.showMessageDialog(jf, "Соединение признано недействительным");
                if (gp.parts[0].equals("shutdown"))
                    JOptionPane.showMessageDialog(jf, "Сервер завершил работу");
                for (String s:gp.parts)
                {
                    //ta.append(s+"\n");
                    System.out.println(s);
                }
            }

        }catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
