package pl.rozowi.app.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void testGetConnection() {
        try (Connection connection = DatabaseManager.getConnection()) {
            assertNotNull(connection, "Połączenie z bazą danych nie powinno być null");
            // Możesz np. sprawdzić, czy autoCommit jest true/false
            assertFalse(connection.isClosed(), "Połączenie nie powinno być zamknięte");
        } catch (SQLException e) {
            fail("Nie udało się uzyskać połączenia z bazą: " + e.getMessage());
        }
    }

    @Test
    void testSchemaExists_ExistingSchema() {
        boolean exists = DatabaseManager.schemaExists("it_task_management");
        assertTrue(exists, "Oczekiwano, że schema it_task_management istnieje w bazie");
    }

    @Test
    void testSchemaExists_NonExistingSchema() {
        boolean exists = DatabaseManager.schemaExists("some_random_schema_123");
        assertFalse(exists, "Oczekiwano, że schema some_random_schema_123 nie istnieje w bazie");
    }
}