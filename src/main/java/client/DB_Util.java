package client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Util {

    private static String DB_URL = "jdbc:postgresql://127.0.0.1:5432/JALP";
    private static boolean isCreated = false;
    private static Connection connection;

    public static Connection getConnection() {
        if (isCreated == true) {
            return connection;
        } else {
            DB_URL = Configuration.getDBAdress();

            String login = new String("postgres");
            String pass = new String("4eJ7sKwB");
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
            }
            connection = null;
            try {
                connection = DriverManager.getConnection(DB_URL, login, pass);
            } catch (SQLException e) {
            }
            isCreated = true;
            return connection;
        }
    }
}
