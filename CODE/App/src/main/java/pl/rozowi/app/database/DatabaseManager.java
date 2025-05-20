package pl.rozowi.app.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Klasa zarządzająca połączeniami do bazy danych i inicjalizacją schematu.
 * Obsługuje dwa tryby pracy:
 * <ul>
 *   <li>Tryb produkcyjny z MariaDB</li>
 *   <li>Tryb embedded z H2 (używany gdy MariaDB nie jest dostępna)</li>
 * </ul>
 * Automatycznie wykrywa dostępność MariaDB i przełącza się między trybami.
 */
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

    /**
     * Ustawia testowy URL bazy danych (używane w testach jednostkowych).
     * Wymusza użycie bazy H2 niezależnie od dostępności MariaDB.
     *
     * @param url URL testowej bazy danych
     */
    public static void setTestUrl(String url) {
        testUrl = url;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Nie udało się załadować sterownika H2.");
            e.printStackTrace();
        }
    }

    /**
     * Zwraca połączenie do bazy danych.
     * Wybiera odpowiedni typ połączenia w zależności od konfiguracji:
     * <ol>
     *   <li>Jeśli ustawiono testUrl - używa go (dla testów)</li>
     *   <li>Jeśli MariaDB jest niedostępna - używa bazy H2 w trybie embedded</li>
     *   <li>W przeciwnym razie używa produkcyjnej bazy MariaDB</li>
     * </ol>
     *
     * @return aktywne połączenie do bazy danych
     * @throws SQLException jeśli wystąpi błąd podczas nawiązywania połączenia
     */
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

    /**
     * Sprawdza czy określony schemat bazy danych istnieje.
     * Działa inaczej dla H2 i MariaDB ze względu na różnice w systemach zarządzania bazami.
     *
     * @param schemaName nazwa schematu do sprawdzenia
     * @return true jeśli schemat istnieje, false w przeciwnym przypadku
     */
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

    /**
     * Sprawdza dostępność serwera MariaDB.
     *
     * @return true jeśli MariaDB jest dostępna, false w przeciwnym przypadku
     */
    private static boolean isMariaDBAvailable() {
        String urlWithoutSchema = "jdbc:mariadb://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(urlWithoutSchema, MARIADB_USER, MARIADB_PASSWORD)) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Inicjalizuje bazę danych w trybie embedded (H2).
     * Wykonuje skrypty migracji i seedera jeśli tabela USERS nie istnieje.
     *
     * @return true jeśli inicjalizacja się powiodła lub nie była potrzebna,
     *         false jeśli wystąpił błąd podczas inicjalizacji
     */
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

    /**
     * Sprawdza czy tabela istnieje w bieżącej bazie danych.
     *
     * @param conn aktywne połączenie do bazy danych
     * @param tableName nazwa tabeli do sprawdzenia
     * @return true jeśli tabela istnieje, false w przeciwnym przypadku
     * @throws SQLException jeśli wystąpi błąd podczas sprawdzania
     */
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, new String[] {"TABLE"});
        return rs.next();
    }

    /**
     * Wykonuje skrypt SQL z zasobów aplikacji.
     *
     * @param conn aktywne połączenie do bazy danych
     * @param resourcePath ścieżka do zasobu ze skryptem SQL
     * @throws SQLException jeśli wystąpi błąd podczas wykonywania skryptu
     */
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

    /**
     * Wykonuje zestaw poleceń SQL jako batch.
     * Dzieli skrypt na pojedyncze polecenia używając średnika jako separatora.
     *
     * @param conn aktywne połączenie do bazy danych
     * @param script pełny skrypt SQL do wykonania
     * @throws SQLException jeśli wystąpi błąd podczas wykonywania poleceń
     */
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