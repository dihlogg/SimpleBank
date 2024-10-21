package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BankDB;encrypt=true;trustServerCertificate=true";
	private static final String USER = "sa";
	private static final String PASSWORD = "123456";

	public static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new SQLException("SQLServerDriver not found.");
		}
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}