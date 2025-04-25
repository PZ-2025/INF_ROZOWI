package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setDescription(rs.getString("content"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setRead(rs.getBoolean("is_read"));
        return n;
    }

    private List<Notification> fetch(String sql, Object... params) {
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Notification> getAll(int userId) {
        String sql = """
                SELECT id, user_id, content, created_at, is_read
                  FROM notifications
                 WHERE user_id = ?
                 ORDER BY created_at DESC
                """;
        return fetch(sql, userId);
    }

    public List<Notification> getUnread(int userId) {
        String sql = """
                SELECT id, user_id, content, created_at, is_read
                  FROM notifications
                 WHERE user_id = ? AND is_read = FALSE
                 ORDER BY created_at DESC
                """;
        return fetch(sql, userId);
    }

    public List<Notification> getRead(int userId) {
        String sql = """
                SELECT id, user_id, content, created_at, is_read
                  FROM notifications
                 WHERE user_id = ? AND is_read = TRUE
                 ORDER BY created_at DESC
                """;
        return fetch(sql, userId);
    }

    public boolean updateReadStatus(int notificationId, boolean isRead) {
        String sql = "UPDATE notifications SET is_read = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isRead);
            ps.setInt(2, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
