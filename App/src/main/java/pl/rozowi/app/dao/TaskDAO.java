package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

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
}
