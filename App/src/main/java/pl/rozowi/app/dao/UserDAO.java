package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

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
            ex.printStackTrace();
        }
        return false;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
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
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(User user) {
        String sqlUser = "UPDATE users SET email = ?, password = ?, password_hint = ? WHERE id = ?";
        String sqlSettings = "UPDATE settings SET theme = ?, default_view = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement stmtSettings = conn.prepareStatement(sqlSettings)) {

            stmtUser.setString(1, user.getEmail());
            stmtUser.setString(2, user.getPassword());
            stmtUser.setString(3, user.getPasswordHint());
            stmtUser.setInt(4, user.getId());

            if (user.getTheme() != null) {
                stmtSettings.setString(1, user.getTheme());
            }
            if (user.getDefaultView() != null) {
                stmtSettings.setString(2, user.getDefaultView());
            }
            stmtSettings.setInt(3, user.getId());

            int affectedUser = stmtUser.executeUpdate();
            int affectedSettings = stmtSettings.executeUpdate();

            return affectedUser > 0 && affectedSettings > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
