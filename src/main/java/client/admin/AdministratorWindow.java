package client.admin;

import client.DB_Util;
import client.customer.ClientInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class AdministratorWindow extends JFrame{

    private static Connection CONNECTION_DB;
    private static ClientInfo USER_INFO;
    public AdministratorWindow(int id) {
        CONNECTION_DB = DB_Util.getConnection();
        getContentPane().setLayout(new MigLayout("", "[200px]", "[200px]"));

        JButton importButton = new JButton("������");
        Action importAction = new ImportAction();
        importButton.setAction(importAction);
        getContentPane().add(importButton, "grow,cell 0 0");

        JButton exportButton = new JButton("�������");
        Action exportAction = new ExportAction();
        exportButton.setAction(exportAction);
        getContentPane().add(exportButton, "cell 0 0, grow");
        this.setSize(600,600);
        this.setVisible(true);
    }
    private class ImportAction extends AbstractAction{
        public ImportAction() {
            putValue(NAME, "������������� ������");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame pf = new JFrame();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("������������� ����");
            int userSelection = fileChooser.showSaveDialog(pf);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToImport = fileChooser.getSelectedFile();
                try {
                    int dialogButton = JOptionPane.YES_NO_OPTION;
                    int dialogResult = JOptionPane.showConfirmDialog (null, "�� ������� ��� ������ ������������� ������?","��������� �� �������������",dialogButton);
                    if(dialogResult == JOptionPane.YES_OPTION){
                        Statement statement = CONNECTION_DB.createStatement();
                        FileReader f = new FileReader(fileToImport);
                        BufferedReader b = new BufferedReader(f);
                        String line;
                        while ((line = b.readLine())!=null){
                            String[] s = line.split(";");
                            String sqlForInsertRequest = "INSERT INTO public.requests (cargo_type, departure_warehouse," +
                                    " cargo_weight, destination_warehouse, filling_time, status) VALUES(" +
                                    s[2] + "," + s[3] + "," + s[5] + "," + s[4] + ",'" +s[1] + "'," + s[6] +") RETURNING id";
                            ResultSet r = statement.executeQuery(sqlForInsertRequest);
                            r.next();
                            int id = r.getInt("id");
                            String sqlForInsertUserRequest = "INSERT INTO public.user_requests (user_id, request_id) " +
                                    "VALUES (" + id + "," + s[0] + ");";
                            statement.execute(sqlForInsertUserRequest);
                    }
                    }
                } catch (IOException | SQLException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        }
    }
    private class ExportAction extends AbstractAction{
        public ExportAction() {
            putValue(NAME, "�������������� ������");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String sql = "SELECT id, cargo_type, departure_warehouse, cargo_weight, description," +
                    " destination_warehouse, filling_time, status, user_id\n" +
                    "\tFROM public.requests JOIN public.user_requests ON requests.id = user_requests.request_id;";
            Statement statement = null;
            try {
                statement = CONNECTION_DB.createStatement();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            try {
                String sqlForAmount = "SELECT count(*) from requests";
                statement = CONNECTION_DB.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = statement.executeQuery(sqlForAmount);
                rs.first();
                int amount = rs.getInt("count");
                Object[][] rawData = new Object[amount][];
                rs = statement.executeQuery(sql);
                int order_id;
                Date order_date;
                int order_cargo_type;
                int order_departure;
                int order_destination;
                int order_weight;
                int order_status;
                int user_id;
                int currentRequest = 0;
                rs.first();
                while (!rs.isAfterLast()){
                    order_id = rs.getInt("id");
                    order_date = rs.getDate("filling_time");
                    order_cargo_type = rs.getInt("cargo_type");
                    order_departure = rs.getInt("departure_warehouse");
                    order_destination = rs.getInt("destination_warehouse");
                    order_weight = rs.getInt("cargo_weight");
                    order_status = rs.getInt("status");
                    user_id = rs.getInt("user_id");
                    rawData[currentRequest] = new Object[]{order_id, order_date, order_cargo_type, order_departure,
                            order_destination, order_weight, order_status, user_id};
                    currentRequest++;
                    rs.next();
                }

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
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }
    }
}
