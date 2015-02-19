import javafx.scene.shape.Circle;
//import sun.jdbc.odbc.JdbcOdbcDriver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.tree.ExpandVetoException;

/**
 * Created by Pavel on 12.02.2015.
 */
public class BaseForm extends JFrame {
    //private ArrayList<User> users;
    public BaseForm()
    {
        setTitle("Schedule");
        setSize(700, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        /*users = new ArrayList<>();
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection con = DriverManager.getConnection("jdbc:odbc:Schedule");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users");
            while (rs.next())
            {
                int id = rs.getInt("userID");
                users.add(new User(rs.getString("name"), rs.getString("pass"), TimeZone.getTimeZone(rs.getString("timezone")),rs.getBoolean("active"),false));
                String sql = "select * from Evnts where userID = ?";
                Connection con2 = DriverManager.getConnection("jdbc:odbc:Schedule");
                PreparedStatement psmnt = con2.prepareStatement(sql);
                psmnt.setInt(1, id);
                ResultSet rs1 = psmnt.executeQuery();
                while (rs1.next())
                {
                    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                    User us = users.get(users.size()-1);
                    us.AddEvent(df.parse(rs1.getString("dtime")), rs1.getString("msg"), false);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }*/
    }
    public void initialize(final JFrame jf) {
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 30, 30));
        JButton b1 = new JButton("Create user");
        buttonPanel.setBorder(new EmptyBorder(30, 10, 10, 10));
        b1.setPreferredSize(new Dimension(150, 30));
        final JTextArea tp = new JTextArea(1, 29);
        Event.area = tp;
        User.ta = tp;
        final ServerSocket sct;
        try {
            sct = new ServerSocket(0);
            Thread trd = new Thread(){
                @Override
                public void run(){
                    Listener.listen(sct, tp);
                }
            };
            trd.start();

        try {
            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final JDialog jd = new JDialog();
                    jd.setModal(true);
                    jd.setTitle("Create user");
                    jd.setSize(300, 250);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    JLabel l1 = new JLabel("Name");
                    JLabel l2 = new JLabel("Timezone");
                    JLabel l3 = new JLabel("Password");
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
                    JPanel panel = new JPanel(new GridLayout(4, 2, 30, 30));
                    panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                    panel.add(l1);
                    panel.add(t1);
                    panel.add(l2);
                    panel.add(t2);
                    panel.add(l3);
                    panel.add(t3);
                    JButton db = new JButton("OK");
                    db.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (t1.getText().trim().length() == 0)
                                tp.append("Введите имя пользователя\n");
                            else {
                                if (t3.getText().trim().length() == 0)
                                    tp.append("Введите пароль\n");
                                else
                                    try {
                                        if (Sender.create()) {
                                            jd.dispose();
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                                    }
                                /*boolean f = false;
                                for (User u : users)
                                {
                                    if (u.getName().equals(t1.getText().trim()))
                                    {
                                        tp.append("Пользователь с таким именем уже существует\n");
                                        f = !f;
                                        break;
                                    }
                                }
                                if (!f) {
                                    users.add(new User(t1.getText().trim(), t3.getText().trim(), TimeZone.getTimeZone(t2.getText().trim()), true, true));
                                    tp.append("Пользователь успешно создан\n");
                                    jd.dispose();
                                }*/
                            }
                        }
                    });
                    panel.add(db);
                    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    jd.getContentPane().add(panel);
                    jd.setVisible(true);
                }
            });
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
                        JLabel l3 = new JLabel("User");
                        JPanel panel = new JPanel(new GridLayout(4, 2, 30, 30));
                        MaskFormatter mf = createFormatter("##.##.####-##:##:##");
                        mf.setPlaceholderCharacter('0');
                        final JTextField t1 = new JTextField();
                        final JTextField t2 = new JFormattedTextField(mf);
                        final JTextField t3 = new JTextField();
                        panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                        panel.add(l1);
                        panel.add(t1);
                        panel.add(l2);
                        panel.add(t2);
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
                                            if (Sender.addEvent()) {
                                                jd.dispose();
                                            }
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                                        }
                                        /*boolean f = false;
                                        boolean f2 = false;
                                        for (User u: users)
                                        {
                                            if (u.getName().equals(t3.getText().trim()))
                                            {
                                                String msg = t1.getText().trim();
                                                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                                try {
                                                    Date d = df.parse(t2.getText().trim());
                                                    u.AddEvent(d, msg, true);
                                                    f = !f;
                                                } catch (ParseException | IllegalArgumentException ex)
                                                {
                                                    tp.append("Вы ввели некорректную дату\n");
                                                    f2 = !f2;
                                                }
                                                break;
                                            }
                                        }
                                        if (!f2)
                                            if (!f) tp.append("Вы ввели некорректное имя\n");
                                            else
                                                jd.dispose();*/
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
                    JButton jb = new JButton("OK");
                    final JTextField t1 = new JTextField();
                    jp.add(l1);
                    jp.add(t1);
                    jp.add(jb);
                    jp.setBorder(new EmptyBorder(10, 10, 10, 10));
                    final JLabel la = new JLabel("");
                    final JLabel lt = new JLabel("");
                    jp.add(la);
                    jp.add(lt);
                    jb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Sender.showInfo();
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                            }
                            /*boolean f = false;
                            for (User u: users)
                            {

                                if (u.getName().trim().equals(t1.getText()))
                                {
                                    f = !f;
                                    lt.setText(u.getTimezone().getID());
                                    if (u.isActive())
                                        la.setText("Active");
                                    else
                                        la.setText("Passive");
                                    Object[] cn = {"Time", "Text"};
                                    Object[][] cs = new Object[u.getEvents().size()][2];
                                    int i = 0;
                                    for (Event ev : u.getEvents())
                                    {
                                        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                        cs[i][0] = df.format(ev.getDate().getTime()-3600000);
                                        cs[i][1] = ev.getText();
                                        ++i;
                                    }
                                    jd.add(new JTable(cs,cn));
                                    jd.repaint();
                                    jd.setVisible(true);
                                }
                            }
                            if (!f)
                                JOptionPane.showMessageDialog(jd,"Такого пользователя не существует");*/
                        }
                    });


                    jd.getContentPane().add(jp, BorderLayout.NORTH);
                    jd.setVisible(true);
                }
            });
            /*JButton b4 = new JButton("Save");
            b4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        Connection con = DriverManager.getConnection("jdbc:odbc:Schedule");
                        for (User u: users) {
                            if (u.isNew()) {
                                String q = "insert Users(name, pass, timezone, active) values (N'"+u.getName()+"', N'"+u.getPass()+"', N'"+u.getTimezone().getID().substring(0,6)+"', ";
                                if (u.isActive())
                                    q += "1)";
                                else
                                    q += "0)";
                                Statement stmt = con.createStatement();
                                stmt.executeUpdate(q);
                                con.commit();
                                u.setNonNew();
                            }
                            for (Event evnt: u.getEvents())
                            {
                                if (evnt.isNeu())
                                {
                                    String q1 = "select userID from Users where name like '" + u.getName()+"'";
                                    Statement stmt1 = con.createStatement();
                                    ResultSet rs = stmt1.executeQuery(q1);
                                    rs.next();
                                    int id = rs.getInt("userID");
                                    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                    String q = "insert Evnts(dtime, msg, userID) values (N'"+df.format(evnt.getDate())+"',N'"+evnt.getText()+"',"+id+")";
                                    Statement stmt2 = con.createStatement();
                                    stmt2.executeUpdate(q);
                                    evnt.setNonNew();
                                    con.commit();
                                }
                            }
                        }
                        tp.append("Изменений сохранены\n");
                    } catch (Exception ex)
                    {
                        System.out.println(ex.getMessage());
                    }
                }
            });*/
            JButton b5 = new JButton("Send");
            b5.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Sender.message(sct.getLocalPort());
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            });
            buttonPanel.add(b1);
            buttonPanel.add(b2);
            buttonPanel.add(b3);
            //buttonPanel.add(b4);
            buttonPanel.add(b5);
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
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }
    public static void main(String[] args)
    {
        final BaseForm bf = new BaseForm();
        bf.initialize(bf);
        bf.setVisible(true);
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
