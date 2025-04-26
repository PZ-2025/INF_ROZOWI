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
        String sql = "SELECT * FROM task_activities WHERE task_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = mapActivityFromResultSet(rs);
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }

    /**
     * Get all task activities from the system, ordered by date (newest first)
     */
    public List<TaskActivity> getAllActivities() {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activities ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TaskActivity activity = mapActivityFromResultSet(rs);
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }

    /**
     * Get activities by user ID
     */
    public List<TaskActivity> getActivitiesByUserId(int userId) {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activities WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = mapActivityFromResultSet(rs);
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }

    /**
     * Get activities by activity type
     */
    public List<TaskActivity> getActivitiesByType(String activityType) {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activities WHERE activity_type = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = mapActivityFromResultSet(rs);
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }

    /**
     * Get activities within a date range
     */
    public List<TaskActivity> getActivitiesByDateRange(java.sql.Date startDate, java.sql.Date endDate) {
        List<TaskActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM task_activities WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TaskActivity activity = mapActivityFromResultSet(rs);
                activities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return activities;
    }

    /**
     * Helper method to map ResultSet to TaskActivity object
     */
    private TaskActivity mapActivityFromResultSet(ResultSet rs) throws SQLException {
        TaskActivity activity = new TaskActivity();
        activity.setId(rs.getInt("id"));
        activity.setTaskId(rs.getInt("task_id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setActivityType(rs.getString("activity_type"));
        activity.setDescription(rs.getString("description"));
        activity.setActivityDate(rs.getTimestamp("created_at"));
        return activity;
    }
}