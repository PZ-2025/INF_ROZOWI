package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Wstawia nowego użytkownika do bazy.
     * @param user obiekt User zawierający dane do rejestracji
     * @return true, jeśli wstawienie przebiegło pomyślnie, w przeciwnym razie false
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (name, last_name, password, email, role_id, group_id, password_hint) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setInt(5, user.getRoleId());
            stmt.setInt(6, user.getGroupId());
            stmt.setString(7, user.getPasswordHint());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting user", ex);
        }
        return false;
    }


    public User getUserByEmail(String email) {
        String sql = "SELECT u.*, s.theme, s.default_view " +
                "FROM users u " +
                "LEFT JOIN settings s ON u.id = s.user_id " +
                "WHERE u.email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setRoleId(rs.getInt("role_id"));
                    user.setGroupId(rs.getInt("group_id"));
                    user.setPasswordHint(rs.getString("password_hint"));

                    user.setTheme(rs.getString("theme"));
                    user.setDefaultView(rs.getString("default_view"));
                    return user;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching user by email", ex);
        }
        return null;
    }


    /**
     * Aktualizuje dane użytkownika oraz powiązane ustawienia.
     * @param user obiekt User z danymi do aktualizacji
     * @return true, jeśli aktualizacja przebiegła pomyślnie, w przeciwnym razie false
     */
    public boolean updateUser(User user) {
        String sqlUser = "UPDATE users SET email = ?, password = ?, password_hint = ? WHERE id = ?";
        String sqlSettings = "UPDATE settings SET theme = ?, default_view = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement stmtSettings = conn.prepareStatement(sqlSettings)) {

            // Aktualizacja danych użytkownika
            stmtUser.setString(1, user.getEmail());
            stmtUser.setString(2, user.getPassword());
            stmtUser.setString(3, user.getPasswordHint());
            stmtUser.setInt(4, user.getId());

            // Aktualizacja ustawień użytkownika (jeśli występują)
            if (user.getTheme() != null) {
                stmtSettings.setString(1, user.getTheme());
            } else {
                stmtSettings.setNull(1, Types.VARCHAR);
            }
            if (user.getDefaultView() != null) {
                stmtSettings.setString(2, user.getDefaultView());
            } else {
                stmtSettings.setNull(2, Types.VARCHAR);
            }
            stmtSettings.setInt(3, user.getId());

            int affectedUser = stmtUser.executeUpdate();
            int affectedSettings = stmtSettings.executeUpdate();

            return affectedUser > 0 && affectedSettings > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating user", ex);
        }
        return false;
    }
}
