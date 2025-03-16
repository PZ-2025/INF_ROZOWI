package pl.rozowi.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    // Używamy prefiksu "jdbc:mariadb://" dla MariaDB
    private static final String URL = "jdbc:mariadb://localhost:3306/it_task_management?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Metoda sprawdzająca, czy schemat (baza) już istnieje
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
