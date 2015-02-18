import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by ImmortalWolf on 12.02.2015.
 */
public class StartForm extends JFrame {
    public static void main(String[] args) {
        StartForm form1 = new StartForm();
        form1.initialize();
        form1.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public StartForm() {
        setTitle("Calendar");
        //setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void initialize() {
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        //jPanel1.setPreferredSize(new Dimension(this.getWidth()/2, this.getHeight()));
        panel2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        //GridBagConstraints gbc = new GridBagConstraints();
        //jPanel1.setLayout(new BorderLayout());
        //jPanel1.add(new JButton("closed"), BorderLayout.EAST);
        getContentPane().add(panel1);
        getContentPane().add(panel2);

        final ArrayList<User> users = new ArrayList<User>();

        JButton button1 = new JButton("Create User");
        panel2.add(button1);

        //panel2.add(text1);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final StartForm form2 = new StartForm();
                form2.setTitle("Create User");
                form2.setSize(300, 180);
                form2.setLocationRelativeTo(null);
                JPanel pUser = new JPanel();
                pUser.setBorder(new EmptyBorder(10, 10, 10, 10));

                form2.getContentPane().add(pUser);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipadx = 20;
//                pUser.setLayout(new GridLayout(2, 2, 30, 30));
                pUser.setLayout(new GridBagLayout());
                JLabel label1 = new JLabel("User");
                JLabel label2 = new JLabel("Timezone");

                MaskFormatter mformat = null;
                try {
                    mformat = new MaskFormatter("?**************************************************************************************************************************************************************************************************************************************************************");
                }
                catch (Exception ex)
                { }

                final JTextField name = new JFormattedTextField(mformat);
                name.setColumns(10);

                try {
                    mformat = new MaskFormatter("GMT*##");
                }
                catch (Exception ex)
                { }

                mformat.setPlaceholder("GMT+00");
                mformat.setValidCharacters("-+0123456789");
                final JTextField timezone = new JFormattedTextField(mformat);
                timezone.setColumns(10);
                gbc.gridx = 0;
                gbc.gridy = 0;
                pUser.add(label1, gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                pUser.add(name, gbc);
                gbc.insets = new Insets(10, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 1;
                pUser.add(label2, gbc);
                gbc.gridx = 1;
                gbc.gridy = 1;
                pUser.add(timezone, gbc);
                form2.setVisible(true);
                //form2.getContentPane().add(text1);
                //form2.getContentPane().add(text2);
                //text1.setText(e.getActionCommand());
                JButton butOK = new JButton("OK");
                //gbc.ipady = 30;
                gbc.insets = new Insets(20, 0, -10, 0);
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;
                pUser.add(butOK, gbc);

                butOK.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        users.add(new User(name.getText(), TimeZone.getTimeZone(timezone.getText()), true));
                        form2.dispose();
                        //users.add(new User("name", TimeZone.getTimeZone("Europe/Moscow"), true));
                    }
                });
            }
        });

        JButton button2 = new JButton("Add Event");
        panel2.add(button2);

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final StartForm form2 = new StartForm();
                form2.setTitle("Add Event");
                form2.setSize(300, 200);
                form2.setLocationRelativeTo(null);
                JPanel pUser = new JPanel();
                pUser.setBorder(new EmptyBorder(10, 10, 10, 10));

                form2.getContentPane().add(pUser);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipadx = 20;
//                pUser.setLayout(new GridLayout(2, 2, 30, 30));
                pUser.setLayout(new GridBagLayout());
                JLabel label1 = new JLabel("User");
                JLabel label2 = new JLabel("Text");
                JLabel label3 = new JLabel("Time");

                MaskFormatter mformat = null;
                try {
                    mformat = new MaskFormatter("?**************************************************************************************************************************************************************************************************************************************************************");
                }
                catch (Exception ex)
                { }

                final JTextField name = new JFormattedTextField(mformat);
                name.setColumns(10);

                final JTextField text = new JTextField();
                text.setColumns(10);

                final JTextField datetime = new JTextField();
                text.setColumns(10);

                gbc.gridx = 0;
                gbc.gridy = 0;
                pUser.add(label1, gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                pUser.add(name, gbc);
                gbc.insets = new Insets(10, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 1;
                pUser.add(label2, gbc);
                gbc.gridx = 1;
                gbc.gridy = 1;
                pUser.add(text, gbc);
                //gbc.insets = new Insets(10, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 2;
                pUser.add(label3, gbc);
                gbc.gridx = 1;
                gbc.gridy = 2;
                pUser.add(datetime, gbc);

                JButton butOK = new JButton("OK");
                gbc.insets = new Insets(20, 0, -10, 0);
                gbc.gridx = 0;
                gbc.gridy = 3;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;
                pUser.add(butOK, gbc);
                form2.setVisible(true);

                butOK.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            for (int i = 0; i < users.size(); i++)
                                if (name.getText().equals(users.get(i).getName()))
                                {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                    formatter.setTimeZone(users.get(i).getTimezone());
                                    Date date = formatter.parse(datetime.getText());
                                    users.get(i).AddEvent(text.getText(), date);
                                    break;
                                }
                        } catch (ParseException e2) {
                            System.out.println(e2.getMessage());
                        }
                        form2.dispose();
                    }
                });
            }
        });

        JButton button3 = new JButton("Add Random Time Event");
        panel2.add(button3);

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final StartForm form2 = new StartForm();
                form2.setTitle("Add Event");
                form2.setSize(330, 240);
                form2.setLocationRelativeTo(null);
                JPanel pUser = new JPanel();
                pUser.setBorder(new EmptyBorder(10, 10, 10, 10));

                form2.getContentPane().add(pUser);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipadx = 20;
//                pUser.setLayout(new GridLayout(2, 2, 30, 30));
                pUser.setLayout(new GridBagLayout());
                JLabel label1 = new JLabel("User");
                JLabel label2 = new JLabel("Text");
                JLabel label3 = new JLabel("dateFrom");
                JLabel label4 = new JLabel("dateTo");

                MaskFormatter mformat = null;
                try {
                    mformat = new MaskFormatter("?**************************************************************************************************************************************************************************************************************************************************************");
                }
                catch (Exception ex)
                { }

                final JTextField name = new JFormattedTextField(mformat);
                name.setColumns(10);

                final JTextField text = new JTextField();
                text.setColumns(10);

                final JTextField dateFrom = new JTextField();
                text.setColumns(10);

                final JTextField dateTo = new JTextField();
                text.setColumns(10);

                gbc.gridx = 0;
                gbc.gridy = 0;
                pUser.add(label1, gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                pUser.add(name, gbc);
                gbc.insets = new Insets(10, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 1;
                pUser.add(label2, gbc);
                gbc.gridx = 1;
                gbc.gridy = 1;
                pUser.add(text, gbc);
                //gbc.insets = new Insets(10, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 2;
                pUser.add(label3, gbc);
                gbc.gridx = 1;
                gbc.gridy = 2;
                pUser.add(dateFrom, gbc);
                gbc.gridx = 0;
                gbc.gridy = 3;
                pUser.add(label4, gbc);
                gbc.gridx = 1;
                gbc.gridy = 3;
                pUser.add(dateTo, gbc);

                JButton butOK = new JButton("OK");
                gbc.insets = new Insets(20, 0, -10, 0);
                gbc.gridx = 0;
                gbc.gridy = 4;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;
                pUser.add(butOK, gbc);
                form2.setVisible(true);

                butOK.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try
                        {
                            for (int i = 0; i < users.size(); i++)
                                if (name.getText().equals(users.get(i).getName()))
                                {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                                    formatter.setTimeZone(users.get(i).getTimezone());
                                    Date date1 = formatter.parse(dateFrom.getText());
                                    Date date2 = formatter.parse(dateTo.getText());
                                    Date date3 = new Date (((long) ((date2.getTime() - date1.getTime())*Math.random())) + date1.getTime());
                                    users.get(i).AddEvent(text.getText(), date3);
                                    break;
                                }
                        }
                        catch (ParseException e3) {
                            System.out.println(e3.getMessage());
                        }
                        form2.dispose();
                    }
                });
            }
        });

        JButton button4 = new JButton("Show Info");
        panel2.add(button4);

        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final StartForm form2 = new StartForm();
                form2.setTitle("Create User");
                form2.setSize(400, 500);
                form2.setLocationRelativeTo(null);
                final JPanel pUser = new JPanel(new GridLayout(2,3,20,20));
                pUser.setBorder(new EmptyBorder(10, 10, 10, 10));

                form2.getContentPane().add(pUser);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.ipadx = 20;
//                pUser.setLayout(new GridLayout(2, 2, 30, 30));
                //pUser.setLayout(new GridBagLayout());

                //pUser.setLayout(new GridLayout(1, 2));
//                pUser.setLayout(new GridLayout(2, 2, 30, 30));
                pUser.setLayout(new GridBagLayout());
                JLabel label1 = new JLabel("User");

                MaskFormatter mformat = null;
                try {
                    mformat = new MaskFormatter("?**************************************************************************************************************************************************************************************************************************************************************");
                }
                catch (Exception ex)
                { }

                final JTextField name = new JFormattedTextField(mformat);
                name.setColumns(10);

                gbc.gridx = 0;
                gbc.gridy = 0;
                pUser.add(label1, gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                pUser.add(name, gbc);

                JButton butOK = new JButton("OK");
                gbc.insets = new Insets(15, 0, -10, 0);
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.NONE;

                pUser.add(butOK, gbc);
                JTable table = new JTable();
                form2.add(table);
                //form2.validate();
                form2.setVisible(true);

                butOK.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object[] columnNames = {"Text", "Date"};
                        ArrayList<Event> temp = new ArrayList<Event>();

                        for (int i = 0; i < users.size(); i++)
                            if (name.getText().equals(users.get(i).getName())) {
                                temp = users.get(i).getEvent();
                                break;
                            }

                        Object[][] data = new Object[temp.size()][2];
                        //Object[][] data = {{"Hello", "12.02.2015-12:27:15"}};

                        for (int i = 0; i < temp.size(); i++)
                        {
                            data[i][0] = temp.get(i).getText();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss");
                            formatter.setTimeZone(TimeZone.getTimeZone("GMT+3"));
                            //System.out.println(formatter.format(temp.get(i).getDate())
                            data[i][1] = formatter.format(temp.get(i).getDate());
                        }
                        JTable table = new JTable(data, columnNames);
                        form2.add(table);
                        form2.validate();


                        //pUser.add(table, gbc);
                        //table.setVisible(true);
                        //gbc.insets = new Insets(10, 0, 0, 0);
                        /*for (int i = 0; i < users.size(); i++)
                        {
                            if (name.equals(users.get(i).getName()))
                            {
                                ArrayList<Event> temp = users.get(i).event;
                                    break;
                            }
                        }*/
                        //Event temp = users.get(fromUser).getEvent(text);
                        //users.get(toUser).AddEvent(temp.getText(), temp.getDate());
                    }
                });
                form2.getContentPane().add(pUser, BorderLayout.NORTH);
            }
        });

        setVisible(true);
    }
}