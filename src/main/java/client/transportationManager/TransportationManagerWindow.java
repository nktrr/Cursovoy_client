package client.transportationManager;

import java.awt.Font;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.Color;
import java.awt.Panel;
import java.util.Date;
import java.util.HashMap;

import client.UserInfo;
import net.miginfocom.swing.MigLayout;

public class TransportationManagerWindow extends JFrame {
	private static Object[][] rawData;
	private JPanel contentPane;
	private static Connection CONNECTION_DB;
	private TransportManagerWorkDB tmd;
	private static UserInfo USER_INFO;
	private JTable transportationRequestsTable;
	private DefaultTableModel resultsTableModel;
	private Panel transportationRequestsPanel;
	private final Action exportAction = new SwingAction_1();

	/**
	 * Launch the application.
	 */

	/**
	 * Create the frame.
	 */
	public TransportationManagerWindow(int id) {
		int requestsPageNumber = 0;
		CONNECTION_DB = TransportManagerWorkDB.transportManagerGetConnection();
		USER_INFO = new ManagerInfo(CONNECTION_DB, id);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 1920, 1020);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu munuProgram = new JMenu("���������");
		menuBar.add(munuProgram);

		JMenuItem menuAboutProgram = new JMenuItem("� ���������");
		munuProgram.add(menuAboutProgram);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		Object[] columnHeaders = new String[] { "ID �������", "����� �������", "��� �����", "������", "����",
				"��� �����", "���� �������" };
		resultsTableModel = new DefaultTableModel();
		resultsTableModel.setColumnIdentifiers(columnHeaders);
		updateTransportationRequests(1);
		JButton trbImg = new JButton("�������");
		contentPane.setLayout(
				new MigLayout("", "[1px][122.00][112.00][155.00]", "[48.00px][111.00][154.00][142.00][191.00]"));

		JButton infoAboutDriversButton = new JButton("���� � ���������");
		infoAboutDriversButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				driverInfoClick();
			}
		});

		JButton transportationRequestsButton = new JButton("������� �� �������������");
		contentPane.add(transportationRequestsButton, "cell 0 0,grow");
		transportationRequestsButton.setBorderPainted(false);
		transportationRequestsButton.setBorder(null);

		infoAboutDriversButton.setBorderPainted(false);
		infoAboutDriversButton.setBorder(null);
		contentPane.add(infoAboutDriversButton, "cell 1 0,grow");


		transportationRequestsPanel = new Panel();

		contentPane.add(transportationRequestsPanel, "cell 0 1 4 4,grow");

		transportationRequestsTable = new JTable(resultsTableModel);
		transportationRequestsTable.setFont(new Font("Verdana", Font.PLAIN, 20));
		resizeColumnWidth(transportationRequestsTable);
		transportationRequestsPanel.add(transportationRequestsTable);

		transportationRequestsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					editRequest();
					
				}
			}
		});

		transportationRequestsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		transportationRequestsTable.setDefaultEditor(Object.class, null);
		transportationRequestsTable.setBackground(Color.LIGHT_GRAY);
		this.setVisible(true);

	}

	// update transportation requests in table
	public void updateTransportationRequests(int page) {
		// 30 results per page
		Object[][] result = new Object[0][];
		Connection connect = CONNECTION_DB;
		String sql = "SELECT * FROM requests";
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
			String sqlFromAmount = "SELECT count(id) FROM requests";
			rs = statement.executeQuery(sqlFromAmount);
			int k = 0;
			rs.first();
			result = new Object[rs.getInt("count")][];
			rawData = new Object[rs.getInt("count")][];

			Integer order_id;
			Date order_date;
			String order_cargo_type;
			String order_departure;
			String order_destination;
			Integer order_weight;
			String order_status;
			rs = statement.executeQuery(sql);
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
				System.out.println(order_id +  " " + order_cargo_type + " " + order_weight);
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

	// automatic resize table
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

	// MOUSE HANDLERS BELOW

	// driver info handler
	private void driverInfoClick() {
		this.transportationRequestsPanel.setVisible(false);
		this.repaint();
		System.out.println("updated");
	}

	private void editRequest() {
		int i = transportationRequestsTable.getSelectedRow();
		int id = Integer.parseInt(transportationRequestsTable.getValueAt(i, 0).toString());
		String type = (String) transportationRequestsTable.getValueAt(i, 2);
		String startPoint = (String) transportationRequestsTable.getValueAt(i, 3);
		String endPoint = (String) transportationRequestsTable.getValueAt(i, 4);
		int weight = Integer.parseInt(transportationRequestsTable.getValueAt(i, 5).toString());
		Order ord = new Order(id, type, startPoint, endPoint, weight);
		TransportationRequestEdit tre = new TransportationRequestEdit(CONNECTION_DB, USER_INFO, ord);
	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
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
