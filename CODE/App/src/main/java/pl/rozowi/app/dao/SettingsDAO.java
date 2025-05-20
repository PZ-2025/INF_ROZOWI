package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Settings;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) dla tabeli ustawień.
 * Zapewnia operacje bazodanowe związane z ustawieniami użytkowników, w tym:
 * <ul>
 *   <li>Pobieranie ustawień użytkowników</li>
 *   <li>Aktualizację i wstawianie ustawień</li>
 *   <li>Zarządzanie znacznikami czasu zmiany hasła</li>
 *   <li>Obsługę preferencji motywów i widoków</li>
 * </ul>
 */
public class SettingsDAO {

    private static final Logger LOGGER = Logger.getLogger(SettingsDAO.class.getName());

    /**
     * Pobiera ustawienia dla określonego użytkownika z bazy danych.
     *
     * @param userId ID użytkownika, którego ustawienia mają zostać pobrane
     * @return Obiekt Settings zawierający wszystkie preferencje użytkownika jeśli znaleziono,
     *         null jeśli nie istnieją ustawienia dla użytkownika
     */
    public Settings getSettingsByUserId(int userId) {
        String sql = "SELECT id, user_id, theme, default_view, last_password_change FROM settings WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Settings settings = new Settings();
                    settings.setId(rs.getInt("id"));
                    settings.setUserId(rs.getInt("user_id"));
                    settings.setTheme(rs.getString("theme"));
                    settings.setDefaultView(rs.getString("default_view"));
                    settings.setLastPasswordChange(rs.getTimestamp("last_password_change"));
                    return settings;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving settings for user ID: " + userId, ex);
        }

        return null;
    }

    /**
     * Aktualizuje znacznik czasu ostatniej zmiany hasła dla użytkownika.
     * Tworzy nowy rekord ustawień jeśli nie istnieje dla użytkownika.
     *
     * @param userId ID użytkownika, którego znacznik czasu powinien zostać zaktualizowany
     * @return true jeśli operacja się powiodła, false w przypadku błędu
     */
    public boolean updateLastPasswordChange(int userId) {
        Settings existingSettings = getSettingsByUserId(userId);

        if (existingSettings != null) {
            String sql = "UPDATE settings SET last_password_change = CURRENT_TIMESTAMP WHERE user_id = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                int affected = stmt.executeUpdate();
                return affected > 0;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error updating last password change for user ID: " + userId, ex);
                return false;
            }
        } else {
            String sql = "INSERT INTO settings (user_id, last_password_change) VALUES (?, CURRENT_TIMESTAMP)";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);
                int affected = stmt.executeUpdate();
                return affected > 0;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error creating settings with last password change for user ID: " + userId, ex);
                return false;
            }
        }
    }

    /**
     * Wstawia nowy rekord ustawień dla użytkownika ze wszystkimi dostępnymi preferencjami.
     * Poprawnie obsługuje wartości null dla pól opcjonalnych.
     *
     * @param settings Obiekt Settings zawierający wszystkie preferencje użytkownika do wstawienia
     * @return true jeśli wstawienie się powiodło, false w przypadku błędu
     */
    public boolean insertSettings(Settings settings) {
        String sql = "INSERT INTO settings (user_id, theme, default_view, last_password_change) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, settings.getUserId());

            if (settings.getTheme() != null) {
                stmt.setString(2, settings.getTheme());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            if (settings.getDefaultView() != null) {
                stmt.setString(3, settings.getDefaultView());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            if (settings.getLastPasswordChange() != null) {
                stmt.setTimestamp(4, settings.getLastPasswordChange());
            } else {
                stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting settings for user ID: " + settings.getUserId(), ex);
            return false;
        }
    }

    /**
     * Aktualizuje istniejące ustawienia dla użytkownika.
     *
     * @param settings Obiekt Settings z zaktualizowanymi wartościami
     * @return true jeśli aktualizacja się powiodła, false w przypadku błędu
     */
    public boolean updateSettings(Settings settings) {
        String sql = "UPDATE settings SET theme = ?, default_view = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (settings.getTheme() != null) {
                stmt.setString(1, settings.getTheme());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }

            if (settings.getDefaultView() != null) {
                stmt.setString(2, settings.getDefaultView());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setInt(3, settings.getUserId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating settings for user ID: " + settings.getUserId(), ex);
            return false;
        }
    }

    /**
     * Usuwa ustawienia dla określonego użytkownika.
     *
     * @param userId ID użytkownika, którego ustawienia mają zostać usunięte
     * @return true jeśli usunięcie się powiodło, false w przypadku błędu
     */
    public boolean deleteSettings(int userId) {
        String sql = "DELETE FROM settings WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting settings for user ID: " + userId, ex);
            return false;
        }
    }

    /**
     * Aktualizuje domyślny widok dla użytkownika.
     *
     * @param userId ID użytkownika
     * @param defaultView Nazwa domyślnego widoku do ustawienia
     * @return true jeśli aktualizacja się powiodła, false w przypadku błędu
     */
    public boolean updateDefaultView(int userId, String defaultView) {
        String sql = "UPDATE settings SET default_view = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, defaultView);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            Logger.getLogger(SettingsDAO.class.getName())
                    .log(Level.SEVERE, "Błąd aktualizacji widoku domyślnego", ex);
            return false;
        }
    }
}