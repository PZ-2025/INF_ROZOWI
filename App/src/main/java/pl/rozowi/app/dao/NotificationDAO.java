package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public List<Notification> getNotificationsForUser(int userId) {
        List<Notification> notifications = new ArrayList<>();

        // Pobieramy nieprzeczytane powiadomienia
        String selectSql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = false ORDER BY date DESC";

        // Zapytanie do oznaczenia powiadomień jako przeczytane
        String updateSql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            selectStmt.setInt(1, userId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));

                    int taskId = rs.getInt("task_id");
                    n.setTaskId(rs.wasNull() ? null : taskId);

                    n.setNotificationType(rs.getString("notification_type"));
                    n.setDescription(rs.getString("description"));
                    n.setDate(rs.getTimestamp("date"));
                    n.setRead(false); // Zawsze false, bo pobieramy nieprzeczytane

                    notifications.add(n);
                }
            }

            // Oznaczanie wszystkich pobranych powiadomień jako przeczytane
            if (!notifications.isEmpty()) {
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return notifications;
    }
}