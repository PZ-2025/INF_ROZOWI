package pl.rozowi.app.database;

import java.sql.*;

public class DatabaseManager {

    private static final String PROD_URL = "jdbc:mariadb://localhost:3306/it_task_management?charset=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static String testUrl = null;

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver"); // sterownik produkcyjny
        } catch (ClassNotFoundException e) {
            System.err.println("Nie udało się załadować sterownika MariaDB.");
            e.printStackTrace();
        }
    }

    // Pozwala testom nadpisać adres URL
    public static void setTestUrl(String url) {
        testUrl = url;
        try {
            Class.forName("org.h2.Driver"); // Załaduj sterownik H2 tylko gdy potrzebny
        } catch (ClassNotFoundException e) {
            System.err.println("Nie udało się załadować sterownika H2.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (testUrl != null) {
            return DriverManager.getConnection(testUrl);
        }
        return DriverManager.getConnection(PROD_URL, USER, PASSWORD);
    }

    public static boolean schemaExists(String schemaName) {
        String urlWithoutSchema = "jdbc:mariadb://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(urlWithoutSchema, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?")) {
            stmt.setString(1, schemaName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
