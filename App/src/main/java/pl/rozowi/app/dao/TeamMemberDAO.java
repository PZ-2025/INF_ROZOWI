package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;

import pl.rozowi.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamMemberDAO {

    public List<User> getTeamMembers(int teamId) {
        String sql = """
                SELECT u.id, u.name, u.last_name, u.email
                FROM team_members tm
                JOIN users u ON u.id = tm.user_id
                WHERE tm.team_id = ?
                """;
        List<User> members = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                members.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public int getTeamIdForUser(int userId) {
        String sql = "SELECT team_id FROM team_members WHERE user_id = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("team_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}
