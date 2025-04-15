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

    // Potrzebne do zamknięcia zasobów Mockito po każdym teście
    AutoCloseable closeable;

    /**
     * Inicjalizacja mocków przed każdym testem oraz utworzenie instancji testowanego LoginService.
     */
    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        loginService = new LoginService(userDaoMock, teamMemberDaoMock);
    }

    /**
     * Zamyka kontekst mocków po każdym teście, aby uniknąć wycieków pamięci.
     */
    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Test sprawdzający poprawne uwierzytelnienie użytkownika przy prawidłowych danych logowania.
     * Wykorzystuje prawidłowy hash SHA-256 dla hasła "secret".
     */
    @Test
    void testAuthenticate_validCredentials() {
        // given
        String email = "john@domain.com";
        String rawPassword = "secret";

        // Tworzymy użytkownika z poprawnym hashem hasła (SHA-256 z "secret")
        User user = new User();
        user.setId(123);
        user.setEmail(email);
        user.setPassword("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b");

        // Symulacja: DAO zwraca użytkownika przy podanym emailu
        when(userDaoMock.getUserByEmail(email)).thenReturn(user);

        // when
        User result = loginService.authenticate(email, rawPassword);

        // then
        assertNotNull(result, "Użytkownik powinien zostać poprawnie zalogowany, nie może być null.");
        assertEquals(123, result.getId(), "Zwrócony użytkownik powinien mieć prawidłowe ID.");
    }

    /**
     * Test sprawdzający przypadek, gdy podano nieprawidłowy email (użytkownik nie istnieje).
     * Oczekujemy zwrócenia null i wywołania metody DAO.
     */
    @Test
    void testAuthenticate_invalidCredentials() {
        // given
        String email = "wrong@domain.com";
        String rawPassword = "pass";

        // Symulacja: DAO nie znajduje użytkownika
        when(userDaoMock.getUserByEmail(email)).thenReturn(null);

        // when
        User result = loginService.authenticate(email, rawPassword);

        // then
        assertNull(result, "Dla niepoprawnych danych logowania metoda powinna zwrócić null.");
        verify(userDaoMock).getUserByEmail(email);
    }

    /**
     * Test sprawdzający, czy LoginService poprawnie pobiera identyfikator zespołu użytkownika.
     */
    @Test
    void testFindTeamIdForUser() {
        // given
        when(teamMemberDaoMock.getTeamIdForUser(123)).thenReturn(10);

        // when
        int teamId = loginService.findTeamIdForUser(123);

        // then
        assertEquals(10, teamId, "Metoda powinna zwrócić poprawny ID zespołu.");
        verify(teamMemberDaoMock).getTeamIdForUser(123);
    }
}
