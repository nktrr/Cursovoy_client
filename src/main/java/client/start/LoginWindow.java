package client.start;


import client.admin.AdministratorWindow;
import client.customer.CustomerWindow;
import client.transportationManager.TransportationManagerWindow;
import org.apache.commons.codec.digest.DigestUtils;



import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

//import DigestUtils;

public class LoginWindow extends JFrame {

	private JPanel contentPane;
	private JTextField loginField;
	private JPasswordField passwordField;
	public static JLabel errorLabel;
	private final Action loginButtonPressed = new SwingAction();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow frame = new LoginWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(450, 300);
		this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
				screenSize.height / 2 - this.getSize().height / 2);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		loginField = new JTextField();
		loginField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == e.VK_ENTER) {
					passwordField.requestFocus();
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, loginField, 63, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, loginField, 114, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, loginField, -147, SpringLayout.EAST, contentPane);
		contentPane.add(loginField);
		loginField.setColumns(10);

		JButton loginButton = new JButton("Login");
		loginButton.setAction(loginButtonPressed);
		loginButton.setBorder(null);
		contentPane.add(loginButton);

		passwordField = new JPasswordField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, loginButton, 22, SpringLayout.SOUTH, passwordField);
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == e.VK_ENTER) {
					loginButton.doClick();
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.SOUTH, loginField, -22, SpringLayout.NORTH, passwordField);
		sl_contentPane.putConstraint(SpringLayout.NORTH, passwordField, 108, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, passwordField, 133, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, passwordField, 114, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, passwordField, -147, SpringLayout.EAST, contentPane);
		contentPane.add(passwordField);

		JLabel lblNewLabel = new JLabel("���� � �������");
		sl_contentPane.putConstraint(SpringLayout.WEST, loginButton, 0, SpringLayout.WEST, lblNewLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, loginButton, -8, SpringLayout.EAST, lblNewLabel);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblNewLabel, 43, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblNewLabel, 153, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblNewLabel, -6, SpringLayout.NORTH, loginField);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblNewLabel, -160, SpringLayout.EAST, contentPane);
		contentPane.add(lblNewLabel);

		errorLabel = new JLabel("");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, loginButton, -6, SpringLayout.NORTH, errorLabel);
		sl_contentPane.putConstraint(SpringLayout.NORTH, errorLabel, 192, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, errorLabel, 173, SpringLayout.WEST, contentPane);
		contentPane.add(errorLabel);
	}

	// loginButtonAction
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "�����");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
			Connection connectToDB = WorkWithDatabase.loginConnection();
			String sql = "SELECT * FROM users WHERE login='" + loginField.getText() + "' ";
			if (connectToDB != null) {
				try {
					Statement statement = connectToDB.createStatement();
					ResultSet rs = statement.executeQuery(sql);
					// System.out.println(rs.next());
					String hex = DigestUtils.md5Hex(DigestUtils.md5Hex(String.valueOf(passwordField.getPassword())));
					if (rs.next() && hex.contentEquals(rs.getString("password"))) {
						int id = rs.getInt("id");
						if (connectUser(id).equals("Can connect")){
							switch (rs.getInt("type")) {
								case (0):  //administrator
									System.out.println("admin");
									AdministratorWindow aw = new AdministratorWindow(id);
									break;
								case (1):   //transportation manager
									dispose();
									TransportationManagerWindow tw = new TransportationManagerWindow(id);
									connectToDB = null;
									break;

								case (2):   //individual
									dispose();
									try {
										CustomerWindow cw = new CustomerWindow(id);
									} catch (URISyntaxException uriSyntaxException) {
										uriSyntaxException.printStackTrace();
									}
									connectToDB = null;
									break;
							}
						}
					} else {
						errorLabel.setText("�������� ��� ������������ ��� ������");
					}
					// if ()
				} catch (SQLException s) {
					System.out.println(s);
				}
			}
		}

		private String connectUser(int id){
			try {
				String userStatus = getRequest("http://localhost:8080/api/getUserStatus/" + loginField.getText());
				if (userStatus.equals("Can connect")){
					String isConnected = getRequest("http://localhost:8080/api/checkUserConnection/" + id);
					if (isConnected.equals("Can connect")) return "Can connect";
					if (isConnected.equals("Connected")){
						JOptionPane.showMessageDialog(null,
								"Учётная запись уже используется. Учетная запись будет заблокирована на 24 часа.");
					}
				}
				if (userStatus.contains(":")){
					Object[] splittedStatus = userStatus.split(":");
					JOptionPane.showMessageDialog(null,"������ ��������� ������������ ��: "
							+ splittedStatus[1].toString() + ":"
							+ splittedStatus[2].toString() + ":"
							+ splittedStatus[3].toString());
					return "Blocked";
				}
			} catch (IOException e) {
			}
			return  "";
		}


		private String getRequest(String url) throws IOException {
			System.out.println(url);
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			connection.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}

	}
}
