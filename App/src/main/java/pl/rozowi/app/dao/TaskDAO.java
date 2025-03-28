package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

public class TaskDAO {

    public boolean insertTask(Task task) {
        String sql = "INSERT INTO tasks (project_id, assigned_to, team_id, name, description, status, priority, start_date, end_date, attachment, comment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, task.getProjectId());
            stmt.setInt(2, task.getAssignedTo());
            stmt.setInt(3, task.getTeamId());
            stmt.setString(4, task.getName());
            stmt.setString(5, task.getDescription());
            stmt.setString(6, task.getStatus());
            stmt.setString(7, task.getPriority());
            if (task.getStartDate() != null && !task.getStartDate().isEmpty()) {
                stmt.setDate(8, Date.valueOf(task.getStartDate()));
            } else {
                stmt.setDate(8, null);
            }
            if (task.getEndDate() != null && !task.getEndDate().isEmpty()) {
                stmt.setDate(9, Date.valueOf(task.getEndDate()));
            } else {
                stmt.setDate(9, null);
            }
            stmt.setString(10, task.getAttachment());
            stmt.setString(11, task.getComment());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Task getTaskById(int id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        Task task = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                task.setTeamId(rs.getInt("team_id"));
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setAttachment(rs.getString("attachment"));
                task.setComment(rs.getString("comment"));
                task.setTeam(String.valueOf(rs.getInt("team_id")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return task;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                task.setTeamId(rs.getInt("team_id"));
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setAttachment(rs.getString("attachment"));
                task.setComment(rs.getString("comment"));
                task.setTeam(String.valueOf(rs.getInt("team_id")));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    // zadania przypisane do danego użytkownika (widok Moje Zadania)
    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE assigned_to = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                task.setTeamId(rs.getInt("team_id"));
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setAttachment(rs.getString("attachment"));
                task.setComment(rs.getString("comment"));
                task.setTeam(String.valueOf(rs.getInt("team_id")));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    // Pobiera zadania współpracowników (ten sam team, ale nie przypisane do aktualnego użytkownika)
    public List<Task> getColleagueTasks(int userId, int teamId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE team_id = ? AND assigned_to <> ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setProjectId(rs.getInt("project_id"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                task.setTeamId(rs.getInt("team_id"));
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setPriority(rs.getString("priority"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setAttachment(rs.getString("attachment"));
                task.setComment(rs.getString("comment"));
                task.setTeam(String.valueOf(rs.getInt("team_id")));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }
}
