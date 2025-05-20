package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.models.EnhancedTaskActivity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object dla tabeli aktywności zadań.
 * Odpowiada za operacje związane z logowaniem działań użytkowników na zadaniach, takie jak:
 * <ul>
 *   <li>Rejestrowanie nowych aktywności</li>
 *   <li>Pobieranie aktywności wg zadania, użytkownika, typu lub daty</li>
 *   <li>Pobieranie rozszerzonych informacji o aktywnościach z dodatkowymi danymi</li>
 *   <li>Filtrowanie aktywności według wielu kryteriów</li>
 * </ul>
 */
public class TaskActivityDAO {

    /**
     * Wstawia nową aktywność związaną z zadaniem do bazy danych.
     *
     * @param activity Obiekt TaskActivity zawierający szczegóły aktywności
     * @return true jeśli wstawienie się powiodło, false w przypadku błędu
     */
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

    /**
     * Pobiera wszystkie aktywności dla danego zadania posortowane malejąco według daty.
     *
     * @param taskId ID zadania, dla którego mają być pobrane aktywności
     * @return Lista obiektów TaskActivity powiązanych z zadaniem
     */
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
     * Pobiera wszystkie aktywności zarejestrowane w systemie, uporządkowane od najnowszych.
     *
     * @return Lista wszystkich aktywności TaskActivity
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
     * Pobiera aktywności wykonane przez określonego użytkownika.
     *
     * @param userId ID użytkownika, którego aktywności mają zostać pobrane
     * @return Lista aktywności wykonanych przez użytkownika
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
     * Pobiera aktywności według typu (np. "utworzono", "edytowano", "usunięto").
     *
     * @param activityType Typ aktywności
     * @return Lista aktywności danego typu
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
     * Pobiera aktywności, które miały miejsce w określonym przedziale czasowym.
     *
     * @param startDate Data początkowa przedziału
     * @param endDate Data końcowa przedziału
     * @return Lista aktywności z określonego zakresu dat
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
     * Pobiera wszystkie aktywności wraz z dodatkowymi informacjami o użytkownikach i zadaniach.
     *
     * @return Lista aktywności z rozszerzonymi danymi (nazwa zadania, imię i nazwisko użytkownika)
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
     * Pobiera aktywności według podanych filtrów:
     * typ aktywności, zakres dat, użytkownik oraz zadanie.
     *
     * @param activityType Typ aktywności (może być null)
     * @param startDate Data początkowa (może być null)
     * @param endDate Data końcowa (może być null)
     * @param userId ID użytkownika (0 oznacza wszystkich)
     * @param taskId ID zadania (0 oznacza wszystkie)
     * @return Lista aktywności spełniających warunki filtrów
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
     * Pobiera rozszerzoną wersję aktywności z dodatkowymi danymi użytkownika i zadania.
     * Używane głównie do wyświetlania informacji w interfejsie użytkownika.
     *
     * @return Lista rozszerzonych aktywności EnhancedTaskActivity
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
     * Pobiera przefiltrowane rozszerzone aktywności na podstawie:
     * typu, zakresu dat, użytkownika, zadania i wyszukiwanej frazy.
     *
     * @param activityType Typ aktywności (może być null)
     * @param startDate Data początkowa (może być null)
     * @param endDate Data końcowa (może być null)
     * @param userId ID użytkownika (0 oznacza wszystkich)
     * @param taskId ID zadania (0 oznacza wszystkie)
     * @param searchText Fraza do przeszukania (np. opis, nazwa zadania, email)
     * @return Lista rozszerzonych aktywności spełniających warunki filtrów
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
     * Metoda pomocnicza mapująca dane z ResultSet na obiekt TaskActivity.
     *
     * @param rs obiekt ResultSet zawierający dane z bazy danych
     * @return obiekt TaskActivity wypełniony danymi z ResultSet
     * @throws SQLException jeśli wystąpi błąd podczas odczytu danych z ResultSet
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