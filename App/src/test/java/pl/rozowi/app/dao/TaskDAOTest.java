package pl.rozowi.app.dao;

import org.junit.jupiter.api.*;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Task;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskDAOTest {

    private TaskDAO taskDAO;

    /**
     * Konfiguracja środowiska testowego – uruchamiana raz przed wszystkimi testami.
     * Tworzy bazę danych H2 w pamięci, tabele i dane testowe.
     */
    @BeforeAll
    void setupDatabase() throws Exception {
        // Ustawienie bazy testowej H2 w pamięci
        DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        taskDAO = new TaskDAO();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Tworzenie wymaganych tabel
            stmt.execute("CREATE TABLE tasks (id INT PRIMARY KEY, project_id INT, team_id INT, title VARCHAR(255), description VARCHAR(255), status VARCHAR(50), priority VARCHAR(50), start_date VARCHAR(20), end_date VARCHAR(20))");
            stmt.execute("CREATE TABLE task_assignments (task_id INT, user_id INT)");
            stmt.execute("CREATE TABLE teams (id INT PRIMARY KEY, team_name VARCHAR(255))");

            // Wprowadzenie danych testowych
            stmt.execute("INSERT INTO tasks VALUES (1, 101, 201, 'Zadanie 1', 'Opis 1', 'Do zrobienia', 'Wysoki', '2024-01-01', '2024-02-01')");
            stmt.execute("INSERT INTO tasks VALUES (2, 101, 201, 'Zadanie 2', 'Opis 2', 'W trakcie', 'Średni', '2024-02-01', '2024-03-01')");
            stmt.execute("INSERT INTO task_assignments VALUES (1, 1)");
            stmt.execute("INSERT INTO task_assignments VALUES (2, 2)");
            stmt.execute("INSERT INTO teams VALUES (201, 'Dev Team')");
        }
    }

    /**
     * Test sprawdza, czy metoda getTasksForUser zwraca tylko zadania przypisane do danego użytkownika.
     * W tym przypadku użytkownik o ID 1 powinien widzieć tylko jedno zadanie: "Zadanie 1".
     */
    @Test
    public void testGetTasksForUser() {
        List<Task> tasks = taskDAO.getTasksForUser(1);
        assertEquals(1, tasks.size());
        assertEquals("Zadanie 1", tasks.get(0).getTitle());
    }

    /**
     * Test sprawdza, czy użytkownik widzi zadania swoich kolegów z zespołu z pominięciem własnych.
     * Użytkownik 1 nie powinien widzieć "Zadanie 1", ale powinien widzieć "Zadanie 2" kolegi z teamu 201.
     */
    @Test
    public void testGetColleagueTasks() {
        List<Task> tasks = taskDAO.getColleagueTasks(1, 201); // userId 1 -> nie powinien widzieć swojego
        assertEquals(1, tasks.size());
        assertEquals("Zadanie 2", tasks.get(0).getTitle());
    }

    /**
     * Test sprawdza aktualizację statusu zadania.
     * Zmienia status zadania o ID 1 na "Zakończone", a potem sprawdza, czy faktycznie został zaktualizowany.
     */
    @Test
    public void testUpdateTask() {
        Task task = new Task();
        task.setId(1);
        task.setStatus("Zakończone");

        boolean updated = taskDAO.updateTask(task);
        assertTrue(updated);

        List<Task> tasks = taskDAO.getTasksForUser(1);
        assertEquals("Zakończone", tasks.get(0).getStatus());
    }

    /**
     * Czyszczenie bazy danych po zakończeniu wszystkich testów.
     * Usuwa wszystkie obiekty z bazy H2 w pamięci.
     */
    @AfterAll
    void teardown() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
    }
}
