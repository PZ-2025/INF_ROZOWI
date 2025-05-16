package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

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
     * Pobiera użytkownika z bazy danych na podstawie ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT u.*, s.theme, s.default_view " +
                "FROM users u " +
                "LEFT JOIN settings s ON u.id = s.user_id " +
                "WHERE u.id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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
            LOGGER.log(Level.SEVERE, "Error fetching user by ID", ex);
        }
        return null;
    }

    public boolean updateUser(User user) {
        String sqlUser = "UPDATE users SET email = ?, password = ?, password_hint = ?, name = ?, last_name = ?, role_id = ?, group_id = ? WHERE id = ?";
        String sqlSettings = "INSERT INTO settings (user_id, theme, default_view) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE theme = VALUES(theme), default_view = VALUES(default_view)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
                 PreparedStatement stmtSettings = conn.prepareStatement(sqlSettings)) {

                stmtUser.setString(1, user.getEmail());
                stmtUser.setString(2, user.getPassword());
                stmtUser.setString(3, user.getPasswordHint());
                stmtUser.setString(4, user.getName());
                stmtUser.setString(5, user.getLastName());
                stmtUser.setInt(6, user.getRoleId());
                stmtUser.setInt(7, user.getGroupId());
                stmtUser.setInt(8, user.getId());

                stmtSettings.setInt(1, user.getId());
                if (user.getTheme() != null) {
                    stmtSettings.setString(2, user.getTheme());
                } else {
                    stmtSettings.setNull(2, Types.VARCHAR);
                }
                if (user.getDefaultView() != null) {
                    stmtSettings.setString(3, user.getDefaultView());
                } else {
                    stmtSettings.setNull(3, Types.VARCHAR);
                }

                int affectedUser = stmtUser.executeUpdate();
                int affectedSettings = stmtSettings.executeUpdate();

                conn.commit();
                return affectedUser > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating user", ex);
        }
        return false;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setRoleId(rs.getInt("role_id"));
                u.setGroupId(rs.getInt("group_id"));
                u.setPasswordHint(rs.getString("password_hint"));
                list.add(u);
            }
        }
        return list;
    }

    public List<User> getAllManagers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, last_name, email, role_id " +
                "FROM users " +
                "WHERE role_id = 2";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setRoleId(rs.getInt("role_id"));
                list.add(u);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Pobiera mapę wszystkich ról (ID -> nazwa roli)
     */
    public Map<Integer, String> getAllRolesMap() {
        Map<Integer, String> roles = new HashMap<>();
        String sql = "SELECT id, role_name FROM roles";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                roles.put(rs.getInt("id"), rs.getString("role_name"));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching roles", ex);
        }
        return roles;
    }

    /**
     * Pobiera mapę wszystkich grup (ID -> nazwa grupy)
     */
    public Map<Integer, String> getAllGroupsMap() {
        Map<Integer, String> groups = new HashMap<>();
        String sql = "SELECT id, group_name FROM groups";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                groups.put(rs.getInt("id"), rs.getString("group_name"));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching groups", ex);
        }
        return groups;
    }

    /**
     * Usuwa użytkownika z bazy danych
     */
    public boolean deleteUser(int userId) {
        String sqlSettings = "DELETE FROM settings WHERE user_id = ?";

        String sqlUser = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtSettings = conn.prepareStatement(sqlSettings);
                 PreparedStatement stmtUser = conn.prepareStatement(sqlUser)) {

                stmtSettings.setInt(1, userId);
                stmtSettings.executeUpdate();

                stmtUser.setInt(1, userId);
                int affectedRows = stmtUser.executeUpdate();

                conn.commit();
                return affectedRows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting user", ex);
        }
        return false;
    }

    public List<String> getAllGroupNames() {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT DISTINCT group_name FROM groups ORDER BY group_name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<Integer> getUsersByGroupName(String groupName) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT id FROM users WHERE group_id = " +
                "(SELECT id FROM groups WHERE group_name = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }
}