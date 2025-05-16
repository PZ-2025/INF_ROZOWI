package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Settings;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * Testy jednostkowe dla klasy SettingsDAO, odpowiedzialnej za operacje
 * na ustawieniach użytkowników w bazie danych.
 */
public class SettingsDAOTest {

    private Connection connection;
    private SettingsDAO settingsDAO;

    /**
     * Konfiguracja środowiska testowego przed każdym testem.
     * Tworzy tymczasową bazę danych H2 w pamięci oraz tabelę settings
     * z jednym przykładowym rekordem dla użytkownika o ID 1.
     */
    @Before
    public void setUp() throws Exception {
        DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DatabaseManager.getConnection();

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE settings ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT NOT NULL,"
                    + "theme VARCHAR(255),"
                    + "default_view VARCHAR(255),"
                    + "last_password_change TIMESTAMP"
                    + ")");

            stmt.execute("INSERT INTO settings (user_id, theme, default_view, last_password_change) "
                    + "VALUES (1, 'Dark', 'Dashboard', '2023-01-01 10:00:00')");
        }

        settingsDAO = new SettingsDAO();
    }

    /**
     * Czyszczenie zasobów po wykonaniu każdego testu.
     * Usuwa wszystkie tabele z bazy danych i zamyka połączenie.
     */
    @After
    public void tearDown() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        connection.close();
        DatabaseManager.setTestUrl(null);
    }

    /**
     * Test sprawdzający metodę getSettingsByUserId dla istniejących ustawień.
     * Powinien zwrócić poprawny obiekt Settings dla użytkownika o ID 1.
     */
    @Test
    public void testGetSettingsByUserId_found() {
        Settings s = settingsDAO.getSettingsByUserId(1);

        assertNotNull(s);
        assertEquals(1, s.getId());
        assertEquals(1, s.getUserId());
        assertEquals("Dark", s.getTheme());
        assertEquals("Dashboard", s.getDefaultView());
        assertNotNull(s.getLastPasswordChange());
    }

    /**
     * Test sprawdzający metodę getSettingsByUserId dla nieistniejących ustawień.
     * Powinien zwrócić null dla użytkownika, który nie ma zapisanych ustawień.
     */
    @Test
    public void testGetSettingsByUserId_notFound() {
        Settings s = settingsDAO.getSettingsByUserId(99);

        assertNull(s);
    }

    /**
     * Test sprawdzający poprawne dodanie nowych ustawień do bazy danych.
     * Tworzy ustawienia dla użytkownika o ID 5 i weryfikuje, czy zostały
     * prawidłowo zapisane poprzez próbę ich odczytu.
     */
    @Test
    public void testInsertSettings_success() {
        Settings in = new Settings();
        in.setUserId(5);
        in.setTheme("Light");
        in.setDefaultView("Tasks");
        in.setLastPasswordChange(Timestamp.valueOf(LocalDateTime.now()));

        boolean ok = settingsDAO.insertSettings(in);

        assertTrue(ok);

        Settings inserted = settingsDAO.getSettingsByUserId(5);
        assertNotNull(inserted);
        assertEquals("Light", inserted.getTheme());
        assertEquals("Tasks", inserted.getDefaultView());
    }

    /**
     * Test sprawdzający reakcję na próbę dodania ustawień dla użytkownika,
     * który już posiada ustawienia w bazie. Po dodaniu ograniczenia unique
     * dla user_id, próba ponownego dodania ustawień powinna zakończyć się
     * niepowodzeniem.
     */
    @Test
    public void testInsertSettings_failure() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE settings ADD CONSTRAINT unique_user UNIQUE (user_id)");
        }

        Settings in1 = new Settings();
        in1.setUserId(7);
        in1.setTheme("Dark");
        boolean ok1 = settingsDAO.insertSettings(in1);
        assertTrue(ok1);

        Settings in2 = new Settings();
        in2.setUserId(7);
        in2.setTheme("Light");

        boolean ok2 = settingsDAO.insertSettings(in2);

        assertFalse(ok2);
    }

    /**
     * Test sprawdzający aktualizację istniejących ustawień użytkownika.
     * Najpierw dodaje ustawienia dla użytkownika o ID 3, a następnie
     * aktualizuje je i weryfikuje, czy zmiany zostały poprawnie zapisane.
     */
    @Test
    public void testUpdateSettings_success() {
        Settings in = new Settings();
        in.setUserId(3);
        in.setTheme("Light");
        in.setDefaultView("Tasks");
        settingsDAO.insertSettings(in);

        Settings update = new Settings();
        update.setUserId(3);
        update.setTheme("Dark");
        update.setDefaultView("Home");

        boolean ok = settingsDAO.updateSettings(update);

        assertTrue(ok);

        Settings updated = settingsDAO.getSettingsByUserId(3);
        assertNotNull(updated);
        assertEquals("Dark", updated.getTheme());
        assertEquals("Home", updated.getDefaultView());
    }

    /**
     * Test sprawdzający usunięcie ustawień użytkownika z bazy danych.
     * Dodaje ustawienia dla użytkownika o ID 8, usuwa je, a następnie
     * weryfikuje, czy faktycznie zostały usunięte.
     */
    @Test
    public void testDeleteSettings_success() {
        Settings in = new Settings();
        in.setUserId(8);
        in.setTheme("Light");
        settingsDAO.insertSettings(in);

        boolean ok = settingsDAO.deleteSettings(8);

        assertTrue(ok);

        Settings deleted = settingsDAO.getSettingsByUserId(8);
        assertNull(deleted);
    }

    /**
     * Test sprawdzający aktualizację domyślnego widoku użytkownika.
     * Tworzy ustawienia z domyślnym widokiem "Tasks", aktualizuje go na
     * "Dashboard" i weryfikuje, czy zmiana została poprawnie zapisana.
     */
    @Test
    public void testUpdateDefaultView_success() {
        Settings in = new Settings();
        in.setUserId(4);
        in.setTheme("Light");
        in.setDefaultView("Tasks");
        settingsDAO.insertSettings(in);

        boolean ok = settingsDAO.updateDefaultView(4, "Dashboard");

        assertTrue(ok);

        Settings updated = settingsDAO.getSettingsByUserId(4);
        assertNotNull(updated);
        assertEquals("Dashboard", updated.getDefaultView());
    }

    /**
     * Test sprawdzający aktualizację daty ostatniej zmiany hasła dla
     * użytkownika, który już posiada rekord ustawień. Weryfikuje, czy
     * nowa data jest późniejsza lub równa dacie przed wywołaniem metody.
     */
    @Test
    public void testUpdateLastPasswordChange_existing() {
        Settings in = new Settings();
        in.setUserId(2);
        settingsDAO.insertSettings(in);

        Timestamp before = new Timestamp(System.currentTimeMillis());

        boolean ok = settingsDAO.updateLastPasswordChange(2);

        assertTrue(ok);

        Settings updated = settingsDAO.getSettingsByUserId(2);
        assertNotNull(updated);
        assertNotNull(updated.getLastPasswordChange());

        assertTrue(updated.getLastPasswordChange().after(before) ||
                updated.getLastPasswordChange().equals(before));
    }

    /**
     * Test sprawdzający aktualizację daty ostatniej zmiany hasła dla
     * użytkownika, który nie posiada jeszcze rekordu ustawień.
     * Metoda powinna utworzyć nowy rekord z datą zmiany hasła.
     */
    @Test
    public void testUpdateLastPasswordChange_newRecord() {
        boolean ok = settingsDAO.updateLastPasswordChange(7);

        assertTrue(ok);

        Settings created = settingsDAO.getSettingsByUserId(7);
        assertNotNull(created);
        assertNotNull(created.getLastPasswordChange());
    }
}