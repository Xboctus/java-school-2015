import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui {
	static private class UserCreateDialog extends JDialog {
		public UserCreateDialog(JFrame owner) {
			super(owner);
		}
	}

	static private class SchedulerFrame extends JFrame implements ActionListener {
		private JButton[] actionButtons;
		private JTextArea messagesArea;

		private SchedulerFrame() {
			super("Scheduler");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setMinimumSize(new Dimension(500, -1));

			Container pane = this.getContentPane();

			GridBagLayout layout = new GridBagLayout();
			layout.columnWeights = new double[]{1, 2};
			pane.setLayout(layout);

			actionButtons = new JButton[Coordinator.Command.values().length];
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;

			for (Coordinator.Command c: Coordinator.Command.values()) {
				JButton button = (actionButtons[c.ordinal()] = new JButton(c.title));
				gbc.gridy = c.ordinal();
				pane.add(button, gbc);
				button.addActionListener(this);
				button.setActionCommand(c.toString());
			}

			messagesArea = new JTextArea();
			messagesArea.setEditable(false);
			messagesArea.setText("Sample text\nSample text");

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridheight = actionButtons.length + 1;
			gbc.fill = GridBagConstraints.BOTH;

			JScrollPane sp = new JScrollPane(messagesArea);

			pane.add(sp, gbc);

			this.pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Coordinator.Command c = Coordinator.Command.valueOf(e.getActionCommand());
			switch (c) {
			case UADD: {
				JTextField firstName = new JTextField();
				JTextField lastName = new JTextField();
				JPasswordField password = new JPasswordField();
				final JComponent[] inputs = new JComponent[] {
					new JLabel("First"), firstName,
					new JLabel("Last"), lastName,
					new JLabel("Password"), password
				};
				messagesArea.append(e.getActionCommand());
				JOptionPane.showMessageDialog(null, inputs, "My custom dialog", JOptionPane.PLAIN_MESSAGE);
				JDialog d = new UserCreateDialog(this);
				break;
			}
			case QUIT:
				this.dispose();
			}
		}
	}

	public static void run() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				SchedulerFrame frame = new SchedulerFrame();
				frame.setVisible(true);
			}
		});
	}
}
