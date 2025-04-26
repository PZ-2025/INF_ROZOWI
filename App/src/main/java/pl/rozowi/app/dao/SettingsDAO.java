package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Settings;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for the Settings table.
 * Handles database operations related to user settings.
 */
public class SettingsDAO {

    private static final Logger LOGGER = Logger.getLogger(SettingsDAO.class.getName());

    /**
     * Retrieves settings for a specific user.
     *
     * @param userId The ID of the user whose settings are to be retrieved
     * @return The Settings object if found, null otherwise
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
     * Updates the last password change timestamp for a user.
     * If no settings record exists for the user, creates a new one.
     *
     * @param userId The ID of the user whose password change timestamp should be updated
     * @return true if the operation was successful, false otherwise
     */
    public boolean updateLastPasswordChange(int userId) {
        // First check if settings record exists for this user
        Settings existingSettings = getSettingsByUserId(userId);

        if (existingSettings != null) {
            // Update existing record
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
            // Insert new record
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
     * Inserts a new settings record for a user.
     *
     * @param settings The Settings object to insert
     * @return true if the insertion was successful, false otherwise
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
     * Updates existing settings for a user.
     *
     * @param settings The Settings object with updated values
     * @return true if the update was successful, false otherwise
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
     * Deletes settings for a specific user.
     *
     * @param userId The ID of the user whose settings are to be deleted
     * @return true if the deletion was successful, false otherwise
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
}