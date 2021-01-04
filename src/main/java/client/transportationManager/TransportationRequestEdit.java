package client.transportationManager;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.sql.Statement;

import client.UserInfo;
import net.miginfocom.swing.MigLayout;
import javax.swing.AbstractAction;
import javax.swing.Action;

public class TransportationRequestEdit extends JFrame {
	Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	private Connection connectionDB;
	private UserInfo userInfo;
	private Order order;
	private JTextPane textPane;
	private final Action rejectAction = new SwingAction();
	private final Action acceptAction = new SwingAction_1();

	TransportationRequestEdit(Connection conn, UserInfo info, Order order) {
		this.connectionDB = conn;
		this.userInfo = info;
		this.order = order;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(800, 500);
		this.setLocation(SCREEN_SIZE.width / 2 - this.getSize().width / 2,
				SCREEN_SIZE.height / 2 - this.getSize().height / 2);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[350.00px][350.00]", "[136px][]"));
				
						textPane = new JTextPane();
						textPane.setFont(new Font("Verdana", Font.PLAIN, 20));
						panel.add(textPane, "cell 0 0 2 1,alignx center,aligny top");
				
				JButton rejectButton = new JButton("New button");
				rejectButton.setAction(rejectAction);
				panel.add(rejectButton, "cell 0 1,alignx right");
				
				JButton acceptButton = new JButton("New button");
				acceptButton.setAction(acceptAction);
				panel.add(acceptButton, "cell 1 1");
		
		this.setPaneText();
		this.setVisible(true);
	}

	void setPaneText() {
		Order ord = this.order;
		String id = "ID: " + ord.id + "\n";
		String type = "��� �����:" + ord.type + "\n";
		String route = "�������: " + ord.startPoint + " - " + ord.endPoint + "\n";
		String weight = "���: " + ord.weight + "\n";
		String finalStr = id + type + route + weight;
		this.textPane.setText(finalStr);
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "���������");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "�������");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			String sql = "UPDATE requests SET status = 1 WHERE id = " + order.id;
			try {
				Statement statement = connectionDB.createStatement();
				statement.execute(sql);
			} catch (SQLException throwables) {
			}

		}
	}
}
