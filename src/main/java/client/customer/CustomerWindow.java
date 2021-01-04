package client.customer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;

import client.MessageListener;
import net.miginfocom.swing.MigLayout;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;



public class CustomerWindow extends JFrame {
	private static Connection CONNECTION_DB;
	private static ClientInfo CLIENT_INFO;
	private static Object[][] rawData;

	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private final Action myOrdersAction = new SwingAction();
	private DefaultTableModel resultsTableModel;
	private final Action newOrderAction = new SwingAction_1();
	private JTable ordersTable;
	private final Action exportAction = new SwingAction_2();
	private MessageListener messageListener;

	public CustomerWindow(int id) throws URISyntaxException {
		CONNECTION_DB = CustomerWorkWithDB.getConnection();
		CLIENT_INFO = new ClientInfo(CONNECTION_DB, id);
		System.out.println("client id " + CLIENT_INFO.getId());

		messageListener = new MessageListener();
		messageListener.start();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1920, 1020);
		this.setLocation(screenSize.width / 2 - this.getSize().width / 2,
				screenSize.height / 2 - this.getSize().height / 2);
		getContentPane().setLayout(new MigLayout("", "[1000.00][][478.00]", "[][246.00,grow][87.00][93.00][108.00]"));

		JButton myOrdersButton = new JButton("New button");
		myOrdersButton.setAction(myOrdersAction);
		getContentPane().add(myOrdersButton, "cell 0 0");

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 1,grow");

		Object[] columnHeaders = new String[]{"����� �������", "���� �������", "��� �����", "������", "����",
				"��� �����", "������"};
		resultsTableModel = new DefaultTableModel();
		resultsTableModel.setColumnIdentifiers(columnHeaders);
		setTransportationRequests();
		ordersTable = new JTable(resultsTableModel);
		resizeColumnWidth(ordersTable);

		scrollPane.setViewportView(ordersTable);

		JButton newOrderButton = new JButton("New button");
		newOrderButton.setAction(newOrderAction);
		getContentPane().add(newOrderButton, "cell 1 1");

		JButton exportButton = new JButton("New button");
		exportButton.setAction(exportAction);
		getContentPane().add(exportButton, "cell 1 1");

		Draft[] drafts = {new Draft_6455()};
		WebSocketClient cc = new WebSocketClient(new URI("ws://localhost:8887"), (Draft) new Draft_6455()) {

			@Override
			public void onMessage(String message) {
				System.out.println("Message: " + message);
				if (message.contains("Block")){
					int id = Integer.parseInt(message.split(":")[1]);
					if (id == CLIENT_INFO.getId()){
						JOptionPane.showMessageDialog(null,
								"Попытка входа в учётную запись. Учётная запись будет временно заблокирована!");
						dispose();
					}
				}
			}

			@Override
			public void onOpen(ServerHandshake handshake) {
				System.out.println("Connected to: " + getURI());


			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				System.out.println("Disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n");
			}

			@Override
			public void onError(Exception ex) {
				System.out.println("Exception occurred ...\n" + ex + "\n");
				ex.printStackTrace();
			}
		};

		cc.connect();

		ordersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ordersTable.setDefaultEditor(Object.class, null);
		ordersTable.setBackground(Color.LIGHT_GRAY);
		this.setVisible(true);
	}

	public void setTransportationRequests() {
		Object[][] result = new Object[0][];
		Connection connect = CONNECTION_DB;
		System.out.println(CLIENT_INFO.getId());
		String sql = "SELECT * FROM user_requests WHERE user_id = '" + CLIENT_INFO.getId() + "' ";
		boolean anyResults = false;
		int resultsAmount = 0;
		try {
			Statement statement = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs;
			// initialize hash maps for cargo_type, request_status, warehouse_code;
			String sqlForTypeHashMap = "SELECT * FROM cargo_types";
			HashMap<Integer, String> typeMap = new HashMap<>();
			rs = statement.executeQuery(sqlForTypeHashMap);
			rs.first();
			while (!rs.isAfterLast()) {
				typeMap.put(rs.getInt("type"), rs.getString("name"));
				rs.next();
			}

			String sqlForRequestStatus = "SELECT * FROM statuses";
			HashMap<Integer, String> statusMap = new HashMap<>();
			rs = statement.executeQuery(sqlForRequestStatus);
			rs.first();
			while (!rs.isAfterLast()) {
				statusMap.put(rs.getInt("status"), rs.getString("status_name"));
				rs.next();
			}

			String sqlForWarehouseCode = "SELECT * FROM warehouses";
			HashMap<Integer, String> warehouseMap = new HashMap<>();
			rs = statement.executeQuery(sqlForWarehouseCode);
			rs.first();
			while (!rs.isAfterLast()) {
				warehouseMap.put(rs.getInt("id"), rs.getString("name"));
				rs.next();
			}

			// get all requests id
			rs.close();
			rs = statement.executeQuery(sql);
			System.out.println(sql);
			rs.next();
			sql = "SELECT * FROM requests WHERE id IN (";
			int k = 0;
			while (!rs.isAfterLast()) {
				sql = sql + rs.getInt("request_id");
				if (!rs.isLast()) sql = sql + ",";
				else sql = sql + ")";
				rs.next();
				k++;
			}

			// get all request by id
			rs = statement.executeQuery(sql);
			result = new Object[k][];
			rawData = new Object[k][];

			Integer order_id;
			Date order_date;
			String order_cargo_type;
			String order_departure;
			String order_destination;
			Integer order_weight;
			String order_status;
			rs.first();
			int currentRequest = 0;
			while (!rs.isAfterLast()) {
				order_id = rs.getInt("id");
				order_date = rs.getDate("filling_time");
				order_cargo_type = typeMap.get(rs.getInt("cargo_type"));
				order_departure = warehouseMap.get(rs.getInt("departure_warehouse"));
				order_destination = warehouseMap.get(rs.getInt("destination_warehouse"));
				order_weight = rs.getInt("cargo_weight");
				order_status = statusMap.get(rs.getInt("status"));
				result[currentRequest] = new Object[]{order_id, order_date, order_cargo_type, order_departure,
						order_destination, order_weight, order_status};
				rawData[currentRequest] = result[currentRequest];
				anyResults = true;
				currentRequest++;
				rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (anyResults) {
			resultsTableModel.setNumRows(result.length);
			for (int i = 0; i < result.length; i++) {
				for (int k = 0; k < result[i].length; k++) {
					resultsTableModel.setValueAt(result[i][k], i, k);
				}
			}
		}
	}

	public void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 30; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 300)
				width = 300;
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "��� ������");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "����� �����");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
			NewOrderWindow w = new NewOrderWindow(CONNECTION_DB, CLIENT_INFO);
		}
	}

	private class SwingAction_2 extends AbstractAction {
		public SwingAction_2() {
			putValue(NAME, "�������");
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			JFrame pf = new JFrame();
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File("data.csv"));
			fileChooser.setDialogTitle("��������� ���� �");
			int userSelection = fileChooser.showSaveDialog(pf);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				try {
					FileWriter writer = new FileWriter(fileToSave);
					for (Object[] o : rawData){
						for(Object o1 : o){
							writer.write(o1.toString() + ";");
						}
						writer.write("\n");
					}
					writer.flush();
					writer.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
	}
}
