package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getTasksByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = """
                    SELECT id,
                           project_id,
                           team_id,
                           title,
                           description,
                           status,
                           priority,
                           start_date,
                           end_date
                      FROM tasks
                     WHERE project_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getInt("id"));
                    t.setProjectId(rs.getInt("project_id"));
                    t.setTeamId(rs.getInt("team_id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setStatus(rs.getString("status"));
                    t.setPriority(rs.getString("priority"));
                    t.setStartDate(rs.getString("start_date"));
                    t.setEndDate(rs.getString("end_date"));
                    tasks.add(t);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getTasksByTeamId(int teamId) {
        List<Task> tasks = new ArrayList<>();
        String sql = """
                    SELECT t.id,
                           t.project_id,
                           t.team_id,
                           t.title,
                           t.description,
                           t.status,
                           t.priority,
                           t.start_date,
                           t.end_date,
                           u.email           AS assigned_email,
                           teams.team_name   AS team_name
                      FROM tasks t
                      JOIN task_assignments ta ON t.id = ta.task_id
                      JOIN users u             ON ta.user_id = u.id
                      JOIN teams               ON t.team_id = teams.id
                     WHERE t.team_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task t = new Task();
                    t.setId(rs.getInt("id"));
                    t.setProjectId(rs.getInt("project_id"));
                    t.setTeamId(rs.getInt("team_id"));
                    t.setTitle(rs.getString("title"));
                    t.setDescription(rs.getString("description"));
                    t.setStatus(rs.getString("status"));
                    t.setPriority(rs.getString("priority"));
                    t.setStartDate(rs.getString("start_date"));
                    t.setEndDate(rs.getString("end_date"));
                    t.setAssignedEmail(rs.getString("assigned_email"));
                    t.setTeamName(rs.getString("team_name"));
                    tasks.add(t);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getTasksForLeader(int leaderId) {
        List<Task> tasks = new ArrayList<>();
        String sql =
                "SELECT t.id,\n" +
                        "       t.project_id,\n" +
                        "       t.team_id,\n" +
                        "       ta.user_id           AS assigned_to,\n" +
                        "       t.title,\n" +
                        "       t.description,\n" +
                        "       t.status,\n" +
                        "       t.priority,\n" +
                        "       t.start_date,\n" +
                        "       t.end_date,\n" +
                        "       teams.team_name      AS team_name,\n" +
                        "       u.email              AS assigned_email\n" +
                        "FROM task_assignments ta\n" +
                        "JOIN tasks t ON ta.task_id = t.id\n" +
                        "JOIN team_members tm ON t.team_id = tm.team_id\n" +
                        "     AND tm.user_id = ? AND tm.is_leader = TRUE\n" +
                        "JOIN teams ON t.team_id = teams.id\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "ORDER BY t.id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, leaderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("id"));
                    task.setProjectId(rs.getInt("project_id"));
                    task.setTeamId(rs.getInt("team_id"));
                    task.setAssignedTo(rs.getInt("assigned_to"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setPriority(rs.getString("priority"));
                    task.setStartDate(rs.getString("start_date"));
                    task.setEndDate(rs.getString("end_date"));
                    task.setTeamName(rs.getString("team_name"));
                    task.setAssignedEmail(rs.getString("assigned_email"));
                    tasks.add(task);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    public boolean insertTask(Task task) {
        String sql =
                "INSERT INTO tasks (project_id, team_id, title, description, status, priority, start_date, end_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, task.getProjectId());
            stmt.setInt(2, task.getTeamId());
            stmt.setString(3, task.getTitle());
            stmt.setString(4, task.getDescription());
            stmt.setString(5, task.getStatus());
            stmt.setString(6, task.getPriority());
            stmt.setString(7, task.getStartDate());
            stmt.setString(8, task.getEndDate());

            int affected = stmt.executeUpdate();
            if (affected == 0) return false;

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateTaskStatus(int taskId, String newStatus) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean assignTask(int taskId, int userId) {
        // usuwamy poprzednie assignment i wstawiamy nowe
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM task_assignments WHERE task_id = ?");
                 PreparedStatement ins = conn.prepareStatement(
                         "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)")) {
                del.setInt(1, taskId);
                del.executeUpdate();
                ins.setInt(1, taskId);
                ins.setInt(2, userId);
                ins.executeUpdate();
                conn.commit();
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t " +
                "JOIN task_assignments ta ON ta.task_id = t.id " +
                "WHERE ta.user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setTeamId(rs.getInt("team_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                task.setStartDate(rs.getString("start_date"));
                task.setEndDate(rs.getString("end_date"));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    private String getTeamNameById(int teamId) {
        String sql = "SELECT team_name FROM teams WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("team_name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Task> getColleagueTasks(int userId, int teamId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t " +
                "JOIN task_assignments ta ON ta.task_id = t.id " +
                "WHERE t.team_id = ? AND ta.user_id != ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setTeamId(rs.getInt("team_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                task.setStartDate(rs.getString("start_date"));
                task.setEndDate(rs.getString("end_date"));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getStatus());
            stmt.setInt(2, task.getId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
