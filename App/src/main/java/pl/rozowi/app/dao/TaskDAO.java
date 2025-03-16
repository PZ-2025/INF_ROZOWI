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
        String sql = "INSERT INTO tasks (name, description, status, start_date, end_date, team, assigned_to) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            if(task.getStartDate() != null && !task.getStartDate().isEmpty()){
                stmt.setDate(4, Date.valueOf(task.getStartDate()));
            } else {
                stmt.setDate(4, null);
            }
            if(task.getEndDate() != null && !task.getEndDate().isEmpty()){
                stmt.setDate(5, Date.valueOf(task.getEndDate()));
            } else {
                stmt.setDate(5, null);
            }
            stmt.setString(6, task.getTeam());
            stmt.setInt(7, task.getAssignedTo());
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
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setTeam(rs.getString("team"));
                task.setAssignedTo(rs.getInt("assigned_to"));
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
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setTeam(rs.getString("team"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    // Pobiera zadania przypisane do danego użytkownika (widok Moje Zadania)
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
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setTeam(rs.getString("team"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }

    // Pobiera zadania kolegów (tej samej drużyny, ale nie przypisane do aktualnego użytkownika)
    public List<Task> getColleagueTasks(int userId, String team) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE team = ? AND assigned_to <> ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, team);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setName(rs.getString("name"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                Date startDate = rs.getDate("start_date");
                task.setStartDate(startDate != null ? startDate.toString() : "");
                Date endDate = rs.getDate("end_date");
                task.setEndDate(endDate != null ? endDate.toString() : "");
                task.setTeam(rs.getString("team"));
                task.setAssignedTo(rs.getInt("assigned_to"));
                tasks.add(task);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tasks;
    }
}
