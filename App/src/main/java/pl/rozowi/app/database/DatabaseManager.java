package pl.rozowi.app.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static final String PROD_URL = "jdbc:mariadb://localhost:3306/it_task_management?charset=utf8";
    private static final String EMBEDDED_URL = "jdbc:h2:file:%s/it_task_management;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String MARIADB_USER = "root";
    private static final String MARIADB_PASSWORD = "";

    private static String testUrl = null;
    private static String databasePath;
    private static boolean useEmbedded = false;

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Class.forName("org.h2.Driver");

            File appDir = new File(System.getProperty("user.home") + File.separator + "RozowiApp");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            databasePath = appDir.getAbsolutePath();

            useEmbedded = !isMariaDBAvailable();
        } catch (ClassNotFoundException e) {
            System.err.println("Nie udało się załadować sterownika bazy danych.");
            e.printStackTrace();
        }
    }

    public static void setTestUrl(String url) {
        testUrl = url;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Nie udało się załadować sterownika H2.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (testUrl != null) {
            return DriverManager.getConnection(testUrl);
        }

        if (useEmbedded) {
            String url = String.format(EMBEDDED_URL, databasePath);
            return DriverManager.getConnection(url, USER, PASSWORD);
        } else {
            return DriverManager.getConnection(PROD_URL, MARIADB_USER, MARIADB_PASSWORD);
        }
    }

    public static boolean schemaExists(String schemaName) {
        if (useEmbedded) {
            try (Connection conn = getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getTables(null, "PUBLIC", "USERS", new String[] {"TABLE"});
                return rs.next();
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        } else {
            String urlWithoutSchema = "jdbc:mariadb://localhost:3306/";
            try (Connection conn = DriverManager.getConnection(urlWithoutSchema, MARIADB_USER, MARIADB_PASSWORD);
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

    private static boolean isMariaDBAvailable() {
        String urlWithoutSchema = "jdbc:mariadb://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(urlWithoutSchema, MARIADB_USER, MARIADB_PASSWORD)) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean initializeDatabase() {
        if (!useEmbedded) {
            return true;
        }

        try (Connection conn = getConnection()) {
            if (!tableExists(conn, "USERS")) {
                executeScriptFromResource(conn, "/db/migration/V1__init.sql");
                executeScriptFromResource(conn, "/db/seeder/V1__init.sql");
                return true;
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        return rs.next();
    }

    private static void executeScriptFromResource(Connection conn, String resourcePath) throws SQLException {
        try {
            InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new SQLException("Resource not found: " + resourcePath);
            }

            String script = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            executeBatchScript(conn, script);
        } catch (Exception ex) {
            throw new SQLException("Error executing script: " + resourcePath, ex);
        }
    }

    private static void executeBatchScript(Connection conn, String script) throws SQLException {
        String[] statements = script.split(";");

        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    stmt.execute(trimmedStatement);
                }
            }
        }
    }
}