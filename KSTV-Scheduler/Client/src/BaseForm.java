//import javafx.scene.shape.Circle;
//import sun.jdbc.odbc.JdbcOdbcDriver;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import java.net.Socket;
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
    public void initialize(final JFrame jf, String log, String pass) {
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
                                            if (Sender.addEvent(t1.getText().trim(),t2.getText().trim())==200) {
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
                                jd.add(Sender.showInfo(tp, login));
                                jd.repaint();
                                jd.setVisible(true);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(jd, "Ошибка соединения");
                            }
                        }
                    });


                    jd.getContentPane().add(jp, BorderLayout.NORTH);
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
                    jd.setSize(300, 250);
                    jd.setResizable(false);
                    jd.setLocationRelativeTo(jf);
                    jd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    JLabel l1 = new JLabel("Name");
                    final JLabel l2 = new JLabel("Timezone");
                    JLabel l3 = new JLabel("Password");
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
                    final JCheckBox t4 = new JCheckBox();
                    final JPanel panel = new JPanel(new GridLayout(3, 2, 30, 30));
                    panel.setPreferredSize(new Dimension(jd.getWidth() / 2, jd.getHeight()));
                    panel.add(l1);
                    panel.add(t1);
                    panel.add(l3);
                    panel.add(t3);
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
                                        if (Sender.modify(t1.getText().trim(), t2.getText().trim(), t4.isSelected())==200) {
                                            tp.append("Данные изменены\n");
                                            jd.dispose();
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(jd, "Ошибка соединения");
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
                        Sender.stop();
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
