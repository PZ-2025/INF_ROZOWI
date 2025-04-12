package pl.rozowi.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    @Mock
    private UserDAO userDaoMock;

    @Mock
    private TeamMemberDAO teamMemberDaoMock;

    private LoginService loginService;

    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        loginService = new LoginService(userDaoMock, teamMemberDaoMock);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testAuthenticate_validCredentials() {
        // given
        String email = "john@domain.com";
        String rawPassword = "secret";

        // Tworzymy usera z poprawnie zahashowanym hasłem "secret" (SHA-256)
        User user = new User();
        user.setId(123);
        user.setEmail(email);
        // Rzeczywisty hash SHA-256("secret") -> 2bb80d537b1da3e38bd30361aa855686bde0baeae0efb56f8b493b7852b855
        user.setPassword("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b");

        when(userDaoMock.getUserByEmail(email)).thenReturn(user);

        // when
        User result = loginService.authenticate(email, rawPassword);

        // then
        assertNotNull(result, "Użytkownik powinien zostać poprawnie zalogowany, nie może być null.");
        assertEquals(123, result.getId());
    }

    @Test
    void testAuthenticate_invalidCredentials() {
        // given
        String email = "wrong@domain.com";
        String rawPassword = "pass";

        // Zwracamy null, żeby symulować brak użytkownika o takim emailu
        when(userDaoMock.getUserByEmail(email)).thenReturn(null);

        // when
        User result = loginService.authenticate(email, rawPassword);

        // then
        assertNull(result, "Dla niepoprawnych danych logowania metoda powinna zwrócić null.");
        verify(userDaoMock).getUserByEmail(email);
    }

    @Test
    void testFindTeamIdForUser() {
        // given
        when(teamMemberDaoMock.getTeamIdForUser(123)).thenReturn(10);

        // when
        int teamId = loginService.findTeamIdForUser(123);

        // then
        assertEquals(10, teamId);
        verify(teamMemberDaoMock).getTeamIdForUser(123);
    }
}