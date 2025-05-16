package pl.rozowi.app.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy TeamMemberDAO z wykorzystaniem Mockito.
 * Klasa testuje operacje na członkach zespołów w bazie danych,
 * stosując mocki do symulacji zachowania warstwy bazy danych.
 */
public class TeamMemberDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseManager> databaseManagerMock;
    private AutoCloseable mocks;

    private TeamMemberDAO teamMemberDAO;

    /**
     * Inicjalizacja środowiska testowego przed każdym testem.
     * Tworzy i konfiguruje mocki dla połączenia z bazą danych,
     * preparedStatements i resultSets. Inicjalizuje też
     * statyczny mock klasy DatabaseManager.
     */
    @Before
    public void setUp() throws SQLException {
        mocks = MockitoAnnotations.openMocks(this);

        databaseManagerMock = Mockito.mockStatic(DatabaseManager.class);
        databaseManagerMock.when(DatabaseManager::getConnection)
                .thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);

        teamMemberDAO = new TeamMemberDAO();
    }

    /**
     * Czyszczenie zasobów po każdym teście.
     * Zamyka statyczny mock DatabaseManager i inne mocki,
     * aby uniknąć wycieków pamięci.
     */
    @After
    public void tearDown() throws Exception {
        databaseManagerMock.close();
        mocks.close();
    }

    /**
     * Test sprawdzający metodę getTeamIdForUser dla użytkownika,
     * który jest członkiem zespołu.
     * Weryfikuje, czy metoda poprawnie zwraca ID zespołu,
     * do którego należy użytkownik.
     */
    @Test
    public void testGetTeamIdForUser_userInTeam() throws SQLException {
        int userId = 1;
        int teamId = 2;

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getInt("team_id"))
                .thenReturn(teamId);

        int result = teamMemberDAO.getTeamIdForUser(userId);

        assertEquals("Team ID should match", teamId, result);
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamIdForUser dla użytkownika,
     * który nie jest członkiem żadnego zespołu.
     * Weryfikuje, czy metoda poprawnie zwraca 0 w takim przypadku.
     */
    @Test
    public void testGetTeamIdForUser_userNotInTeam() throws SQLException {
        int userId = 1;
        when(mockResultSet.next())
                .thenReturn(false);

        int result = teamMemberDAO.getTeamIdForUser(userId);

        assertEquals("Should return 0 when user is not in any team", 0, result);
        verify(mockPreparedStatement).setInt(1, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę isTeamLeader dla użytkownika,
     * który jest liderem zespołu.
     * Weryfikuje, czy metoda poprawnie zwraca true w tym przypadku.
     */
    @Test
    public void testIsTeamLeader_isLeader() throws SQLException {
        int teamId = 1;
        int userId = 2;

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getBoolean("is_leader"))
                .thenReturn(true);

        boolean result = teamMemberDAO.isTeamLeader(teamId, userId);

        assertTrue("Should return true when user is team leader", result);
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę isTeamLeader dla użytkownika,
     * który jest członkiem zespołu, ale nie jest jego liderem.
     * Weryfikuje, czy metoda poprawnie zwraca false w tym przypadku.
     */
    @Test
    public void testIsTeamLeader_notLeader() throws SQLException {
        int teamId = 1;
        int userId = 2;

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getBoolean("is_leader"))
                .thenReturn(false);

        boolean result = teamMemberDAO.isTeamLeader(teamId, userId);

        assertFalse("Should return false when user is not team leader", result);
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę isTeamLeader dla użytkownika,
     * który nie jest członkiem danego zespołu.
     * Weryfikuje, czy metoda poprawnie zwraca false w tym przypadku.
     */
    @Test
    public void testIsTeamLeader_notInTeam() throws SQLException {
        int teamId = 1;
        int userId = 2;

        when(mockResultSet.next())
                .thenReturn(false);

        boolean result = teamMemberDAO.isTeamLeader(teamId, userId);

        assertFalse("Should return false when user is not in team", result);
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamMembers, która powinna zwrócić
     * listę użytkowników będących członkami danego zespołu.
     * Weryfikuje, czy dane z bazy są poprawnie przetwarzane na obiekty User.
     */
    @Test
    public void testGetTeamMembers() throws SQLException {
        int teamId = 1;

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(mockResultSet.getString("name")).thenReturn("John").thenReturn("Jane");
        when(mockResultSet.getString("lastName")).thenReturn("Doe").thenReturn("Smith");
        when(mockResultSet.getString("email"))
                .thenReturn("john.doe@example.com")
                .thenReturn("jane.smith@example.com");
        when(mockResultSet.getString("password"))
                .thenReturn("hash1").thenReturn("hash2");
        when(mockResultSet.getInt("roleId")).thenReturn(3).thenReturn(4);
        when(mockResultSet.getInt("groupId")).thenReturn(1).thenReturn(2);

        List<User> results = teamMemberDAO.getTeamMembers(teamId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 members", 2, results.size());
        assertEquals("First member email should match",
                "john.doe@example.com", results.get(0).getEmail());
        assertEquals("Second member email should match",
                "jane.smith@example.com", results.get(1).getEmail());

        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający metodę getTeamIdsForTeamLeader, która zwraca
     * identyfikatory zespołów, dla których dany użytkownik jest liderem.
     * Weryfikuje, czy ID zespołów są poprawnie pobierane z bazy danych.
     */
    @Test
    public void testGetTeamIdsForTeamLeader() throws SQLException {
        int leaderId = 1;

        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mockResultSet.getInt("team_id"))
                .thenReturn(1).thenReturn(2);

        List<Integer> results = teamMemberDAO.getTeamIdsForTeamLeader(leaderId);

        assertNotNull("Result list should not be null", results);
        assertEquals("Result list should contain 2 team IDs", 2, results.size());
        assertEquals("First team ID should match", 1, (int) results.get(0));
        assertEquals("Second team ID should match", 2, (int) results.get(1));

        verify(mockPreparedStatement).setInt(1, leaderId);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test sprawdzający pomyślne dodanie nowego członka do zespołu.
     * Weryfikuje, czy metoda insertTeamMember poprawnie ustawia parametry
     * zapytania i zwraca true w przypadku powodzenia operacji.
     */
    @Test
    public void testInsertTeamMember_success() throws SQLException {
        int teamId = 1;
        int userId = 2;
        boolean isLeader = true;

        when(mockPreparedStatement.executeUpdate())
                .thenReturn(1);

        boolean result = teamMemberDAO.insertTeamMember(teamId, userId, isLeader);

        assertTrue("Insert should return true on success", result);
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).setBoolean(3, isLeader);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający niepowodzenie dodania nowego członka do zespołu.
     * Symuluje sytuację, gdy operacja INSERT nie dodaje żadnych wierszy,
     * i weryfikuje, czy metoda zwraca false w takim przypadku.
     */
    @Test
    public void testInsertTeamMember_failure() throws SQLException {
        when(mockPreparedStatement.executeUpdate())
                .thenReturn(0);

        boolean result = teamMemberDAO.insertTeamMember(1, 2, true);

        assertFalse("Insert should return false on failure", result);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający pomyślną aktualizację zespołu użytkownika.
     * Weryfikuje, czy metoda updateUserTeam poprawnie ustawia parametry
     * zapytania i zwraca true w przypadku powodzenia operacji.
     */
    @Test
    public void testUpdateUserTeam_success() throws SQLException {
        int userId = 1;
        int teamId = 2;

        when(mockPreparedStatement.executeUpdate())
                .thenReturn(1);

        boolean result = teamMemberDAO.updateUserTeam(userId, teamId);

        assertTrue("Update should return true on success", result);
        verify(mockPreparedStatement).setInt(1, teamId);
        verify(mockPreparedStatement).setInt(2, userId);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający pomyślne usunięcie członka z zespołu.
     * Weryfikuje, czy metoda deleteTeamMember poprawnie ustawia parametry
     * zapytania i zwraca true w przypadku powodzenia operacji.
     */
    @Test
    public void testDeleteTeamMember_success() throws SQLException {
        when(mockPreparedStatement.executeUpdate())
                .thenReturn(1);

        boolean result = teamMemberDAO.deleteTeamMember(1, 2);

        assertTrue("Delete should return true on success", result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 2);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test sprawdzający obsługę wyjątków SQL w metodzie getTeamIdForUser.
     * Symuluje sytuację, gdy podczas wykonania zapytania jest rzucany
     * wyjątek SQLException, i weryfikuje, czy metoda poprawnie zwraca 0.
     */
    @Test
    public void testHandleSQLException() throws SQLException {
        int userId = 1;
        when(mockPreparedStatement.executeQuery())
                .thenThrow(new SQLException("Database error"));

        int result = teamMemberDAO.getTeamIdForUser(userId);

        assertEquals("Should return 0 on SQLException", 0, result);
        verify(mockPreparedStatement).executeQuery();
    }
}