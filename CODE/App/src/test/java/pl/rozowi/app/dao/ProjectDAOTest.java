package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Project;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Testy jednostkowe dla klasy ProjectDAO, odpowiedzialnej za operacje
 * na projektach w bazie danych.
 */
public class ProjectDAOTest {

    private Connection connection;
    private ProjectDAO projectDAO;

    /**
     * Konfiguracja środowiska testowego przed każdym testem.
     * Tworzy tymczasową bazę danych H2 w pamięci oraz tabele
     * potrzebne do testowania operacji na projektach.
     */
    @Before
    public void setUp() throws Exception {
        DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DatabaseManager.getConnection();

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE projects ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "project_name VARCHAR(255),"
                    + "description VARCHAR(1024),"
                    + "start_date DATE,"
                    + "end_date DATE,"
                    + "manager_id INT"
                    + ")");
            stmt.execute("CREATE TABLE tasks (id INT AUTO_INCREMENT PRIMARY KEY, project_id INT)");
            stmt.execute("CREATE TABLE teams (id INT AUTO_INCREMENT PRIMARY KEY, project_id INT)");
            stmt.execute("CREATE TABLE task_assignments (task_id INT)");
            stmt.execute("CREATE TABLE task_activity (task_id INT)");
            stmt.execute("CREATE TABLE task_activities (task_id INT)");
            stmt.execute("CREATE TABLE team_members (team_id INT)");
        }

        projectDAO = new ProjectDAO();
    }

    /**
     * Czyszczenie zasobów po wykonaniu każdego testu.
     * Usuwa wszystkie tabele z bazy danych i zamyka połączenie.
     */
    @After
    public void tearDown() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        connection.close();
        DatabaseManager.setTestUrl(null);
    }

    /**
     * Test sprawdzający poprawność dodania projektu do bazy danych
     * oraz odczytania go przez metodę getProjectById.
     * Weryfikuje, czy wszystkie pola projektu są prawidłowo zapisane i odczytane.
     */
    @Test
    public void testInsertAndGetById() throws Exception {
        Project p = new Project();
        p.setName("Test Project");
        p.setDescription("Description");
        p.setStartDate(LocalDate.of(2023, 1, 1));
        p.setEndDate(LocalDate.of(2023, 12, 31));
        p.setManagerId(100);

        assertTrue(projectDAO.insertProject(p));
        assertTrue(p.getId() > 0);

        Project fetched = projectDAO.getProjectById(p.getId());
        assertNotNull(fetched);
        assertEquals(p.getName(), fetched.getName());
        assertEquals(p.getDescription(), fetched.getDescription());
        assertEquals(p.getStartDate(), fetched.getStartDate());
        assertEquals(p.getEndDate(), fetched.getEndDate());
        assertEquals(p.getManagerId(), fetched.getManagerId());
    }

    /**
     * Test sprawdzający metodę getAllProjects, która powinna zwrócić
     * wszystkie projekty zapisane w bazie danych.
     * Dodaje trzy projekty testowe i weryfikuje, czy wszystkie zostały zwrócone.
     */
    @Test
    public void testGetAllProjects() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Project p = new Project();
            p.setName("P" + i);
            p.setDescription("Desc" + i);
            p.setStartDate(LocalDate.of(2023, i, 1));
            p.setEndDate(LocalDate.of(2023, i, 10));
            p.setManagerId(i);
            projectDAO.insertProject(p);
        }

        List<Project> all = projectDAO.getAllProjects();
        assertEquals(3, all.size());
    }

    /**
     * Test sprawdzający aktualizację danych projektu w bazie.
     * Tworzy projekt, zmienia jego wartości, a następnie weryfikuje,
     * czy zmiany zostały poprawnie zapisane w bazie danych.
     */
    @Test
    public void testUpdateProject() throws Exception {
        Project p = new Project();
        p.setName("Old");
        p.setDescription("Old Desc");
        p.setStartDate(LocalDate.of(2023, 1, 1));
        p.setEndDate(LocalDate.of(2023, 6, 1));
        p.setManagerId(1);
        projectDAO.insertProject(p);

        p.setName("New");
        p.setDescription("New Desc");
        p.setStartDate(LocalDate.of(2023, 2, 1));
        p.setEndDate(LocalDate.of(2023, 7, 1));
        p.setManagerId(2);
        assertTrue(projectDAO.updateProject(p));

        Project updated = projectDAO.getProjectById(p.getId());
        assertEquals("New", updated.getName());
        assertEquals(2, updated.getManagerId());
    }

    /**
     * Test sprawdzający metodę getProjectsForManager, która powinna zwrócić
     * tylko projekty przypisane do konkretnego menedżera.
     * Dodaje projekty przypisane do różnych menedżerów i weryfikuje,
     * czy filtrowanie po ID menedżera działa poprawnie.
     */
    @Test
    public void testGetProjectsForManager() throws Exception {
        for (int i = 0; i < 2; i++) {
            Project p = new Project();
            p.setName("M10-P" + i);
            p.setDescription("x");
            p.setStartDate(LocalDate.now());
            p.setEndDate(LocalDate.now());
            p.setManagerId(10);
            projectDAO.insertProject(p);
        }
        Project p = new Project();
        p.setName("M20");
        p.setDescription("x");
        p.setStartDate(LocalDate.now());
        p.setEndDate(LocalDate.now());
        p.setManagerId(20);
        projectDAO.insertProject(p);

        List<Project> mgr10 = projectDAO.getProjectsForManager(10);
        assertEquals(2, mgr10.size());
    }
}