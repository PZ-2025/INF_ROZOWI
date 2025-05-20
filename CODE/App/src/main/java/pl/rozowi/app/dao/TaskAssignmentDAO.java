package pl.rozowi.app.dao;

import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data Access Object do operacji na przypisaniach zadań (TaskAssignment).
 */
public class TaskAssignmentDAO {

    /**
     * Wstawia nowe przypisanie zadania do bazy danych.
     *
     * @param assignment obiekt TaskAssignment zawierający identyfikatory zadania i użytkownika
     * @return true, jeśli operacja zakończyła się powodzeniem; false w przeciwnym razie
     */
    public boolean insertTaskAssignment(TaskAssignment assignment) {
        String sql = "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignment.getTaskId());
            stmt.setInt(2, assignment.getUserId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
