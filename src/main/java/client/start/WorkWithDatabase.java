package client.start;
import client.Configuration;

import java.sql.DriverManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;


public class WorkWithDatabase {
	
	private static String DB_URL = "jdbc:postgresql://127.0.0.1:5432/JALP";
	private static boolean isCreated = false;
	private static Connection connection;
	public static Connection loginConnection() {
		if(isCreated == true) {
			return connection;
			}
		else {
			DB_URL = Configuration.getDBAdress();
			
			String login = new String("postgres");
			String pass = new String("4eJ7sKwB");
			connection = null;
			try {
				Class.forName("org.postgresql.Driver");
				connection = DriverManager.getConnection(DB_URL, login, pass);
			} catch (SQLException | ClassNotFoundException e) {
				LoginWindow.errorLabel.setText("������ �����������");
				e.printStackTrace();
			}
			isCreated = true;
			return connection;
		}
	}
}
