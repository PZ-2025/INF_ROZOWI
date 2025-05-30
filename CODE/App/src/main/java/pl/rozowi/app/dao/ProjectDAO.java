package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Project;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) dla zarządzania projektami w bazie danych.
 * Zapewnia podstawowe operacje CRUD oraz dodatkowe metody do zarządzania projektami.
 */
public class ProjectDAO {

    /**
     * Dodaje nowy projekt do bazy danych.
     *
     * @param p Obiekt projektu do dodania
     * @return true jeśli projekt został pomyślnie dodany, false w przeciwnym przypadku
     * @throws SQLException jeśli wystąpi błąd podczas operacji na bazie danych
     */
    public boolean insertProject(Project p) throws SQLException {
        String sql = "INSERT INTO projects (project_name, description, start_date, end_date, manager_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, p.getName());
            s.setString(2, p.getDescription());
            s.setDate(3, Date.valueOf(p.getStartDate()));
            s.setDate(4, Date.valueOf(p.getEndDate()));
            s.setInt(5, p.getManagerId());
            if (s.executeUpdate() == 0) return false;
            ResultSet k = s.getGeneratedKeys();
            if (k.next()) p.setId(k.getInt(1));
            return true;
        }
    }

    /**
     * Aktualizuje istniejący projekt w bazie danych.
     *
     * @param p Obiekt projektu z zaktualizowanymi danymi
     * @return true jeśli projekt został pomyślnie zaktualizowany, false w przeciwnym przypadku
     * @throws RuntimeException jeśli wystąpi błąd SQL
     */
    public boolean updateProject(Project p) {
        String sql = "UPDATE projects SET project_name=?, description=?, start_date=?, end_date=?, manager_id=? WHERE id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, p.getName());
            s.setString(2, p.getDescription());
            s.setDate(3, Date.valueOf(p.getStartDate()));
            s.setDate(4, Date.valueOf(p.getEndDate()));
            s.setInt(5, p.getManagerId());
            s.setInt(6, p.getId());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pobiera listę wszystkich projektów z bazy danych.
     *
     * @return Lista wszystkich projektów, pusta lista jeśli nie ma projektów
     */
    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT id, project_name, description, start_date, end_date, manager_id FROM projects";
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
                list.add(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Pobiera listę projektów przypisanych do określonego menedżera.
     *
     * @param managerId ID menedżera
     * @return Lista projektów menedżera, pusta lista jeśli nie ma projektów
     */
    public List<Project> getProjectsForManager(int managerId) {
        List<Project> list = new ArrayList<>();
        String sql = """
                    SELECT id, project_name, description, start_date, end_date, manager_id
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
                    list.add(p);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Pobiera nazwę projektu na podstawie ID.
     *
     * @param projectId ID projektu
     * @return Nazwa projektu lub null jeśli projekt nie istnieje
     * @throws RuntimeException jeśli wystąpi błąd podczas pobierania danych
     */
    public String getProjectNameById(int projectId) {
        String sql = "SELECT project_name FROM projects WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("project_name");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch project name by ID", e);
        }
        return null;
    }

    /**
     * Usuwa projekt wraz z wszystkimi powiązanymi encjami (zadania, zespoły itp.).
     * Operacja wykonuje się w transakcji - w przypadku błędu następuje rollback.
     *
     * @param projectId ID projektu do usunięcia
     * @return true jeśli projekt został pomyślnie usunięty, false w przeciwnym przypadku
     */
    public boolean deleteProject(int projectId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {

                List<Integer> taskIds = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM tasks WHERE project_id = ?")) {
                    stmt.setInt(1, projectId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        taskIds.add(rs.getInt("id"));
                    }
                }

                List<Integer> teamIds = new ArrayList<>();
                try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM teams WHERE project_id = ?")) {
                    stmt.setInt(1, projectId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        teamIds.add(rs.getInt("id"));
                    }
                }

                boolean taskAssignmentsExists = false;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SHOW TABLES LIKE 'task_assignments'")) {
                    ResultSet rs = stmt.executeQuery();
                    taskAssignmentsExists = rs.next();
                }

                if (taskAssignmentsExists && !taskIds.isEmpty()) {
                    StringBuilder questionMarks = new StringBuilder();
                    for (int i = 0; i < taskIds.size(); i++) {
                        if (i > 0) {
                            questionMarks.append(",");
                        }
                        questionMarks.append("?");
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM task_assignments WHERE task_id IN (" + questionMarks.toString() + ")")) {
                        for (int i = 0; i < taskIds.size(); i++) {
                            stmt.setInt(i + 1, taskIds.get(i));
                        }
                        stmt.executeUpdate();
                    }
                }

                boolean taskActivityExists = false;
                boolean taskActivitiesExists = false;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SHOW TABLES LIKE 'task_activity'")) {
                    ResultSet rs = stmt.executeQuery();
                    taskActivityExists = rs.next();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SHOW TABLES LIKE 'task_activities'")) {
                    ResultSet rs = stmt.executeQuery();
                    taskActivitiesExists = rs.next();
                }

                if (!taskIds.isEmpty()) {
                    StringBuilder questionMarks = new StringBuilder();
                    for (int i = 0; i < taskIds.size(); i++) {
                        if (i > 0) {
                            questionMarks.append(",");
                        }
                        questionMarks.append("?");
                    }

                    if (taskActivityExists) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "DELETE FROM task_activity WHERE task_id IN (" + questionMarks.toString() + ")")) {
                            for (int i = 0; i < taskIds.size(); i++) {
                                stmt.setInt(i + 1, taskIds.get(i));
                            }
                            stmt.executeUpdate();
                        }
                    }

                    if (taskActivitiesExists) {
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "DELETE FROM task_activities WHERE task_id IN (" + questionMarks.toString() + ")")) {
                            for (int i = 0; i < taskIds.size(); i++) {
                                stmt.setInt(i + 1, taskIds.get(i));
                            }
                            stmt.executeUpdate();
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE project_id = ?")) {
                    stmt.setInt(1, projectId);
                    stmt.executeUpdate();
                }

                boolean teamMembersExists = false;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SHOW TABLES LIKE 'team_members'")) {
                    ResultSet rs = stmt.executeQuery();
                    teamMembersExists = rs.next();
                }

                if (teamMembersExists && !teamIds.isEmpty()) {
                    StringBuilder questionMarks = new StringBuilder();
                    for (int i = 0; i < teamIds.size(); i++) {
                        if (i > 0) {
                            questionMarks.append(",");
                        }
                        questionMarks.append("?");
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM team_members WHERE team_id IN (" + questionMarks.toString() + ")")) {
                        for (int i = 0; i < teamIds.size(); i++) {
                            stmt.setInt(i + 1, teamIds.get(i));
                        }
                        stmt.executeUpdate();
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM teams WHERE project_id = ?")) {
                    stmt.setInt(1, projectId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM projects WHERE id = ?")) {
                    stmt.setInt(1, projectId);
                    int affectedRows = stmt.executeUpdate();

                    conn.commit();
                    return affectedRows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pobiera projekt na podstawie ID.
     *
     * @param projectId ID projektu
     * @return Obiekt projektu lub null jeśli projekt nie istnieje
     */
    public Project getProjectById(int projectId) {
        String sql = "SELECT id, project_name, description, start_date, end_date, manager_id FROM projects WHERE id = ?";
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
                    return p;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}