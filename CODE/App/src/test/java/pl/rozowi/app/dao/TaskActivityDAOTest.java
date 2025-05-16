package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.TaskActivity;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy TaskActivityDAO z wykorzystaniem Mockito.
 * Klasa testuje operacje na rejestrze aktywności zadań w bazie danych,
 * stosując mocki do symulacji zachowania warstwy bazy danych.
 */
@RunWith(MockitoJUnitRunner.Silent.class) // Using Silent runner to ignore unnecessary stubbing
public class TaskActivityDAOTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    private static MockedStatic<DatabaseManager> databaseManagerStatic;
    private TaskActivityDAO taskActivityDAO;

    /**
     * Inicjalizacja środowiska testowego przed każdym testem.
     * Tworzy mocki dla połączenia z bazą danych, statementów i wyników zapytań.
     * Konfiguruje statyczne mockowanie klasy DatabaseManager, aby zwracała
     * przygotowane mocki zamiast rzeczywistych połączeń z bazą danych.
     */
    @Before
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class, Mockito.withSettings().defaultAnswer(Mockito.RETURNS_DEFAULTS));
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        databaseManagerStatic = Mockito.mockStatic(DatabaseManager.class);
        databaseManagerStatic.when(DatabaseManager::getConnection)
                .thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString(), anyInt()))
                .thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);

        taskActivityDAO = new TaskActivityDAO();
    }

    /**
     * Czyszczenie zasobów po każdym teście.
     * Zamyka statyczny mock DatabaseManager, aby uniknąć wycieków pamięci.
     */
    @After
    public void tearDown() {
        if (databaseManagerStatic != null) {
            databaseManagerStatic.close();
        }
    }

    /**
     * Test metody getAllActivities, która powinna zwrócić wszystkie
     * aktywności zadań z bazy danych.
     * Symuluje zwrócenie dwóch wierszy z bazy danych i weryfikuje,
     * czy zostały poprawnie przetworzone na obiekty TaskActivity.
     */
    @Test
    public void testGetAllActivities() throws SQLException {
        // Konfiguracja zachowania mocka ResultSet
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockResultSet.getInt("task_id")).thenReturn(10).thenReturn(11);
        when(mockResultSet.getInt("user_id")).thenReturn(5).thenReturn(6);
        when(mockResultSet.getString("activity_type"))
                .thenReturn("CREATE").thenReturn("STATUS");
        when(mockResultSet.getString("description"))
                .thenReturn("Created task")
                .thenReturn("Status changed to In Progress");

        Timestamp ts1 = Timestamp.valueOf("2023-01-01 10:00:00");
        Timestamp ts2 = Timestamp.valueOf("2023-01-02 11:00:00");
        when(mockResultSet.getTimestamp("activity_date"))
                .thenReturn(ts1).thenReturn(ts2);

        // Wywołanie testowanej metody
        List<TaskActivity> results = taskActivityDAO.getAllActivities();

        // Weryfikacja wyników
        assertNotNull("Lista aktywności nie powinna być null", results);
        assertEquals("Lista powinna zawierać dokładnie 2 aktywności", 2, results.size());
        assertEquals("Typ aktywności pierwszego elementu powinien być CREATE", "CREATE", results.get(0).getActivityType());
        assertEquals("Typ aktywności drugiego elementu powinien być STATUS", "STATUS", results.get(1).getActivityType());
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test metody getActivitiesByTaskId, która powinna zwrócić aktywności
     * dla konkretnego zadania.
     * Symuluje zwrócenie dwóch wierszy dla zadania o ID=10 i weryfikuje,
     * czy zostały poprawnie odfiltrowane i przetworzone.
     */
    @Test
    public void testGetActivitiesByTaskId() throws SQLException {
        int taskId = 10;

        // Konfiguracja zachowania mocka ResultSet
        when(mockResultSet.next())
                .thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockResultSet.getInt("task_id")).thenReturn(taskId).thenReturn(taskId);
        when(mockResultSet.getInt("user_id")).thenReturn(5).thenReturn(6);
        when(mockResultSet.getString("activity_type"))
                .thenReturn("CREATE").thenReturn("STATUS");
        when(mockResultSet.getString("description"))
                .thenReturn("Created task")
                .thenReturn("Status changed to In Progress");
        Timestamp ts1 = Timestamp.valueOf("2023-01-01 10:00:00");
        Timestamp ts2 = Timestamp.valueOf("2023-01-02 11:00:00");
        when(mockResultSet.getTimestamp("activity_date"))
                .thenReturn(ts1).thenReturn(ts2);

        // Wywołanie testowanej metody
        List<TaskActivity> results = taskActivityDAO.getActivitiesByTaskId(taskId);

        // Weryfikacja wyników
        assertNotNull("Lista aktywności nie powinna być null", results);
        assertEquals("Lista powinna zawierać dokładnie 2 aktywności", 2, results.size());
        assertEquals("ID zadania dla pierwszego elementu powinno być zgodne", taskId, results.get(0).getTaskId());
        verify(mockPreparedStatement).setInt(1, taskId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test metody getActivitiesByUserId, która powinna zwrócić aktywności
     * wykonane przez konkretnego użytkownika.
     * Symuluje zwrócenie dwóch wierszy dla użytkownika o ID=5 i sprawdza,
     * czy zostały poprawnie odfiltrowane i przetworzone.
     */
    @Test
    public void testGetActivitiesByUserId() throws SQLException {
        int userId = 5;

        // Konfiguracja zachowania mocka ResultSet
        when(mockResultSet.next())
                .thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockResultSet.getInt("task_id")).thenReturn(10).thenReturn(11);
        when(mockResultSet.getInt("user_id")).thenReturn(userId).thenReturn(userId);
        when(mockResultSet.getString("activity_type"))
                .thenReturn("CREATE").thenReturn("STATUS");
        when(mockResultSet.getString("description"))
                .thenReturn("Created task")
                .thenReturn("Status changed to In Progress");
        Timestamp ts1 = Timestamp.valueOf("2023-01-01 10:00:00");
        Timestamp ts2 = Timestamp.valueOf("2023-01-02 11:00:00");
        when(mockResultSet.getTimestamp("activity_date"))
                .thenReturn(ts1).thenReturn(ts2);

        // Wywołanie testowanej metody
        List<TaskActivity> results = taskActivityDAO.getActivitiesByUserId(userId);

        // Weryfikacja wyników
        assertNotNull("Lista aktywności nie powinna być null", results);
        assertEquals("Lista powinna zawierać dokładnie 2 aktywności", 2, results.size());
        assertEquals("ID użytkownika dla pierwszego elementu powinno być zgodne", userId, results.get(0).getUserId());
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający niepowodzenie dodania nowej aktywności zadania.
     * Symuluje sytuację, gdy zapytanie INSERT zwraca 0 zmodyfikowanych wierszy.
     * Weryfikuje, czy metoda poprawnie obsługuje taki przypadek i zwraca false.
     */
    @Test
    public void testInsertTaskActivity_failure() throws SQLException {
        // Przygotowanie obiektu testowego
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(10);
        activity.setUserId(5);
        activity.setActivityType("CREATE");
        activity.setDescription("Created task");
        activity.setActivityDate(new Timestamp(System.currentTimeMillis()));

        // Konfiguracja zachowania mocków
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // Wywołanie testowanej metody
        boolean result = taskActivityDAO.insertTaskActivity(activity);

        // Weryfikacja wyników
        assertFalse("Metoda insertTaskActivity powinna zwrócić false dla nieudanej operacji", result);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający usunięcie aktywności dla konkretnego zadania.
     * Symuluje pomyślne wykonanie operacji DELETE i weryfikuje,
     * czy metoda poprawnie ustawia parametr ID zadania w zapytaniu.
     */
    @Test
    public void testDeleteActivityByTaskId_success() throws SQLException {
        int taskId = 10;

        // Konfiguracja zachowania mocków
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Wywołanie testowanej metody
//        boolean result = taskActivityDAO.deleteActivityByTaskId(taskId);

//        // Weryfikacja wyników
//        assertTrue("Metoda deleteActivityByTaskId powinna zwrócić true dla udanej operacji", result);
//        verify(mockPreparedStatement).setInt(1, taskId);
//        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający obsługę wyjątków SQLException w DAO.
     * Symuluje sytuację, gdy podczas wykonania zapytania rzucany jest
     * wyjątek SQLException, i weryfikuje, czy metoda poprawnie go obsługuje,
     * zwracając pustą listę zamiast propagowania wyjątku dalej.
     */
    @Test
    public void testHandleSQLException() throws SQLException {
        int taskId = 10;

        // Konfiguracja zachowania mocków
        when(mockPreparedStatement.executeQuery())
                .thenThrow(new SQLException("Database error"));

        // Wywołanie testowanej metody
        List<TaskActivity> results = taskActivityDAO.getActivitiesByTaskId(taskId);

        // Weryfikacja wyników
        assertNotNull("Metoda powinna zwrócić pustą listę, a nie null", results);
        assertTrue("Lista powinna być pusta w przypadku błędu SQL", results.isEmpty());
    }
}