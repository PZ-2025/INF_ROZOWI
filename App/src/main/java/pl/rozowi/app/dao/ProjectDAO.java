package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Project;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public boolean insertProject(Project p) throws SQLException {
        String sql = "INSERT INTO projects (project_name, description, start_date, end_date, manager_id, team_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, p.getName());
            s.setString(2, p.getDescription());
            s.setDate(3, Date.valueOf(p.getStartDate()));
            s.setDate(4, Date.valueOf(p.getEndDate()));
            s.setInt(5, p.getManagerId());
            s.setInt(6, p.getTeamId());
            if (s.executeUpdate() == 0) return false;
            ResultSet k = s.getGeneratedKeys();
            if (k.next()) p.setId(k.getInt(1));
            return true;
        }
    }

    public boolean updateProject(Project p) {
        String sql = "UPDATE projects SET project_name=?, description=?, start_date=?, end_date=?, manager_id=?, team_id=? WHERE id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, p.getName());
            s.setString(2, p.getDescription());
            s.setDate(3, Date.valueOf(p.getStartDate()));
            s.setDate(4, Date.valueOf(p.getEndDate()));
            s.setInt(5, p.getManagerId());
            s.setInt(6, p.getTeamId());
            s.setInt(7, p.getId());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteProject(int projectId) {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, projectId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT id, project_name, description, start_date, end_date, manager_id, team_id FROM projects";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setProjectName(rs.getString("project_name"));
                p.setDescription(rs.getString("description"));
                p.setStartDate(LocalDate.parse(rs.getString("start_date")));
                p.setEndDate(LocalDate.parse(rs.getString("end_date")));
                p.setManagerId(rs.getInt("manager_id"));
                p.setTeamId(rs.getInt("team_id"));
                list.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Project> getProjectsForManager(int managerId) {
        List<Project> list = new ArrayList<>();
        String sql = """
                    SELECT id, project_name, description, start_date, end_date, manager_id, team_id
                      FROM projects
                     WHERE manager_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project();
                    p.setId(rs.getInt("id"));
                    p.setProjectName(rs.getString("project_name"));
                    p.setDescription(rs.getString("description"));
                    p.setStartDate(LocalDate.parse(rs.getString("start_date")));
                    p.setEndDate(LocalDate.parse(rs.getString("end_date")));
                    p.setManagerId(rs.getInt("manager_id"));
                    p.setTeamId(rs.getInt("team_id"));
                    list.add(p);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

public Project getProjectById(int projectId) {
        String sql = "SELECT id, project_name, description, start_date, end_date, manager_id, team_id FROM projects WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Project p = new Project();
                    p.setId(rs.getInt("id"));
                    p.setProjectName(rs.getString("project_name"));
                    p.setDescription(rs.getString("description"));
                    p.setStartDate(LocalDate.parse(rs.getString("start_date")));
                    p.setEndDate(LocalDate.parse(rs.getString("end_date")));
                    p.setManagerId(rs.getInt("manager_id"));
                    p.setTeamId(rs.getInt("team_id"));
                    return p;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Project> getProjectsForTeam(int teamId) {
        List<Project> list = new ArrayList<>();
        String sql = """
                    SELECT id, project_name, description, start_date, end_date, manager_id, team_id
                      FROM projects
                     WHERE team_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Project p = new Project();
                    p.setId(rs.getInt("id"));
                    p.setProjectName(rs.getString("project_name"));
                    p.setDescription(rs.getString("description"));
                    p.setStartDate(LocalDate.parse(rs.getString("start_date")));
                    p.setEndDate(LocalDate.parse(rs.getString("end_date")));
                    p.setManagerId(rs.getInt("manager_id"));
                    p.setTeamId(rs.getInt("team_id"));
                    list.add(p);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}