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

    /**
     * Wstawia nowy rekord członka zespołu
     */
    public boolean insertTeamMember(int teamId, int userId, boolean isLeader) throws SQLException {
        String sql = "INSERT INTO team_members (team_id, user_id, is_leader) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            stmt.setBoolean(3, isLeader);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * Usuwa powiązanie użytkownika z zespołem
     */
    public boolean deleteTeamMember(int teamId, int userId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Aktualizuje przypisanie użytkownika do zespołu.
     * Jeśli użytkownik jest już przypisany do innego zespołu, najpierw usuwa to przypisanie.
     */
    public boolean updateUserTeam(int userId, int newTeamId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Sprawdź, czy użytkownik jest już przypisany do jakiegoś zespołu
                int currentTeamId = getTeamIdForUser(userId);

                // Jeśli jest przypisany do tego samego zespołu, nic nie rób
                if (currentTeamId == newTeamId) {
                    return true;
                }

                // Jeśli jest przypisany do innego zespołu, usuń to przypisanie
                if (currentTeamId > 0) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM team_members WHERE user_id = ?")) {
                        deleteStmt.setInt(1, userId);
                        deleteStmt.executeUpdate();
                    }
                }

                // Dodaj nowe przypisanie
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO team_members (team_id, user_id, is_leader) VALUES (?, ?, ?)")) {
                    insertStmt.setInt(1, newTeamId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setBoolean(3, false); // Domyślnie nie jest liderem
                    insertStmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}