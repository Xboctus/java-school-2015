import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

public class Gui {
/*	static private class UserCreateDialog extends JDialog {
		public UserCreateDialog(JFrame owner) {
			super(owner);
		}
	}
*/
	static private class SchedulerFrame extends JFrame implements ActionListener {
		private JButton[] actionButtons;
		private JTextArea messagesArea;

		private SchedulerFrame() {
			this.setTitle("Scheduler");
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
			messagesArea.setText("Sample text\nSample text\n");

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridheight = actionButtons.length + 1;
			gbc.fill = GridBagConstraints.BOTH;

			JScrollPane sp = new JScrollPane(messagesArea);

			pane.add(sp, gbc);

			this.pack();
			this.setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Coordinator.Command c = Coordinator.Command.valueOf(e.getActionCommand());
			Coordinator.Error error = null;
			switch (c) {
			case UADD: {
				JTextField name = new JTextField();
				JTextField timezone = new JTextField();
				JCheckBox active = new JCheckBox();
				final JComponent[] inputs = new JComponent[] {
					new JLabel("Name"), name,
					new JLabel("Timezone"), timezone,
					new JLabel("Is active"), active
				};
				messagesArea.append(e.getActionCommand());
				JOptionPane.showMessageDialog(this, inputs, "Create user", JOptionPane.PLAIN_MESSAGE);
				error = Coordinator.createUser(
					name.getText(),
					TimeZone.getTimeZone("GMT" + timezone),
					active.isSelected()
				);
				break;
			}
			case QUIT:
				this.dispose();
				return;
			}
			if (error == Coordinator.Error.NO_ERROR) {
				if (c.r != null) {
					messagesArea.append(c.r + "\n");
				}
			} else {
				messagesArea.append(error.msg + "\n");
			}
		}
	}

	public static void run() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				new SchedulerFrame();
			}
		});
	}
}
