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
        String sql = "INSERT INTO task_activities (task_id, user_id, activity_type, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, activity.getTaskId());
            stmt.setInt(2, activity.getUserId());
            stmt.setString(3, activity.getActivityType());
            stmt.setString(4, activity.getDescription());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<TaskActivity> getActivitiesByTaskId(int taskId) {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activities WHERE task_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = new TaskActivity();
                activity.setId(rs.getInt("id"));
                activity.setTaskId(rs.getInt("task_id"));
                activity.setUserId(rs.getInt("user_id"));
                activity.setActivityType(rs.getString("activity_type"));
                activity.setDescription(rs.getString("description"));
                activity.setActivityDate(rs.getTimestamp("created_at"));
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }
}
