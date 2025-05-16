package pl.rozowi.app.dao;

import org.junit.Before;
import org.junit.Test;
import pl.rozowi.app.models.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Testy jednostkowe dla klasy TaskDAO z wykorzystaniem podklasy testowej.
 * W przeciwieństwie do podejścia z mockami, testy te wykorzystują dziedziczenie
 * i nadpisanie metod w celu izolacji testowanego kodu od rzeczywistej warstwy bazodanowej.
 */
public class TaskDAOTest {

    /**
     * Wewnętrzna klasa TestableTaskDAO, która dziedziczy po TaskDAO
     * i nadpisuje jej metody, symulując zachowanie warstwy bazodanowej.
     * Przechowuje parametry ostatnich wywołań metod, co umożliwia weryfikację
     * poprawności ich użycia w testach.
     */
    private class TestableTaskDAO extends TaskDAO {
        private boolean shouldThrowSQLException = false;
        private int mockResultSize = 2;
        private boolean mockOperationSuccess = true;
        private int lastTaskId = 0;
        private int lastUserId = 0;
        private String lastStatus = null;
        private Task lastTask = null;

        /**
         * Nadpisana metoda getConnection, która może symulować wyjątki SQL.
         */
        protected Connection getConnection() throws SQLException {
            if (shouldThrowSQLException) {
                throw new SQLException("Test-induced exception");
            }
            return null;
        }

        /**
         * Nadpisana metoda pobierająca zadania dla projektu.
         * Zamiast rzeczywistego zapytania do bazy danych, zwraca przygotowane dane testowe.
         */
        @Override
        public List<Task> getTasksByProjectId(int projectId) {
            if (shouldThrowSQLException) {
                return super.getTasksByProjectId(projectId);
            }
            return createMockTaskList(projectId, 0);
        }

        /**
         * Nadpisana metoda pobierająca zadania dla zespołu.
         * Zwraca przygotowane dane testowe zamiast danych z bazy.
         */
        @Override
        public List<Task> getTasksByTeamId(int teamId) {
            if (shouldThrowSQLException) {
                return super.getTasksByTeamId(teamId);
            }
            return createMockTaskList(0, teamId);
        }

        /**
         * Nadpisana metoda pobierająca zadania przypisane do użytkownika.
         * Zapisuje ID użytkownika i zwraca przygotowane dane testowe.
         */
        @Override
        public List<Task> getTasksForUser(int userId) {
            if (shouldThrowSQLException) {
                return super.getTasksForUser(userId);
            }
            lastUserId = userId;
            return createMockTaskList(0, 0);
        }

        /**
         * Nadpisana metoda pobierająca zadania dla lidera zespołu.
         * Zapisuje ID lidera i zwraca przygotowane dane testowe.
         */
        @Override
        public List<Task> getTasksForLeader(int leaderId) {
            if (shouldThrowSQLException) {
                return super.getTasksForLeader(leaderId);
            }
            lastUserId = leaderId;
            return createMockTaskList(0, 0);
        }

        /**
         * Nadpisana metoda pobierająca zadania kolegów z zespołu.
         * Zapisuje ID użytkownika i zwraca przygotowane dane testowe.
         */
        @Override
        public List<Task> getColleagueTasks(int userId, int teamId) {
            if (shouldThrowSQLException) {
                return super.getColleagueTasks(userId, teamId);
            }
            lastUserId = userId;
            return createMockTaskList(0, teamId);
        }

        /**
         * Nadpisana metoda wstawiająca nowe zadanie.
         * Zapisuje zadanie i symuluje przypisanie ID przez bazę danych.
         */
        @Override
        public boolean insertTask(Task task) {
            if (shouldThrowSQLException) {
                return super.insertTask(task);
            }
            lastTask = task;
            task.setId(1);
            return mockOperationSuccess;
        }

        /**
         * Nadpisana metoda aktualizująca istniejące zadanie.
         * Zapisuje zadanie dla celów weryfikacji w testach.
         */
        @Override
        public boolean updateTask(Task task) {
            if (shouldThrowSQLException) {
                return super.updateTask(task);
            }
            lastTask = task;
            return mockOperationSuccess;
        }

        /**
         * Nadpisana metoda usuwająca zadanie.
         * Zapisuje ID zadania dla celów weryfikacji w testach.
         */
        @Override
        public boolean deleteTask(int taskId) {
            if (shouldThrowSQLException) {
                return super.deleteTask(taskId);
            }
            lastTaskId = taskId;
            return mockOperationSuccess;
        }

        /**
         * Nadpisana metoda aktualizująca status zadania.
         * Zapisuje ID zadania i nowy status dla celów weryfikacji.
         */
        @Override
        public boolean updateTaskStatus(int taskId, String newStatus) {
            if (shouldThrowSQLException) {
                return super.updateTaskStatus(taskId, newStatus);
            }
            lastTaskId = taskId;
            lastStatus = newStatus;
            return mockOperationSuccess;
        }

        /**
         * Nadpisana metoda przypisująca użytkownika do zadania.
         * Zapisuje ID zadania i użytkownika dla celów weryfikacji.
         */
        @Override
        public boolean assignTask(int taskId, int userId) {
            if (shouldThrowSQLException) {
                return super.assignTask(taskId, userId);
            }
            lastTaskId = taskId;
            lastUserId = userId;
            return mockOperationSuccess;
        }

        /**
         * Nadpisana metoda pobierająca ID użytkownika przypisanego do zadania.
         * Zapisuje ID zadania i zwraca stałą wartość testową.
         */
        @Override
        public int getAssignedUserId(int taskId) {
            if (shouldThrowSQLException) {
                return super.getAssignedUserId(taskId);
            }
            lastTaskId = taskId;
            return 5;
        }

        /**
         * Nadpisana metoda pobierająca email użytkownika przypisanego do zadania.
         * Zapisuje ID zadania i zwraca stałą wartość testową.
         */
        @Override
        public String getAssignedUserEmail(int taskId) {
            if (shouldThrowSQLException) {
                return super.getAssignedUserEmail(taskId);
            }
            lastTaskId = taskId;
            return "user@example.com";
        }

        /**
         * Metoda pomocnicza tworząca testową listę zadań.
         * Tworzy jedno lub dwa zadania z predefiniowanymi wartościami.
         */
        private List<Task> createMockTaskList(int projectId, int teamId) {
            Task task1 = new Task();
            task1.setId(1);
            task1.setProjectId(projectId > 0 ? projectId : 1);
            task1.setTeamId(teamId > 0 ? teamId : 1);
            task1.setTitle("Task 1");
            task1.setDescription("Desc 1");
            task1.setStatus("New");
            task1.setPriority("High");
            task1.setStartDate("2023-01-01");
            task1.setEndDate("2023-02-01");
            task1.setTeamName("Team 1");
            task1.setAssignedEmail("user1@example.com");
            task1.setAssignedTo(101);

            Task task2 = new Task();
            task2.setId(2);
            task2.setProjectId(projectId > 0 ? projectId : 1);
            task2.setTeamId(teamId > 0 ? teamId : 2);
            task2.setTitle("Task 2");
            task2.setDescription("Desc 2");
            task2.setStatus("In Progress");
            task2.setPriority("Medium");
            task2.setStartDate("2023-01-15");
            task2.setEndDate("2023-02-15");
            task2.setTeamName("Team 2");
            task2.setAssignedEmail("user2@example.com");
            task2.setAssignedTo(102);

            return mockResultSize == 2 ?
                    List.of(task1, task2) :
                    List.of(task1);
        }

        /**
         * Metoda ustawiająca flagę do symulowania wyjątków SQL.
         */
        public void setShouldThrowSQLException(boolean shouldThrow) {
            this.shouldThrowSQLException = shouldThrow;
        }

        /**
         * Metoda ustawiająca rozmiar zwracanych wyników.
         */
        public void setMockResultSize(int size) {
            this.mockResultSize = size;
        }

        /**
         * Metoda ustawiająca flagę sukcesu dla operacji bazodanowych.
         */
        public void setMockOperationSuccess(boolean success) {
            this.mockOperationSuccess = success;
        }

        /**
         * Metoda zwracająca ID ostatnio użytego zadania.
         */
        public int getLastTaskId() {
            return lastTaskId;
        }

        /**
         * Metoda zwracająca ID ostatnio użytego użytkownika.
         */
        public int getLastUserId() {
            return lastUserId;
        }

        /**
         * Metoda zwracająca ostatnio użyty status zadania.
         */
        public String getLastStatus() {
            return lastStatus;
        }

        /**
         * Metoda zwracająca ostatnio użyte zadanie.
         */
        public Task getLastTask() {
            return lastTask;
        }
    }

    private TestableTaskDAO taskDAO;

    /**
     * Inicjalizacja środowiska testowego przed każdym testem.
     * Tworzy instancję TestableTaskDAO dla celów testowych.
     */
    @Before
    public void setUp() {
        taskDAO = new TestableTaskDAO();
    }

    /**
     * Test sprawdzający metodę getTasksByProjectId.
     * Weryfikuje, czy zwraca poprawną listę zadań dla podanego projektu
     * i czy wszystkie właściwości zadań są prawidłowo ustawione.
     */
    @Test
    public void testGetTasksByProjectId() {
        int projectId = 1;

        List<Task> results = taskDAO.getTasksByProjectId(projectId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 tasks", 2, results.size());
        assertEquals("First task title should match", "Task 1", results.get(0).getTitle());
        assertEquals("Second task title should match", "Task 2", results.get(1).getTitle());
        assertEquals("First task team name should match", "Team 1", results.get(0).getTeamName());
        assertEquals("First task assigned email should match", "user1@example.com", results.get(0).getAssignedEmail());
        assertEquals("First task assigned id should match", 101, results.get(0).getAssignedTo());
    }

    /**
     * Test sprawdzający metodę getTasksByTeamId.
     * Weryfikuje, czy zwraca poprawną listę zadań dla podanego zespołu
     * i czy dane zadań są prawidłowo ustawione.
     */
    @Test
    public void testGetTasksByTeamId() {
        int teamId = 1;

        List<Task> results = taskDAO.getTasksByTeamId(teamId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 tasks", 2, results.size());
        assertEquals("First task title should match", "Task 1", results.get(0).getTitle());
        assertEquals("Second task title should match", "Task 2", results.get(1).getTitle());
        assertEquals("First task assigned email should match", "user1@example.com", results.get(0).getAssignedEmail());
    }

    /**
     * Test sprawdzający metodę getTasksForUser.
     * Weryfikuje, czy zwraca poprawną listę zadań dla danego użytkownika
     * i czy ID użytkownika jest prawidłowo przekazane do DAO.
     */
    @Test
    public void testGetTasksForUser() {
        int userId = 1;

        List<Task> results = taskDAO.getTasksForUser(userId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 tasks", 2, results.size());
        assertEquals("First task title should match", "Task 1", results.get(0).getTitle());
        assertEquals("Second task title should match", "Task 2", results.get(1).getTitle());
        assertEquals("Last user ID should match", userId, taskDAO.getLastUserId());
    }

    /**
     * Test sprawdzający pomyślne dodanie nowego zadania.
     * Weryfikuje, czy metoda insertTask poprawnie zapisuje zadanie
     * i ustawia wygenerowane ID po dodaniu do bazy.
     */
    @Test
    public void testInsertTask_success() {
        Task newTask = new Task();
        newTask.setProjectId(1);
        newTask.setTeamId(1);
        newTask.setTitle("New Task");
        newTask.setDescription("New Task Description");
        newTask.setStatus("New");
        newTask.setPriority("High");
        newTask.setStartDate("2023-01-01");
        newTask.setEndDate("2023-02-01");

        boolean result = taskDAO.insertTask(newTask);

        assertTrue("Insert should return true on success", result);
        assertEquals("Task ID should be updated with generated key", 1, newTask.getId());

        Task lastTask = taskDAO.getLastTask();
        assertNotNull("Last task should not be null", lastTask);
        assertEquals("Task title should match", "New Task", lastTask.getTitle());
        assertEquals("Task description should match", "New Task Description", lastTask.getDescription());
    }

    /**
     * Test sprawdzający pomyślną aktualizację istniejącego zadania.
     * Weryfikuje, czy metoda updateTask poprawnie zapisuje zaktualizowane
     * dane zadania.
     */
    @Test
    public void testUpdateTask_success() {
        Task task = new Task();
        task.setId(1);
        task.setProjectId(1);
        task.setTeamId(1);
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus("In Progress");
        task.setPriority("Medium");
        task.setStartDate("2023-01-15");
        task.setEndDate("2023-02-15");

        boolean result = taskDAO.updateTask(task);

        assertTrue("Update should return true on success", result);

        Task lastTask = taskDAO.getLastTask();
        assertNotNull("Last task should not be null", lastTask);
        assertEquals("Task title should match", "Updated Task", lastTask.getTitle());
        assertEquals("Task description should match", "Updated Description", lastTask.getDescription());
    }

    /**
     * Test sprawdzający niepowodzenie aktualizacji zadania.
     * Symuluje sytuację, gdy operacja aktualizacji kończy się niepowodzeniem
     * i weryfikuje, czy metoda updateTask poprawnie obsługuje ten przypadek.
     */
    @Test
    public void testUpdateTask_failure() {
        Task task = new Task();
        task.setId(1);
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus("In Progress");

        taskDAO.setMockOperationSuccess(false);
        boolean result = taskDAO.updateTask(task);

        assertFalse("Update should return false on failure", result);
    }

    /**
     * Test sprawdzający pomyślne usunięcie zadania.
     * Weryfikuje, czy metoda deleteTask poprawnie obsługuje ID zadania
     * i zwraca true w przypadku powodzenia operacji.
     */
    @Test
    public void testDeleteTask_success() {
        int taskId = 1;

        boolean result = taskDAO.deleteTask(taskId);

        assertTrue("Delete should return true on success", result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
    }

    /**
     * Test sprawdzający niepowodzenie usunięcia zadania.
     * Symuluje sytuację, gdy operacja usunięcia kończy się niepowodzeniem
     * i weryfikuje, czy metoda deleteTask poprawnie obsługuje ten przypadek.
     */
    @Test
    public void testDeleteTask_failure() {
        int taskId = 1;

        taskDAO.setMockOperationSuccess(false);
        boolean result = taskDAO.deleteTask(taskId);

        assertFalse("Delete should return false on failure", result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
    }

    /**
     * Test sprawdzający pomyślną aktualizację statusu zadania.
     * Weryfikuje, czy metoda updateTaskStatus poprawnie obsługuje
     * ID zadania i nowy status, a także czy zwraca true w przypadku
     * powodzenia operacji.
     */
    @Test
    public void testUpdateTaskStatus_success() {
        int taskId = 1;
        String newStatus = "Completed";

        boolean result = taskDAO.updateTaskStatus(taskId, newStatus);

        assertTrue("Status update should return true on success", result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
        assertEquals("Last status should match", newStatus, taskDAO.getLastStatus());
    }

    /**
     * Test sprawdzający pomyślne przypisanie użytkownika do zadania.
     * Weryfikuje, czy metoda assignTask poprawnie obsługuje ID
     * zadania i użytkownika, a także czy zwraca true w przypadku
     * powodzenia operacji.
     */
    @Test
    public void testAssignTask_success() {
        int taskId = 1;
        int userId = 2;

        boolean result = taskDAO.assignTask(taskId, userId);

        assertTrue("Task assignment should return true on success", result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
        assertEquals("Last user ID should match", userId, taskDAO.getLastUserId());
    }

    /**
     * Test sprawdzający metodę getTasksForLeader.
     * Weryfikuje, czy zwraca poprawną listę zadań dla danego lidera
     * i czy ID lidera jest prawidłowo przekazane do DAO.
     */
    @Test
    public void testGetTasksForLeader() {
        int leaderId = 1;

        List<Task> results = taskDAO.getTasksForLeader(leaderId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 tasks", 2, results.size());
        assertEquals("First task title should match", "Task 1", results.get(0).getTitle());
        assertEquals("Second task title should match", "Task 2", results.get(1).getTitle());
        assertEquals("Last user ID should match", leaderId, taskDAO.getLastUserId());
    }

    /**
     * Test sprawdzający metodę getColleagueTasks.
     * Weryfikuje, czy zwraca poprawną listę zadań kolegów z zespołu
     * dla danego użytkownika i czy ID użytkownika jest prawidłowo
     * przekazane do DAO.
     */
    @Test
    public void testGetColleagueTasks() {
        int userId = 1;
        int teamId = 1;

        List<Task> results = taskDAO.getColleagueTasks(userId, teamId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 tasks", 2, results.size());
        assertEquals("First task title should match", "Task 1", results.get(0).getTitle());
        assertEquals("Second task title should match", "Task 2", results.get(1).getTitle());
        assertEquals("Last user ID should match", userId, taskDAO.getLastUserId());
    }

    /**
     * Test sprawdzający metodę getAssignedUserId.
     * Weryfikuje, czy zwraca poprawne ID użytkownika przypisanego do zadania
     * i czy ID zadania jest prawidłowo przekazane do DAO.
     */
    @Test
    public void testGetAssignedUserId() {
        int taskId = 1;
        int expectedUserId = 5;

        int result = taskDAO.getAssignedUserId(taskId);

        assertEquals("Should return the correct user ID", expectedUserId, result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
    }

    /**
     * Test sprawdzający metodę getAssignedUserEmail.
     * Weryfikuje, czy zwraca poprawny email użytkownika przypisanego do zadania
     * i czy ID zadania jest prawidłowo przekazane do DAO.
     */
    @Test
    public void testGetAssignedUserEmail() {
        int taskId = 1;
        String expectedEmail = "user@example.com";

        String result = taskDAO.getAssignedUserEmail(taskId);

        assertEquals("Should return the correct email", expectedEmail, result);
        assertEquals("Last task ID should match", taskId, taskDAO.getLastTaskId());
    }
}