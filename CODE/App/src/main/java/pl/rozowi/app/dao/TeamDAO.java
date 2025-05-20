package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa dostępu do danych dla zespołów.
 * Zawiera metody do zarządzania zespołami, ich członkami oraz relacjami w systemie.
 */
public class TeamDAO {

    /**
     * Pobiera listę wszystkich zespołów z bazy danych.
     *
     * @return Lista wszystkich zespołów
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
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

    /**
     * Dodaje nowy zespół do bazy danych.
     *
     * @param t Obiekt Team zawierający dane nowego zespołu
     * @return true jeśli operacja się powiodła, false w przeciwnym wypadku
     */
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

    /**
     * Aktualizuje dane zespołu w bazie danych.
     *
     * @param t Obiekt Team zawierający zaktualizowane dane
     * @return true jeśli operacja się powiodła, false w przeciwnym wypadku
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
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

    /**
     * Pobiera listę członków określonego zespołu.
     *
     * @param teamId ID zespołu
     * @return Lista użytkowników będących członkami zespołu
     */
    public List<User> getTeamMembers(int teamId) {
        List<User> members = new ArrayList<>();
        String sql = """
                    SELECT u.id, u.name, u.last_name, u.email, u.role_id, tm.is_leader
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
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setRoleId(rs.getInt("role_id"));
                members.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return members;
    }

    /**
     * Dodaje użytkownika do zespołu.
     *
     * @param teamId ID zespołu
     * @param userId ID użytkownika
     * @param isLeader flaga określająca czy użytkownik jest liderem zespołu
     * @return true jeśli operacja się powiodła, false w przeciwnym wypadku
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
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

    /**
     * Usuwa użytkownika z zespołu.
     *
     * @param teamId ID zespołu
     * @param userId ID użytkownika
     * @return true jeśli operacja się powiodła, false w przeciwnym wypadku
     */
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

    /**
     * Pobiera listę zespołów, do których należy określony użytkownik.
     *
     * @param userId ID użytkownika
     * @return Lista zespołów użytkownika
     */
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

    /**
     * Pobiera nazwę zespołu na podstawie jego ID.
     *
     * @param teamId ID zespołu
     * @return Nazwa zespołu lub "-" jeśli nie znaleziono
     */
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

    /**
     * Pobiera listę zespołów zarządzanych przez określonego menedżera.
     *
     * @param managerId ID menedżera
     * @return Lista zespołów zarządzanych przez menedżera
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
    public List<Team> getTeamsForManager(int managerId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT t.* FROM teams t " +
                "JOIN projects p ON t.project_id = p.id " +
                "WHERE p.manager_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Team team = new Team();
                    team.setId(rs.getInt("id"));
                    team.setTeamName(rs.getString("team_name"));
                    team.setProjectId(rs.getInt("project_id"));
                    teams.add(team);
                }
            }
        }
        return teams;
    }

    /**
     * Pobiera zespół na podstawie jego ID.
     *
     * @param teamId ID zespołu
     * @return Obiekt Team lub null jeśli nie znaleziono
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
    public Team getTeamById(int teamId) throws SQLException {
        String sql = "SELECT id, team_name, project_id FROM teams WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Team team = new Team();
                team.setId(rs.getInt("id"));
                team.setTeamName(rs.getString("team_name"));
                team.setProjectId(rs.getInt("project_id"));
                return team;
            }
        }
        return null;
    }

    /**
     * Pobiera listę ID zespołów, w których określony użytkownik jest liderem.
     *
     * @param leaderId ID użytkownika będącego liderem
     * @return Lista ID zespołów
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
    public List<Integer> getTeamsByLeaderId(int leaderId) throws SQLException {
        List<Integer> teamIds = new ArrayList<>();
        String sql = "SELECT t.id FROM teams t " +
                "JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.user_id = ? AND tm.is_leader = true";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                teamIds.add(rs.getInt("id"));
            }
        }

        return teamIds;
    }

    /**
     * Pobiera listę zespołów, w których określony użytkownik jest liderem.
     *
     * @param leaderId ID użytkownika będącego liderem
     * @return Lista obiektów Team
     * @throws SQLException w przypadku błędu podczas komunikacji z bazą danych
     */
    public List<Team> getTeamsByLeaderIdAsList(int leaderId) throws SQLException {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT t.id, t.team_name, t.project_id FROM teams t " +
                "JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.user_id = ? AND tm.is_leader = true";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, leaderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Team team = new Team();
                team.setId(rs.getInt("id"));
                team.setTeamName(rs.getString("team_name"));
                team.setProjectId(rs.getInt("project_id"));
                teams.add(team);
            }
        }

        return teams;
    }
}