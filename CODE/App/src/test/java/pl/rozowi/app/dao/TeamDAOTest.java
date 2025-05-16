package pl.rozowi.app.dao;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy TeamDAO z wykorzystaniem Mockito.
 * Klasa testuje operacje na zespołach w bazie danych,
 * stosując mocki do symulacji zachowania warstwy bazy danych.
 */
public class TeamDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private Statement mockStatement;

    private static MockedStatic<DatabaseManager> mockStaticDatabaseManager;

    private TeamDAO teamDAO;

    /**
     * Inicjalizacja statycznych mocków przed uruchomieniem wszystkich testów.
     * Tworzy statyczny mock dla klasy DatabaseManager, który będzie używany
     * we wszystkich testach.
     */
    @BeforeClass
    public static void initStaticMock() throws SQLException {
        mockStaticDatabaseManager = mockStatic(DatabaseManager.class);
    }

    /**
     * Czyszczenie statycznych mocków po zakończeniu wszystkich testów.
     * Zamyka statyczny mock DatabaseManager, aby uniknąć wycieków pamięci.
     */
    @AfterClass
    public static void closeStaticMock() {
        mockStaticDatabaseManager.close();
    }

    /**
     * Inicjalizacja środowiska testowego przed każdym testem.
     * Inicjalizuje mocki, konfiguruje ich zachowanie i tworzy
     * instancję testowanej klasy TeamDAO.
     */
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);

        mockStaticDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        teamDAO = new TeamDAO();
    }

    /**
     * Test sprawdzający metodę getAllTeams.
     * Symuluje zwrócenie dwóch zespołów z bazy danych i weryfikuje,
     * czy zostały poprawnie przetworzone na obiekty Team.
     */
    @Test
    public void testGetAllTeams() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("team_name")).thenReturn("Team A", "Team B");
        when(mockResultSet.getInt("project_id")).thenReturn(1, 2);

        List<Team> results = teamDAO.getAllTeams();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals(2, results.get(1).getId());
        assertEquals("Team A", results.get(0).getTeamName());
        assertEquals("Team B", results.get(1).getTeamName());

        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamById dla istniejącego zespołu.
     * Symuluje znalezienie zespołu o podanym ID i weryfikuje,
     * czy został poprawnie przetworzony na obiekt Team.
     */
    @Test
    public void testGetTeamById_teamExists() throws SQLException {
        int teamId = 1;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(teamId);
        when(mockResultSet.getString("team_name")).thenReturn("Team A");
        when(mockResultSet.getInt("project_id")).thenReturn(1);

        Team result = teamDAO.getTeamById(teamId);

        assertNotNull(result);
        assertEquals(teamId, result.getId());
        assertEquals("Team A", result.getTeamName());
        assertEquals(1, result.getProjectId());

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamById dla nieistniejącego zespołu.
     * Symuluje brak zespołu o podanym ID i weryfikuje,
     * czy metoda poprawnie zwraca null w takim przypadku.
     */
    @Test
    public void testGetTeamById_teamDoesNotExist() throws SQLException {
        int teamId = 999;
        when(mockResultSet.next()).thenReturn(false);

        Team result = teamDAO.getTeamById(teamId);

        assertNull(result);

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamNameById dla istniejącego zespołu.
     * Symuluje znalezienie nazwy zespołu o podanym ID i weryfikuje,
     * czy została poprawnie zwrócona.
     */
    @Test
    public void testGetTeamNameById() throws SQLException {
        int teamId = 1;
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("team_name")).thenReturn("Team A");

        String result = teamDAO.getTeamNameById(teamId);

        assertEquals("Team A", result);

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamNameById dla nieistniejącego zespołu.
     * Symuluje brak zespołu o podanym ID i weryfikuje,
     * czy metoda poprawnie zwraca symbol "–" w takim przypadku.
     */
    @Test
    public void testGetTeamNameById_teamDoesNotExist() throws SQLException {
        int teamId = 999;
        when(mockResultSet.next()).thenReturn(false);

        String result = teamDAO.getTeamNameById(teamId);

        assertEquals("–", result);

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę insertTeam w przypadku powodzenia.
     * Symuluje pomyślne dodanie zespołu do bazy danych,
     * w tym generację ID, i weryfikuje poprawność operacji.
     */
    @Test
    public void testInsertTeam_success() throws SQLException {
        Team newTeam = new Team();
        newTeam.setTeamName("Team A");
        newTeam.setProjectId(1);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        boolean result = teamDAO.insertTeam(newTeam);

        assertTrue(result);
        assertEquals(1, newTeam.getId());

        verify(mockPreparedStatement).setString(1, newTeam.getTeamName());
        verify(mockPreparedStatement).setInt(2, newTeam.getProjectId());
        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).getGeneratedKeys();
    }

    /**
     * Test sprawdzający metodę updateTeam w przypadku powodzenia.
     * Symuluje pomyślną aktualizację danych zespołu w bazie
     * i weryfikuje poprawność przekazanych parametrów.
     */
    @Test
    public void testUpdateTeam_success() throws SQLException {
        Team team = new Team();
        team.setId(1);
        team.setTeamName("Updated");
        team.setProjectId(2);

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = teamDAO.updateTeam(team);

        assertTrue(result);

        verify(mockPreparedStatement).setString(1, team.getTeamName());
        verify(mockPreparedStatement).setInt(2, team.getProjectId());
        verify(mockPreparedStatement).setInt(3, team.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający metodę getTeamMembers.
     * Symuluje pobranie członków zespołu z bazy danych
     * i weryfikuje poprawne przetworzenie ich na obiekty User.
     */
    @Test
    public void testGetTeamMembers() throws SQLException {
        int teamId = 1;
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("John", "Jane");
        when(mockResultSet.getString("last_name")).thenReturn("Doe", "Smith");
        when(mockResultSet.getString("email")).thenReturn("john@example.com", "jane@example.com");
        when(mockResultSet.getInt("role_id")).thenReturn(3, 4);
        when(mockResultSet.getBoolean("is_leader")).thenReturn(true, false);

        List<User> results = teamDAO.getTeamMembers(teamId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("John", results.get(0).getName());
        assertEquals("Jane", results.get(1).getName());
        assertEquals("Doe", results.get(0).getLastName());
        assertEquals("Smith", results.get(1).getLastName());
        assertEquals("john@example.com", results.get(0).getEmail());
        assertEquals("jane@example.com", results.get(1).getEmail());
        assertEquals(3, results.get(0).getRoleId());
        assertEquals(4, results.get(1).getRoleId());

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę insertTeamMember w przypadku powodzenia.
     * Symuluje pomyślne dodanie członka do zespołu i weryfikuje,
     * czy parametry zapytania zostały poprawnie ustawione.
     */
    @Test
    public void testInsertTeamMember_success() throws SQLException {
        int teamId = 1;
        int userId = 1;
        boolean isLeader = true;

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = teamDAO.insertTeamMember(teamId, userId, isLeader);

        assertTrue(result);

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).setBoolean(3, isLeader);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający metodę deleteTeamMember w przypadku powodzenia.
     * Symuluje pomyślne usunięcie członka z zespołu i weryfikuje,
     * czy parametry zapytania zostały poprawnie ustawione.
     */
    @Test
    public void testDeleteTeamMember_success() throws SQLException {
        int teamId = 1;
        int userId = 1;

        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = teamDAO.deleteTeamMember(teamId, userId);

        assertTrue(result);

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający metodę getTeamsForUser.
     * Symuluje pobranie zespołów, do których należy dany użytkownik,
     * i weryfikuje poprawne przetworzenie ich na obiekty Team.
     */
    @Test
    public void testGetTeamsForUser() throws SQLException {
        int userId = 1;
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("team_name")).thenReturn("Team A", "Team B");
        when(mockResultSet.getInt("project_id")).thenReturn(1, 2);

        List<Team> results = teamDAO.getTeamsForUser(userId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals(2, results.get(1).getId());
        assertEquals("Team A", results.get(0).getTeamName());
        assertEquals("Team B", results.get(1).getTeamName());

        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamsForManager.
     * Symuluje pobranie zespołów, które są przypisane do projektów
     * zarządzanych przez danego kierownika, i weryfikuje poprawne
     * przetworzenie ich na obiekty Team.
     */
    @Test
    public void testGetTeamsForManager() throws SQLException {
        int managerId = 1;
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("team_name")).thenReturn("Team A", "Team B");
        when(mockResultSet.getInt("project_id")).thenReturn(1, 2);

        List<Team> results = teamDAO.getTeamsForManager(managerId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals(2, results.get(1).getId());
        assertEquals("Team A", results.get(0).getTeamName());
        assertEquals("Team B", results.get(1).getTeamName());

        verify(mockPreparedStatement).setInt(1, managerId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamsByLeaderId.
     * Symuluje pobranie identyfikatorów zespołów, których liderem
     * jest dany użytkownik, i weryfikuje poprawność zwróconych danych.
     */
    @Test
    public void testGetTeamsByLeaderId() throws SQLException {
        int leaderId = 1;
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);

        List<Integer> results = teamDAO.getTeamsByLeaderId(leaderId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(Integer.valueOf(1), results.get(0));
        assertEquals(Integer.valueOf(2), results.get(1));

        verify(mockPreparedStatement).setInt(1, leaderId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamsByLeaderIdAsList.
     * Symuluje pobranie pełnych obiektów zespołów, których liderem
     * jest dany użytkownik, i weryfikuje poprawność przetworzenia
     * danych na obiekty Team.
     */
    @Test
    public void testGetTeamsByLeaderIdAsList() throws SQLException {
        int leaderId = 1;
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("team_name")).thenReturn("Team A", "Team B");
        when(mockResultSet.getInt("project_id")).thenReturn(1, 2);

        List<Team> results = teamDAO.getTeamsByLeaderIdAsList(leaderId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals(2, results.get(1).getId());
        assertEquals("Team A", results.get(0).getTeamName());
        assertEquals("Team B", results.get(1).getTeamName());

        verify(mockPreparedStatement).setInt(1, leaderId);
        verify(mockPreparedStatement).executeQuery();
    }
}