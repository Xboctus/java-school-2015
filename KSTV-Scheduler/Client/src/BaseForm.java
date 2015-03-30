//import javafx.scene.shape.Circle;
//import sun.jdbc.odbc.JdbcOdbcDriver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.ExpandVetoException;

/**
 * Created by Pavel on 12.02.2015.
 */
public class BaseForm extends JFrame {
    private Socket sct;
    private String login;
    private String password;
    public static JTextArea tp = new JTextArea(1, 29);
    public BaseForm(Socket sct)
    {
        setTitle("Schedule");
        setSize(700, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        this.sct = sct;
    }
    public void initialize(final JFrame jf, String log, String pass, final StringBuilder cookie) {
        /*Timer tim = new Timer(3500000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PrintWriter w = new PrintWriter(sct.getOutputStream());
                    String[] mas = new String[1];
                    mas[0] = "prolongate";
                    Shared.putParts(mas, w);
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            }
        });
        tim.start();*/
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 30, 30));
        JButton b1 = new JButton("Create user");
        buttonPanel.setBorder(new EmptyBorder(30, 10, 10, 10));
        b1.setPreferredSize(new Dimension(150, 30));
        //final JTextArea tp = new JTextArea(1, 29);
        Event.area = tp;
        User.ta = tp;
        login = log; password = pass;
        /*Thread trd = new Thread() {
            @Override
            public void run() {
                Listener.listen(sct, tp);
            }
        };
        trd.start();*/
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    URL url = new URL("http://localhost:8080/Server/hello/logout");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
                    con.setRequestProperty("Cookie", cookie.toString());
                    int responseCode = con.getResponseCode();
                    System.out.println(responseCode);
                }catch (Exception ex)
                {
                    System.out.println(ex.getMessage());
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        try {
            JButton b2 = new JButton("Add event");
            b2.setPreferredSize(new Dimension(150, 30));
            try {
                b2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final JDialog jd = new JDialog();
                        jd.setModal(true);
                        jd.setTitle("Add event");
                        jd.setSize(400, 300);
                        jd.setResizable(false);
                        jd.setLocationRelativeTo(jf);
                        jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        JLabel l1 = new JLabel("Text");
                        JLabel l2 = new JLabel("Time");
                        JPanel panel = new JPanel(new GridLayout(4, 2, 30, 30));
                        MaskFormatter mf = createFormatter("##.##.####-##:##:##");
                        mf.setPlaceholderCharacter('0');
                        final JTextField t1 = new JTextField();
                        final JTextField t2 = new JFormattedTextField(mf);
                        panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                        panel.add(l1);
                        panel.add(t1);
                        panel.add(l2);
                        panel.add(t2);
                        JButton db = new JButton("OK");
                        db.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                    if (t1.getText().trim().length() == 0)
                                        tp.append("Введите текст сообщения\n");
                                    else {
                                        try {
                                            if (Sender.addEvent(t1.getText().trim(),t2.getText().trim())==200) {
                                                tp.append("Событие успешно создано\n");
                                                jd.dispose();
                                            }
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                                        }
                                    }
                            }
                        });
                        panel.add(db);
                        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                        jd.getContentPane().add(panel);
                        jd.setVisible(true);
                    }
                });
            } catch (Exception e) {
                tp.append("Невозможно создать событие\n");
            }
            JButton b3 = new JButton("Show info");
            b3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final JDialog jd = new JDialog();
                    jd.setModal(true);
                    jd.setTitle("User's info");
                    jd.setSize(450, 400);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    final JPanel jp = new JPanel(new GridLayout(0, 3, 20, 20));
                    JLabel l1 = new JLabel("User");
                    JLabel l2 = new JLabel(login);
                    jp.add(l1);
                    jp.add(l2);
                    jp.setBorder(new EmptyBorder(10, 10, 10, 10));
                    jd.getContentPane().add(jp, BorderLayout.NORTH);
                    try {
                        JTable table = Sender.showInfo(tp, cookie);
                        jd.add(new JScrollPane(table));
                    }catch (Exception ex) {
                        JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                    }
                    jd.setVisible(true);
                }
            });
            JButton b5 = new JButton("Send");
            b5.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        //Sender.message(sct.getLocalPort());
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            });
            JButton b6 = new JButton("Modify");
            b6.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final JDialog jd = new JDialog();
                    jd.setTitle("Login");
                    jd.setSize(400, 350);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    JLabel l1 = new JLabel("Name");
                    final JLabel l2 = new JLabel("Timezone");
                    JLabel l3 = new JLabel("Old password");
                    JLabel l5 = new JLabel("New password");
                    JLabel l4 = new JLabel("Active");
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
                    final JTextField t5 = new JTextField();
                    final JCheckBox t4 = new JCheckBox();
                    final JPanel panel = new JPanel(new GridLayout(5, 0, 30, 30));
                    panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                    //panel.add(l1);
                    //panel.add(t1);
                    panel.add(l2);
                    panel.add(t2);
                    panel.add(l4);
                    panel.add(t4);
                    panel.add(l3);
                    panel.add(t3);
                    panel.add(l5);
                    panel.add(t5);
                    try {
                        URL url = new URL("http://localhost:8080/Server/hello/users/:me");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setRequestProperty("Cookie", cookie.toString());
                        int responseCode = con.getResponseCode();
                        System.out.println(responseCode);
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        Shared.GetPartsResult pr = Shared.getParts(in);
                        t1.setText(login);
                        t2.setText(pr.parts[0]);
                        t4.setSelected((pr.parts[1].equals("true"))?true:false);
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                    }
                    final String oldLogin = t1.getText();
                    final String oldGMT = t2.getText();
                    final boolean oldActive = t4.isSelected();
                    final StringBuilder f = new StringBuilder("0");
                    JButton db = new JButton("OK");
                    db.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            /*if (t1.getText().trim().length() == 0)
                                JOptionPane.showMessageDialog(jd, "Введите имя пользователя");
                            else {
                                if (t3.getText().trim().length() == 0)
                                    JOptionPane.showMessageDialog(jd, "Ведите пароль");
                                else
                                    try {
                                        if (Sender.modify(t1.getText().trim(), t2.getText().trim(), t4.isSelected())==200) {
                                            tp.append("Данные изменены\n");
                                            jd.dispose();
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                                    }
                            }*/
                            if (((t3.getText().trim().length()==0)&&(t5.getText().trim().length()!=0))||
                                    ((t3.getText().trim().length()!=0)&&(t5.getText().trim().length()==0)))
                            {
                                JOptionPane.showMessageDialog(jd, "Пустой пароль недопустим");
                            }
                            else {
                                //ArrayList<String> mas = new ArrayList<String>();
                                String prm = "";
                                if (!t2.getText().trim().equals(oldGMT)) {
                                    prm += "new_timezone=" + t2.getText().trim()+"\u001F";
                                }
                                if (t4.isSelected() ^ oldActive) {
                                    prm += "new_active=" + (t4.isSelected() ? "true" : "false")+"\u001F";
                                }
                                if (t5.getText().trim().length()!=0)
                                {
                                    prm += ("old_password=" + t3.getText().trim()+"\u001F");
                                    prm += ("new_password=" + t5.getText().trim()+"\u001F");
                                }
                                try {
                                    URL url = new URL("http://localhost:8080/Server/hello/users/:me");
                                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                    con.setRequestMethod("PUT");
                                    con.setRequestProperty("Cookie", cookie.toString());
                                    con.setDoOutput(true);
                                    DataOutputStream os = new DataOutputStream(con.getOutputStream());
                                    os.writeBytes(prm+"\u001E");
                                    os.flush();
                                    os.close();
                                    int responseCode = con.getResponseCode();
                                    if (responseCode == 200) {
                                        JOptionPane.showMessageDialog(jd, "Данные успешно изменены");
                                        jd.dispose();
                                    }
                                    else
                                        JOptionPane.showMessageDialog(jd, "Неверен текущий пароль или временная зона");
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(jd, "Ошибка ввода");
                                }
                            }
                        }
                    });
                    panel.add(db);
                    panel.setBorder(new EmptyBorder(20,20,20,20));
                    jd.getContentPane().add(panel, BorderLayout.CENTER);
                    jd.setVisible(true);
                }
            });
            JButton b7 = new JButton("Add random event");
            b7.setPreferredSize(new Dimension(150, 30));
            try {
                b7.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final JDialog jd = new JDialog();
                        jd.setModal(true);
                        jd.setTitle("Add event");
                        jd.setSize(400, 300);
                        jd.setResizable(false);
                        jd.setLocationRelativeTo(jf);
                        jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        JLabel l1 = new JLabel("Text");
                        JLabel l2 = new JLabel("Time1");
                        JLabel l4 = new JLabel("Time2");
                        JLabel l3 = new JLabel("User");
                        JPanel panel = new JPanel(new GridLayout(5, 2, 30, 30));
                        MaskFormatter mf = createFormatter("##.##.####-##:##:##");
                        mf.setPlaceholderCharacter('0');
                        final JTextField t1 = new JTextField();
                        final JTextField t2 = new JFormattedTextField(mf);
                        final JTextField t3 = new JTextField();
                        final JTextField t4 = new JFormattedTextField(mf);
                        panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                        panel.add(l1);
                        panel.add(t1);
                        panel.add(l2);
                        panel.add(t2);
                        panel.add(l4);
                        panel.add(t4);
                        panel.add(l3);
                        panel.add(t3);
                        JButton db = new JButton("OK");
                        db.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (t3.getText().trim().length() == 0)
                                    tp.append("Введите имя пользователя\n");
                                else {
                                    if (t1.getText().trim().length() == 0)
                                        tp.append("Введите текст сообщения\n");
                                    else {
                                        try {
                                            if (Sender.addRanEvent(t1.getText().trim(), t2.getText().trim(), t4.getText().trim())==200) {
                                                tp.append("Событие успешно создано\n");
                                                jd.dispose();
                                            }
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                                        }
                                    }
                                }
                            }
                        });
                        panel.add(db);
                        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                        jd.getContentPane().add(panel);
                        jd.setVisible(true);
                    }
                });
            } catch (Exception e) {
                tp.append("Невозможно создать событие\n");
            }
            JButton b8 = new JButton("Exit");
            b8.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try
                    {
                        PrintWriter w = new PrintWriter(sct.getOutputStream());
                        String[] mas = new String[1];
                        mas[0]="logout";
                        Shared.putParts(mas, w);

                        URL url = new URL("http://localhost:8080/Server/hello/logout");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("PUT");
                        con.setRequestProperty("Cookie", cookie.toString());
                        int responseCode = con.getResponseCode();
                        System.out.println(responseCode);

                        dispose();
                        Login lg = new Login();
                        lg.initialize();
                        lg.setVisible(true);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(ex);
                    }
                }
            });
            buttonPanel.add(b2);
            buttonPanel.add(b7);
            buttonPanel.add(b3);
            buttonPanel.add(b6);
            buttonPanel.add(b8);
            JPanel east = new JPanel(new GridBagLayout());
            east.setBorder(BorderFactory.createLineBorder(Color.black));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.weighty = 1;
            east.setPreferredSize(new Dimension(this.getWidth() / 2, this.getHeight()));
            east.add(buttonPanel, gbc);
            getContentPane().add(east, BorderLayout.EAST);
            JPanel left = new JPanel(new BorderLayout(0, 0));
            tp.setLineWrap(true);
            tp.setEditable(false);
            JScrollPane sp = new JScrollPane(tp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            left.setSize(new Dimension(this.getWidth() / 2, this.getHeight()));
            left.add(sp, BorderLayout.EAST);
            getContentPane().add(left);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            tp.append("Вы указали неверный входной параметр\n");
        }
    }
    /*public static void main(String[] args)
    {
        final BaseForm bf = new BaseForm();
        bf.initialize(bf);
        bf.setVisible(true);
    }*/
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
