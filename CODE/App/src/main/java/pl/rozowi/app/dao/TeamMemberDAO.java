package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa dostępu do danych dla członków zespołów.
 * Zawiera metody do zarządzania przypisaniami użytkowników do zespołów.
 */
public class TeamMemberDAO {

    /**
     * Pobiera listę członków określonego zespołu.
     *
     * @param teamId ID zespołu
     * @return Lista obiektów User reprezentujących członków zespołu
     */
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

    /**
     * Pobiera ID zespołu, do którego przypisany jest użytkownik.
     *
     * @param userId ID użytkownika
     * @return ID zespołu lub 0 jeśli użytkownik nie jest przypisany do żadnego zespołu
     */
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
     * Sprawdza czy użytkownik jest liderem zespołu.
     *
     * @param teamId ID zespołu
     * @param userId ID użytkownika
     * @return true jeśli użytkownik jest liderem, false w przeciwnym przypadku
     */
    public boolean isTeamLeader(int teamId, int userId) {
        String sql = "SELECT is_leader FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_leader");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Dodaje użytkownika do zespołu.
     *
     * @param teamId ID zespołu
     * @param userId ID użytkownika
     * @param isLeader flaga określająca czy użytkownik ma być liderem
     * @return true jeśli operacja się powiodła, false w przeciwnym przypadku
     * @throws SQLException w przypadku błędu dostępu do bazy danych
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
     * Usuwa użytkownika z zespołu.
     *
     * @param teamId ID zespołu
     * @param userId ID użytkownika
     * @return true jeśli operacja się powiodła, false w przeciwnym przypadku
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
     * Jeśli użytkownik jest już przypisany do innego zespołu, stare przypisanie jest usuwane.
     *
     * @param userId ID użytkownika
     * @param newTeamId ID nowego zespołu
     * @return true jeśli operacja się powiodła, false w przeciwnym przypadku
     */
    public boolean updateUserTeam(int userId, int newTeamId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int currentTeamId = getTeamIdForUser(userId);

                if (currentTeamId == newTeamId) {
                    return true;
                }

                if (currentTeamId > 0) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM team_members WHERE user_id = ?")) {
                        deleteStmt.setInt(1, userId);
                        deleteStmt.executeUpdate();
                    }
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO team_members (team_id, user_id, is_leader) VALUES (?, ?, ?)")) {
                    insertStmt.setInt(1, newTeamId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setBoolean(3, false);
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

    /**
     * Pobiera listę ID wszystkich zespołów, do których należy użytkownik.
     *
     * @param userId ID użytkownika
     * @return Lista ID zespołów
     */
    public List<Integer> getAllTeamIdsForUser(int userId) {
        List<Integer> teamIds = new ArrayList<>();
        String sql = "SELECT team_id FROM team_members WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                teamIds.add(rs.getInt("team_id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return teamIds;
    }

    /**
     * Pobiera listę ID zespołów, w których użytkownik jest liderem.
     *
     * @param userId ID użytkownika
     * @return Lista ID zespołów
     */
    public List<Integer> getTeamIdsForTeamLeader(int userId) {
        List<Integer> teamIds = new ArrayList<>();
        String sql = "SELECT team_id FROM team_members WHERE user_id = ? AND is_leader = true";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                teamIds.add(rs.getInt("team_id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return teamIds;
    }
}