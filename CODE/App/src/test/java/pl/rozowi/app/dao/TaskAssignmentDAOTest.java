package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy TaskAssignmentDAO z wykorzystaniem Mockito.
 * Klasa testuje operacje związane z przypisywaniem użytkowników do zadań,
 * stosując mocki do symulacji zachowania warstwy bazy danych.
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskAssignmentDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    private TaskAssignmentDAO dao;
    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    /**
     * Inicjalizacja środowiska testowego przed każdym testem.
     * Tworzy mocki dla połączenia z bazą danych i statementów.
     * Konfiguruje statyczne mockowanie klasy DatabaseManager, aby zwracała
     * przygotowane mocki zamiast rzeczywistych połączeń z bazą danych.
     */
    @Before
    public void setUp() throws SQLException {
        dao = new TaskAssignmentDAO();

        mockedDatabaseManager = mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    /**
     * Czyszczenie zasobów po każdym teście.
     * Zamyka statyczny mock DatabaseManager, aby uniknąć wycieków pamięci.
     */
    @After
    public void tearDown() {
        if (mockedDatabaseManager != null) {
            mockedDatabaseManager.close();
        }
    }

    /**
     * Test sprawdzający pomyślne przypisanie użytkownika do zadania.
     * Symuluje udane wykonanie operacji INSERT z jednym zmodyfikowanym wierszem.
     * Weryfikuje, czy metoda poprawnie ustawia parametry zapytania i zwraca true.
     */
    @Test
    public void testInsertTaskAssignment_success() throws SQLException {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(10);
        assignment.setUserId(20);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = dao.insertTaskAssignment(assignment);

        assertTrue("Should return true when affected > 0", result);

        verify(mockPreparedStatement).setInt(1, 10);
        verify(mockPreparedStatement).setInt(2, 20);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający sytuację, gdy operacja INSERT nie modyfikuje żadnych wierszy.
     * Symuluje wykonanie zapytania, które zwraca 0 jako liczbę zmodyfikowanych wierszy.
     * Weryfikuje, czy metoda poprawnie ustawia parametry zapytania i zwraca false.
     */
    @Test
    public void testInsertTaskAssignment_noRowsAffected() throws SQLException {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(5);
        assignment.setUserId(6);

        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = dao.insertTaskAssignment(assignment);

        assertFalse("Should return false when affected == 0", result);

        verify(mockPreparedStatement).setInt(1, 5);
        verify(mockPreparedStatement).setInt(2, 6);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający obsługę wyjątków SQLException w DAO.
     * Symuluje sytuację, gdy podczas przygotowania zapytania rzucany jest
     * wyjątek SQLException, i weryfikuje, czy metoda poprawnie go obsługuje,
     * zwracając false zamiast propagowania wyjątku.
     */
    @Test
    public void testInsertTaskAssignment_sqlException() throws SQLException {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setTaskId(1);
        assignment.setUserId(2);

        reset(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        boolean result = dao.insertTaskAssignment(assignment);

        assertFalse("Should return false on SQLException", result);
    }
}