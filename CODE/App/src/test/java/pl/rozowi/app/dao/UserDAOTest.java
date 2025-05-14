package pl.rozowi.app.dao;

import org.junit.jupiter.api.*;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static UserDAO userDAO;
    private static User testUser;

    private static final String ORIGINAL_EMAIL = "test.user@example.com";
    private static final String UPDATED_EMAIL = "updated.user@example.com";

    /**
     * Pomocnicza metoda do czyszczenia użytkownika testowego z bazy.
     * Usuwa zarówno użytkownika, jak i powiązane ustawienia (foreign key).
     */
    private static void cleanUpTestUser(String email) {
        String deleteSettingsSql = "DELETE FROM settings WHERE user_id IN (SELECT id FROM users WHERE email = ?)";
        String deleteUserSql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement psSettings = conn.prepareStatement(deleteSettingsSql);
             PreparedStatement psUser = conn.prepareStatement(deleteUserSql)) {
            psSettings.setString(1, email);
            psUser.setString(1, email);
            psSettings.executeUpdate();
            psUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uruchamiane raz przed wszystkimi testami – czyści dane i przygotowuje użytkownika testowego.
     */
    @BeforeAll
    static void setUpBeforeClass() {
        cleanUpTestUser(ORIGINAL_EMAIL);
        cleanUpTestUser(UPDATED_EMAIL);

        userDAO = new UserDAO();

        testUser = new User();
        testUser.setName("Test");
        testUser.setLastName("User");
        testUser.setEmail(ORIGINAL_EMAIL);
        testUser.setPassword("hashedpassword");
        testUser.setRoleId(3);
        testUser.setGroupId(1);
        testUser.setPasswordHint("test hint");
    }

    /**
     * Testuje poprawność dodania użytkownika do bazy danych.
     */
    @Test
    @Order(1)
    void testInsertUser() {
        boolean inserted = userDAO.insertUser(testUser);
        assertTrue(inserted, "InsertUser powinno zwrócić true przy prawidłowych danych");

        User insertedUser = userDAO.getUserByEmail(testUser.getEmail());
        assertNotNull(insertedUser, "Wstawiony użytkownik powinien być dostępny w bazie");
        testUser.setId(insertedUser.getId());
        assertTrue(testUser.getId() > 0, "TestUser powinien mieć ustawione ID po insercie");
    }

    /**
     * Testuje poprawność pobierania użytkownika na podstawie e-maila.
     */
    @Test
    @Order(2)
    void testGetUserByEmail_Found() {
        User found = userDAO.getUserByEmail(testUser.getEmail());
        assertNotNull(found, "User powinien zostać znaleziony");
        assertEquals(testUser.getName(), found.getName());
        assertEquals(testUser.getLastName(), found.getLastName());
        assertEquals(testUser.getEmail(), found.getEmail());
        assertEquals(testUser.getRoleId(), found.getRoleId());
        assertEquals(testUser.getGroupId(), found.getGroupId());
    }

    /**
     * Testuje aktualizację danych użytkownika w bazie (email, hasło, podpowiedź hasła).
     */
    @Test
    @Order(3)
    void testUpdateUser() throws SQLException {
        assertTrue(testUser.getId() > 0, "TestUser powinien mieć ustawione ID po insercie.");

        cleanUpTestUser(UPDATED_EMAIL);

        String insertSettingsSql = "INSERT INTO settings (user_id, theme, default_view) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSettingsSql)) {
            stmt.setInt(1, testUser.getId());
            stmt.setString(2, "default_theme");
            stmt.setString(3, "default_view");
            int insertedSettings = stmt.executeUpdate();
            if (insertedSettings == 0) {
                fail("Nie udało się wstawić rekordu do tabeli settings dla testUser.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Nie udało się wstawić rekordu do tabeli settings dla testUser.");
        }

        testUser.setEmail(UPDATED_EMAIL);
        testUser.setPassword("newhashedpassword");
        testUser.setPasswordHint("new hint");

        boolean updated = userDAO.updateUser(testUser);
        assertTrue(updated, "UpdateUser powinno zwrócić true przy udanej aktualizacji");

        User updatedUser = userDAO.getUserByEmail(testUser.getEmail());
        assertNotNull(updatedUser, "Po aktualizacji użytkownik powinien zostać znaleziony");
        assertEquals("newhashedpassword", updatedUser.getPassword());
        assertEquals("new hint", updatedUser.getPasswordHint());
    }

    /**
     * Czyszczenie danych testowych po zakończeniu wszystkich testów.
     */
    @AfterAll
    static void tearDownAfterClass() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0;");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String deleteSettingsSql = "DELETE FROM settings WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSettingsSql)) {
            stmt.setInt(1, testUser.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String deleteUserSql = "DELETE FROM users WHERE email = ? OR email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteUserSql)) {
            stmt.setString(1, ORIGINAL_EMAIL);
            stmt.setString(2, UPDATED_EMAIL);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
