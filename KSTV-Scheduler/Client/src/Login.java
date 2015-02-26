import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Created by Pavel on 19.02.2015.
 */
public class Login extends JFrame {
    ServerSocket sct;
    public Login(ServerSocket sct)
    {
        setTitle("Login");
        setSize(450, 350);
        setResizable(false);
        setLocationRelativeTo(null);
        this.sct = sct;
    }
    public void initialize() {
        final JDialog jd = new JDialog();
        jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JLabel l1 = new JLabel("Name");
        final JLabel l2 = new JLabel("Timezone");
        JLabel l3 = new JLabel("Password");
        final JLabel l4 = new JLabel("Active");
        final JCheckBox cb = new JCheckBox();
        cb.setSelected(true);
        String formatter = "?";
        for (int i = 0; i < 254; i++)
            formatter += "*";
        final JTextField t1 = new JFormattedTextField(createFormatter(formatter));
        MaskFormatter mf = createFormatter("GMT*##");
        mf.setPlaceholder("GMT+00");
        mf.setPlaceholderCharacter('0');
        mf.setValidCharacters("+-0123456789");
        final JFormattedTextField t2 = new JFormattedTextField(mf);
        final JTextField t3 = new JTextField();
        final JPanel panel = new JPanel(new GridLayout(4, 2, 30, 30));
        panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
        panel.add(l1);
        panel.add(t1);
        panel.add(l3);
        panel.add(t3);
        String soc="";
        try {
            URL url = new URL("http://localhost:8080/Server/hello/event_port");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //String prm = "action=event_port";
            //con.setDoOutput(true);
            //DataOutputStream os = new DataOutputStream(con.getOutputStream());
            //os.writeBytes(prm);
            //os.flush();
            //os.close();
            int responseCode = con.getResponseCode();
            System.out.println(responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            Scanner sc = new Scanner(in);
            soc = sc.next();
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.print(soc);
        final StringBuilder f = new StringBuilder("0");
        JButton db = new JButton("OK");
        db.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (t1.getText().trim().length() == 0)
                    JOptionPane.showMessageDialog(jd, "Введите имя пользователя");
                else {
                    if (t3.getText().trim().length() == 0)
                        JOptionPane.showMessageDialog(jd, "Ведите пароль");
                    else
                        try {
                            if (f.toString().equals("1")) {
                                if (Sender.create(sct.getLocalPort(),t1.getText().trim(),t3.getText().trim(), t2.getText().trim())==200) {
                                    jd.dispose();
                                    final BaseForm bf = new BaseForm(sct);
                                    bf.initialize(bf,t1.getText().trim(),t3.getText().trim());
                                    bf.setVisible(true);
                                }
                                else
                                    JOptionPane.showMessageDialog(jd, "Произошла ошибка");
                            }
                            else
                            {
                                if (Sender.login(sct.getLocalPort(), t1.getText().trim(), t3.getText().trim())==200) {
                                    jd.dispose();
                                    final BaseForm bf = new BaseForm(sct);
                                    bf.initialize(bf,t1.getText().trim(),t3.getText().trim());
                                    bf.setVisible(true);
                                }
                                else
                                    JOptionPane.showMessageDialog(jd, "Произошла ошибка");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                            System.out.print(ex);
                        }
                }

            }
        });
        panel.setBorder(new EmptyBorder(20,20,20,20));
        JPanel bpanel = new JPanel(new GridLayout(1, 2, 20, 20));
        JButton db2 = new JButton("Регистрация");
        db2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.add(l2);
                panel.add(t2);
                panel.add(l4);
                panel.add(cb);
                panel.repaint();
                f.setCharAt(0,'1');
                panel.validate();
            }
        });
        JButton db3 = new JButton("Логин");
        db3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.remove(l2);
                panel.remove(t2);
                panel.remove(l4);
                panel.remove(cb);
                panel.repaint();
                f.setCharAt(0,'0');
                panel.validate();
            }
        });
        bpanel.add(db);
        bpanel.add(db2);
        bpanel.add(db3);
        bpanel.setBorder(new EmptyBorder(20,20,20,20));
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bpanel, BorderLayout.SOUTH);
    }
    public static void main(String[] args)
    {
        final ServerSocket sct;
        try {
            sct = new ServerSocket(0);
            Login lg = new Login(sct);
            lg.initialize();
            lg.setVisible(true);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
    protected MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);
        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }
}
