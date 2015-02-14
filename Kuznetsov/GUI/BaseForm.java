import javafx.scene.shape.Circle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

/**
 * Created by Pavel on 12.02.2015.
 */
public class BaseForm extends JFrame {
    private ArrayList<User> users;
    public BaseForm()
    {
        setTitle("Schedule");
        setSize(700, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        users = new ArrayList<>();
    }
    public void initialize(final JFrame jf)
    {
        JPanel buttonPanel = new JPanel(new GridLayout(0,1,30,30));
        JButton b1 = new JButton("Create user");
        buttonPanel.setBorder(new EmptyBorder(30, 10, 10, 10));
        b1.setPreferredSize(new Dimension(150, 30));
        final JTextArea tp = new JTextArea(1, 29);
        Event.area = tp;
        User.ta = tp;
        try {
            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final JDialog jd = new JDialog();
                    jd.setTitle("Create user");
                    jd.setSize(300, 200);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    JLabel l1 = new JLabel("Name");
                    JLabel l2 = new JLabel("Timezone");
                    String formatter = "?";
                    for (int i = 0; i < 254; i++)
                        formatter += "*";
                    final JTextField t1 = new JFormattedTextField(createFormatter(formatter));
                    MaskFormatter mf = createFormatter("GMT*##");
                    mf.setPlaceholder("GMT+00");
                    mf.setPlaceholderCharacter('0');
                    mf.setValidCharacters("+-0123456789");
                    final JFormattedTextField t2 = new JFormattedTextField(mf);
                    JPanel panel = new JPanel(new GridLayout(3, 2, 30, 30));
                    panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                    panel.add(l1);
                    panel.add(t1);
                    panel.add(l2);
                    panel.add(t2);
                    JButton db = new JButton("OK");
                    db.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (t1.getText().trim().length()==0)
                                tp.append("Введите имя пользователя\n");
                            else {
                                boolean f = false;
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
                                    users.add(new User(t1.getText().trim(), TimeZone.getTimeZone(t2.getText().trim()), true));
                                    tp.append("Пользователь успешно создан\n");
                                    jd.dispose();
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
            JButton b2 = new JButton("Add event");
            b2.setPreferredSize(new Dimension(150, 30));
            try{
                b2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final JDialog jd = new JDialog();
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
                                if (t3.getText().trim().length()==0)
                                    tp.append("Введите имя пользователя\n");
                                else {
                                    if (t1.getText().trim().length()==0)
                                        tp.append("Введите текст сообщения\n");
                                    else {
                                        boolean f = false;
                                        boolean f2 = false;
                                        for (User u: users)
                                        {
                                            if (u.getName().equals(t3.getText().trim()))
                                            {
                                                String msg = t1.getText().trim();
                                                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                                try {
                                                    Date d = df.parse(t2.getText().trim());
                                                    u.AddEvent(d, msg);
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
                                                jd.dispose();
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
            }catch (Exception e)
            {
                tp.append("Невозможно создать событие\n");
            }
            JButton b3 = new JButton("Show info");
            b3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final JDialog jd = new JDialog();
                    jd.setTitle("User's info");
                    jd.setSize(450, 400);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    final JPanel jp = new JPanel(new GridLayout(0,3,20,20));
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
                            for (User u: users)
                            {
                                boolean f = false;
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
                                    //jd.getContentPane().add(jp,BorderLayout.NORTH);
                                    jd.repaint();
                                    jd.setVisible(true);
                                }
                                if (!f)
                                    JOptionPane.showMessageDialog(jd,"Такого пользователя не существует");
                            }
                        }
                    });


                    jd.getContentPane().add(jp,BorderLayout.NORTH);
                    jd.setVisible(true);
                }
            });
            buttonPanel.add(b1);
            buttonPanel.add(b2);
            buttonPanel.add(b3);
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
            left.add(sp,BorderLayout.EAST);
            getContentPane().add(left);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }catch (Exception e)
        {
            tp.append("Вы указали неверный входной параметр\n");
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
