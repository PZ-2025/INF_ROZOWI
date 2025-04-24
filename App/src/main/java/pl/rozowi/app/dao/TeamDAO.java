package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

    public List<Team> getAllTeams() throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT id, team_name, project_id FROM teams";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                Team t = new Team();
                t.setId(rs.getInt("id"));
                t.setTeamName(rs.getString("team_name"));
                t.setProjectId(rs.getInt("project_id"));
                list.add(t);
            }
        }
        return list;
    }

    public boolean insertTeam(Team t) {
        String sql = "INSERT INTO teams (team_name, project_id) VALUES (?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, t.getTeamName());
            s.setInt(2, t.getProjectId());
            if (s.executeUpdate() == 0) return false;
            ResultSet keys = s.getGeneratedKeys();
            if (keys.next()) t.setId(keys.getInt(1));
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateTeam(Team t) throws SQLException {
        String sql = "UPDATE teams SET team_name = ?, project_id = ? WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, t.getTeamName());
            s.setInt(2, t.getProjectId());
            s.setInt(3, t.getId());
            return s.executeUpdate() > 0;
        }
    }

    public List<User> getTeamMembers(int teamId) {
        List<User> members = new ArrayList<>();
        String sql = """
                    SELECT u.id, u.name, u.email
                      FROM users u
                      JOIN team_members tm ON u.id = tm.user_id
                     WHERE tm.team_id = ?
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, teamId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                members.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return members;
    }

    public boolean insertTeamMember(int teamId, int userId, boolean isLeader) throws SQLException {
        String sql = "INSERT INTO team_members (team_id, user_id, is_leader) VALUES (?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, teamId);
            s.setInt(2, userId);
            s.setBoolean(3, isLeader);
            return s.executeUpdate() > 0;
        }
    }

    public boolean deleteTeamMember(int teamId, int userId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, teamId);
            s.setInt(2, userId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Team> getTeamsForUser(int userId) {
        List<Team> teams = new ArrayList<>();
        String sql = """
                    SELECT t.id, t.team_name, t.project_id
                      FROM teams t
                      JOIN team_members tm ON t.id = tm.team_id
                     WHERE tm.user_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Team t = new Team();
                    t.setId(rs.getInt("id"));
                    t.setTeamName(rs.getString("team_name"));
                    t.setProjectId(rs.getInt("project_id"));
                    teams.add(t);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return teams;
    }

    public String getTeamNameById(int teamId) {
        String sql = "SELECT team_name FROM teams WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("team_name");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "–";
    }
}
