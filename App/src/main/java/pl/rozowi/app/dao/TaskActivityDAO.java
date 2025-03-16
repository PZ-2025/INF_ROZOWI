package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskActivityDAO {

    public boolean insertTaskActivity(TaskActivity activity) {
        String sql = "INSERT INTO task_activity (task_id, user_id, activity_description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, activity.getTaskId());
            stmt.setInt(2, activity.getUserId());
            stmt.setString(3, activity.getActivityDescription());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<TaskActivity> getActivitiesByTaskId(int taskId) {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activity WHERE task_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = new TaskActivity();
                activity.setId(rs.getInt("id"));
                activity.setTaskId(rs.getInt("task_id"));
                activity.setUserId(rs.getInt("user_id"));
                activity.setActivityDescription(rs.getString("activity_description"));
                activity.setActivityDate(rs.getTimestamp("activity_date"));
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }
}
