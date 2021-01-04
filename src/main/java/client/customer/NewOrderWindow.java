package client.customer;

import java.sql.Array;
import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;

public class NewOrderWindow extends JFrame{
	private final Action addOrderAction = new SwingAction();
	private final Action cancelOrderAction = new SwingAction_1();
	private JComboBox typeBox;
	private JComboBox arrivalBox;
	private JComboBox fromBox;
	private JTextField weightField;
	private JEditorPane editorPane;
	private Connection connect;
	private ClientInfo info;
	NewOrderWindow(Connection connect, ClientInfo info){
		this.info = info;
		this.connect = connect;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(900, 900);
		getContentPane().setLayout(new MigLayout("",
				"[][108.00,center][200.00,center][200.00,center]",
				"[][52.00,bottom][43.00,top][32.00,bottom][45.00,center][][38.00][]" +
						"[42.00][27.00][109.00][76.00][65.00][]"));
		
		JLabel lblNewLabel = new JLabel("�������� ��� �����");
		getContentPane().add(lblNewLabel, "cell 1 2,alignx center");
		
		typeBox = new JComboBox();
		getContentPane().add(typeBox, "cell 2 2,growx");
		
		JLabel lblNewLabel_1 = new JLabel("������");
		getContentPane().add(lblNewLabel_1, "cell 1 4");
		
		arrivalBox = new JComboBox();
		getContentPane().add(arrivalBox, "cell 2 4,growx");
		
		JLabel lblNewLabel_2 = new JLabel("����");
		getContentPane().add(lblNewLabel_2, "cell 1 6,alignx center");
		
		fromBox = new JComboBox();
		getContentPane().add(fromBox, "cell 2 6,growx");
		
		JLabel lblNewLabel_3 = new JLabel("��� �����");
		getContentPane().add(lblNewLabel_3, "cell 1 8,alignx center");
		
		weightField = new JTextField();
		getContentPane().add(weightField, "cell 2 8,growx");
		weightField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("�������������� ��������");
		getContentPane().add(lblNewLabel_4, "cell 1 10,alignx center");
		
		editorPane = new JEditorPane();
		getContentPane().add(editorPane, "cell 2 10 2 2,grow");
		
		JButton cancelOrderButton = new JButton("New button");
		cancelOrderButton.setAction(cancelOrderAction);
		getContentPane().add(cancelOrderButton, "cell 2 13");
		
		JButton addOrderButton = new JButton("New button");
		addOrderButton.setAction(addOrderAction);
		getContentPane().add(addOrderButton, "cell 3 13");
		
		Object[] warehouses = getWarehouses(connect);
		for(Object i : warehouses) {
			arrivalBox.addItem(i);
			fromBox.addItem(i);
			
		}
		Object[] cargoTypes = new Object[0];
		try {
			cargoTypes = getCargoTypes();
		} catch (SQLException throwables) {
		}
		for(Object i : cargoTypes) {
			typeBox.addItem(i);
		}
		this.setVisible(true);
	}
	
	private String[] getCargoTypes() throws SQLException {
		String[] cargoTypes = {""};
		Statement statement = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = statement.executeQuery("SELECT * FROM cargo_types");
		ArrayList<String> c = new ArrayList<>();
		rs.first();
		while (!rs.isAfterLast()){
			System.out.println(rs.getString("name"));
			c.add(rs.getString("name"));
			rs.next();
		}
		cargoTypes = new String[c.size()];
		int k = 0;
		for (String s : c){
			cargoTypes[k] = s;
			k++;
		}
		return cargoTypes;
	}
	private String[] getWarehouses(Connection connect) {
		String[] warehouses = {""};
		String sql = "SELECT * FROM warehouses";
		try {
			Statement statement = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(sql);
			rs.last();
			int resultsAmount = rs.getRow();
			int i = 0;
			warehouses = new String[resultsAmount];
			rs.first();
			while ((i <= resultsAmount - 1)) {
				warehouses[i] = rs.getString("name");
				i++;
				rs.next();
			}
		} catch (SQLException e) {
		}
		return warehouses;
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "�������� �����");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			addOrder();
		}
	}
	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "�������� �����");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	public void addOrder() {
		boolean isAllNormal = true;
		String cargoName = (String) this.typeBox.getSelectedItem();
		String s = "SELECT * FROM cargo_types WHERE name = '" + cargoName + "'";
		String from = (String) this.fromBox.getSelectedItem();
		String to = (String) this.arrivalBox.getSelectedItem();
		int weight = 0;
		try{
			weight = Integer.valueOf(this.weightField.getText());
		} catch (NumberFormatException e){
			isAllNormal = false;
			JOptionPane.showMessageDialog(this, "����������� ������ ���.", "������", JOptionPane.ERROR_MESSAGE);
		}
		if (weight>30000){
			JOptionPane.showMessageDialog(this, "��������� ��� ��������� ���������� (30000 ��).", "������", JOptionPane.ERROR_MESSAGE);
			isAllNormal = false;
		}
		if (from.equals(to)){
			JOptionPane.showMessageDialog(this, "����� ����������� ��������� � ������� ����������.", "������", JOptionPane.ERROR_MESSAGE);
			isAllNormal = false;
		}
		String additional = this.editorPane.getText();
		Statement statement;
		try {
			System.out.println(s);
			statement = connect.createStatement();
			ResultSet r = statement.executeQuery(s);
			r.next();
			Integer cargoType = r.getInt("type");
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			String sql =
					" INSERT INTO public.requests(" +
					"filling_time, cargo_type, departure_warehouse, destination_warehouse, cargo_weight, " +
					"status, description)" +
					"VALUES ('" + formatter.format(date) + "','"+ cargoType + "','" + this.fromBox.getSelectedIndex()
					+ "','" + this.arrivalBox.getSelectedIndex()
					+ "','" + weight + "','" +
					0 + "','" +  additional + "')" +
					"RETURNING id ";
			if (isAllNormal){
				r = statement.executeQuery(sql);
				r.next();
				Integer id = r.getInt("id");
				sql = "INSERT INTO public.user_requests (user_id, request_id) VALUES (" + info.getId() + "," + id + ");";
				statement.execute(sql);
				JOptionPane.showMessageDialog(this, "������ ������� �������.", "�����", JOptionPane.NO_OPTION);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
}
