package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeamMemberDAO {

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
