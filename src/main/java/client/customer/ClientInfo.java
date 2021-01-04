package client.customer;

import client.UserInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientInfo extends UserInfo {
	private int id;
	private String name;
	public ClientInfo(Connection connect, int id) {
		System.out.println("user id " + id);
		this.id = id;
		String sql = "SELECT * FROM users WHERE id='";
		try {
			Statement statement = connect.createStatement();
			ResultSet rs = statement.executeQuery(sql + id+ "' ");
			rs.next();
			this.name = rs.getString("full_name");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public int getId(){
		return this.id;
	}
}
