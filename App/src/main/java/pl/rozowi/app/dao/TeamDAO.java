package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

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
}
