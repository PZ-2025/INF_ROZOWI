package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.models.EnhancedTaskActivity;

import java.sql.*;
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
     * Pobiera listę wszystkich aktywności zadań z bazy danych razem z dodatkowymi informacjami o zadaniach i użytkownikach.
     * Ta metoda przeprowadza złączenie z tabelami users i tasks, aby zminimalizować potrzebę późniejszych zapytań.
     *
     * @return Lista aktywności zadań z dodatkowymi informacjami
     */
    public List<TaskActivity> getAllActivitiesWithDetails() {
        List<TaskActivity> activities = new ArrayList<>();

        String sql = "SELECT ta.*, t.title as task_title, u.name as user_name, u.last_name as user_last_name " +
                    "FROM task_activities ta " +
                    "LEFT JOIN tasks t ON ta.task_id = t.id " +
                    "LEFT JOIN users u ON ta.user_id = u.id " +
                    "ORDER BY ta.created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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

    /**
     * Pobiera aktywności z możliwością filtrowania.
     *
     * @param activityType Typ aktywności (może być null, co oznacza wszystkie typy)
     * @param startDate Data początkowa (może być null)
     * @param endDate Data końcowa (może być null)
     * @param userId ID użytkownika (0 oznacza wszystkich użytkowników)
     * @param taskId ID zadania (0 oznacza wszystkie zadania)
     * @return Lista aktywności zadań spełniających kryteria filtrowania
     */
    public List<TaskActivity> getFilteredActivities(String activityType, java.sql.Date startDate,
                                                 java.sql.Date endDate, int userId, int taskId) {
        List<TaskActivity> activities = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT ta.*, t.title as task_title, u.name as user_name, u.last_name as user_last_name " +
            "FROM task_activities ta " +
            "LEFT JOIN tasks t ON ta.task_id = t.id " +
            "LEFT JOIN users u ON ta.user_id = u.id " +
            "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (activityType != null && !activityType.isEmpty()) {
            sqlBuilder.append("AND ta.activity_type = ? ");
            params.add(activityType);
        }

        if (startDate != null) {
            sqlBuilder.append("AND DATE(ta.created_at) >= ? ");
            params.add(startDate);
        }

        if (endDate != null) {
            sqlBuilder.append("AND DATE(ta.created_at) <= ? ");
            params.add(endDate);
        }

        if (userId > 0) {
            sqlBuilder.append("AND ta.user_id = ? ");
            params.add(userId);
        }

        if (taskId > 0) {
            sqlBuilder.append("AND ta.task_id = ? ");
            params.add(taskId);
        }

        sqlBuilder.append("ORDER BY ta.created_at DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return activities;
    }

    /**
     * Gets enhanced task activities with user and task information for better UI display
     */
    public List<EnhancedTaskActivity> getEnhancedActivities() {
        List<EnhancedTaskActivity> enhancedActivities = new ArrayList<>();

        String sql = "SELECT ta.*, t.title as task_title, u.name as user_name, u.last_name as user_last_name, u.email as user_email " +
                     "FROM task_activities ta " +
                     "LEFT JOIN tasks t ON ta.task_id = t.id " +
                     "LEFT JOIN users u ON ta.user_id = u.id " +
                     "ORDER BY ta.created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EnhancedTaskActivity activity = new EnhancedTaskActivity();

                activity.setId(rs.getInt("id"));
                activity.setTaskId(rs.getInt("task_id"));
                activity.setUserId(rs.getInt("user_id"));
                activity.setActivityType(rs.getString("activity_type"));
                activity.setDescription(rs.getString("description"));
                activity.setActivityDate(rs.getTimestamp("created_at"));

                activity.setTaskTitle(rs.getString("task_title"));
                activity.setUserName(rs.getString("user_name"));
                activity.setUserLastName(rs.getString("user_last_name"));
                activity.setUserEmail(rs.getString("user_email"));

                enhancedActivities.add(activity);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return enhancedActivities;
    }

    /**
     * Gets filtered enhanced activities with user and task information
     */
    public List<EnhancedTaskActivity> getFilteredEnhancedActivities(
            String activityType,
            java.sql.Date startDate,
            java.sql.Date endDate,
            int userId,
            int taskId,
            String searchText) {

        List<EnhancedTaskActivity> enhancedActivities = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT ta.*, t.title as task_title, u.name as user_name, u.last_name as user_last_name, u.email as user_email " +
            "FROM task_activities ta " +
            "LEFT JOIN tasks t ON ta.task_id = t.id " +
            "LEFT JOIN users u ON ta.user_id = u.id " +
            "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (activityType != null && !activityType.isEmpty() && !activityType.equals("Wszystkie")) {
            sqlBuilder.append("AND ta.activity_type = ? ");
            params.add(activityType);
        }

        if (startDate != null) {
            sqlBuilder.append("AND DATE(ta.created_at) >= ? ");
            params.add(startDate);
        }

        if (endDate != null) {
            sqlBuilder.append("AND DATE(ta.created_at) <= ? ");
            params.add(endDate);
        }

        if (userId > 0) {
            sqlBuilder.append("AND ta.user_id = ? ");
            params.add(userId);
        }

        if (taskId > 0) {
            sqlBuilder.append("AND ta.task_id = ? ");
            params.add(taskId);
        }

        if (searchText != null && !searchText.isEmpty()) {
            sqlBuilder.append("AND (LOWER(ta.description) LIKE ? OR LOWER(t.title) LIKE ? " +
                             "OR LOWER(u.name) LIKE ? OR LOWER(u.last_name) LIKE ? OR LOWER(u.email) LIKE ?) ");
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sqlBuilder.append("ORDER BY ta.created_at DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EnhancedTaskActivity activity = new EnhancedTaskActivity();

                    activity.setId(rs.getInt("id"));
                    activity.setTaskId(rs.getInt("task_id"));
                    activity.setUserId(rs.getInt("user_id"));
                    activity.setActivityType(rs.getString("activity_type"));
                    activity.setDescription(rs.getString("description"));
                    activity.setActivityDate(rs.getTimestamp("created_at"));

                    activity.setTaskTitle(rs.getString("task_title"));
                    activity.setUserName(rs.getString("user_name"));
                    activity.setUserLastName(rs.getString("user_last_name"));
                    activity.setUserEmail(rs.getString("user_email"));

                    enhancedActivities.add(activity);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return enhancedActivities;
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