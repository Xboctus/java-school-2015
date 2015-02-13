import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.text.DefaultCaret;

class LogHolder {
	private JTextArea logArea;
	private static SimpleDateFormat timeFormat =
			new SimpleDateFormat("dd.MM-HH:mm:ss");
	
	LogHolder(JTextArea la) {
		logArea = la;
	}
	
	public void print(String text) {
		logArea.append(timeFormat.format(new Date())+": "+text+"\n");
	}
}

class MainWindow extends JFrame {
	private JButton btUseradd, btUseredit, btEventadd, btEventrem,
						btEventrnd, btUserinfo, btEventcopy;
	private JTextArea logArea = new JTextArea();
	private DefaultListModel<String> lModel = new DefaultListModel<String>();
	private JList<String> lUsers = new JList<String>(lModel);
	
	private void initComponents() {
		JLabel lbLog = new JLabel("Scheduler log"),
				lbUsers = new JLabel("Users");
		btUseradd = new JButton("Create user");
		btUseredit = new JButton("Modify user");
		btEventadd = new JButton("Add event");
		btEventrem = new JButton("Remove event");
		btEventrnd = new JButton("Add random event");
		btUserinfo = new JButton("Show user info");
		btEventcopy = new JButton("Clone event");
		JPanel logPanel = new JPanel(),
				actionsPanel = new JPanel(new FlowLayout());
		logPanel.setLayout(new BoxLayout(logPanel,BoxLayout.PAGE_AXIS));
		logArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(logArea);
		logPanel.add(lbLog);
		logPanel.add(scrollPane);
		logPanel.setPreferredSize(new Dimension((int)(getWidth()/2.2), getHeight()-40));
		int actionsPanelWidth = getWidth()-logPanel.getPreferredSize().width-20;
		lUsers.setPreferredSize(new Dimension(actionsPanelWidth-10, getHeight()/4));
		actionsPanel.setPreferredSize(new Dimension(actionsPanelWidth, getHeight()-40));
		actionsPanel.add(lbUsers);
		actionsPanel.add(lUsers);
		actionsPanel.add(btUseradd);
		actionsPanel.add(btUseredit);
		actionsPanel.add(btEventadd);
		actionsPanel.add(btEventrem);
		actionsPanel.add(btEventrnd);
		actionsPanel.add(btUserinfo);
		actionsPanel.add(btEventcopy);
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(logPanel);
		getContentPane().add(actionsPanel);
		addButtonListener(this,btUseradd,CmdType.ADDUSER);
		addButtonListener(this,btUseredit,CmdType.EDITUSER);
		addButtonListener(this,btEventadd,CmdType.ADDEVENT);
		addButtonListener(this,btEventrem,CmdType.REMOVEEVENT);
		addButtonListener(this,btEventrnd,CmdType.ADDRANDOMEVENT);
		addButtonListener(this,btUserinfo,CmdType.USERINFO);
		addButtonListener(this,btEventcopy,CmdType.CLONEEVENT);
	}
	
	private void addButtonListener(final MainWindow mainWindow, JButton bt, final CmdType cmd) {
		bt.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Rectangle pos = getBounds();
				(new CommandWindow(mainWindow, cmd, lUsers.getSelectedValue(),
						pos.x, pos.y)).setVisible(true);
			}
		});
	}
	
	private void updateUsers() {
		lModel.clear();
		ArrayList<SchUser> users = SScheduler.getUsers();
		for (SchUser u : users)
			lModel.addElement(u.getName());
	}
	
	MainWindow() {
		super("GSScheduler");
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int w = screen.width, h = screen.height;
		setBounds(w/4, h/4, w/2, h/2);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initComponents();
		SchEvent.setLogHolder(new LogHolder(logArea));
	}
	
	public void perform(String cmdStr) {
		SScheduler.parseCmd(cmdStr);
		updateUsers();
	}
}

enum CmdType {
	ADDUSER,
	EDITUSER,
	ADDEVENT,
	REMOVEEVENT,
	ADDRANDOMEVENT,
	CLONEEVENT,
	USERINFO
}

class CommandWindow extends JDialog {
	
	private MainWindow mainWindow;
	private JButton btSubmit = new JButton("Submit");
	private ArrayList<Component> fields =
			new ArrayList<Component>();
	
	private Component[] getCmdDialogConfig(CmdType cmd, final String username) {
		Component[] params = null;
		final JDialog _this = this;
		switch(cmd) {
		case ADDUSER:
			params = new Component[]{
					new JLabel("Create user"),
					new JLabel("Name"),
					new JLabel("Timezone"),
					new JCheckBox("Active")
					};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "Create "+((JTextField)fields.get(0)).getText()+" "+
							((JTextField)fields.get(1)).getText()+" "+
							(((JCheckBox)fields.get(2)).isSelected() ? "active" : "disabled");
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case EDITUSER:
			if (username==null) return null;
			params = new Component[]{
					new JLabel("Change settings for "+username),
					new JLabel("Timezone"),
					new JCheckBox("Active")
			};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "Modify "+username+" "+
							((JTextField)fields.get(0)).getText()+" "+
							(((JCheckBox)fields.get(1)).isSelected() ? "active" : "disabled");
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case ADDEVENT:
			if (username==null) return null;
			params = new Component[]{
					new JLabel("Add event for "+username),
					new JLabel("Text"),
					new JLabel("Datetime")
			};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "AddEvent "+username+" "+
							((JTextField)fields.get(0)).getText()+" "+
							((JTextField)fields.get(1)).getText();
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case REMOVEEVENT:
			if (username==null) return null;
			params = new Component[]{
					new JLabel("Remove event for "+username),
					new JLabel("Text")
			};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "RemoveEvent "+username+" "+
							((JTextField)fields.get(0)).getText();
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case ADDRANDOMEVENT:
			if (username==null) return null;
			params = new Component[]{
					new JLabel("Add random event for "+username),
					new JLabel("Text"),
					new JLabel("Start date"),
					new JLabel("End date")
			};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "AddRandomTimeEvent "+username+" "+
							((JTextField)fields.get(0)).getText()+" "+
							((JTextField)fields.get(1)).getText()+" "+
							((JTextField)fields.get(2)).getText();
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case CLONEEVENT:
			if (username==null) return null;
			params = new Component[]{
					new JLabel("Copy event from "+username),
					new JLabel("Text"),
					new JLabel("Destination user")
			};
			btSubmit.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					String cmdStr = "CloneEvent "+
							((JTextField)fields.get(0)).getText()+" "+
							username+" "+
							((JTextField)fields.get(1)).getText();
					mainWindow.perform(cmdStr);
					_this.dispose();
				}
			});
			break;
		case USERINFO:
			if (username==null) return null;
			mainWindow.perform("ShowInfo "+username);
		}
		return params;
	}
	
	CommandWindow(MainWindow mainW, CmdType cmd, String username, int x, int y) {
		mainWindow = mainW;
		Component[] params = getCmdDialogConfig(cmd, username);
		if (params!=null) {
		setBounds(x, y, 0, 0);
		setTitle(((JLabel)params[0]).getText());
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		SequentialGroup vGroup = layout.createSequentialGroup(),
				hGroup = layout.createSequentialGroup();
		ParallelGroup lGroup = layout.createParallelGroup(),
				tGroup = layout.createParallelGroup();
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		for(int i = 1; i < params.length; i++) {
			if (params[i] instanceof JLabel) {
				JTextField txtBox = new JTextField();
				txtBox.setPreferredSize(new Dimension(300, txtBox.getHeight()));
				fields.add(txtBox);
				vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(params[i]).addComponent(txtBox));
				lGroup.addComponent(params[i]);
				tGroup.addComponent(txtBox);
			}
			else {
				JLabel lbl = new JLabel();
				vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(lbl).addComponent(params[i]));
				lGroup.addComponent(lbl);
				tGroup.addComponent(params[i]);
				fields.add(params[i]);
			}
		}
		vGroup.addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(btSubmit));
		tGroup.addComponent(btSubmit);
		hGroup.addGroup(lGroup);
		hGroup.addGroup(tGroup);
		layout.setVerticalGroup(vGroup);
		layout.setHorizontalGroup(hGroup);
		getContentPane().add(panel);
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
	}
}

public class GSScheduler {

	public static void main(String[] args) {
		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
	}

}
