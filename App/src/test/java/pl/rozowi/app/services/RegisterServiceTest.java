package pl.rozowi.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterServiceTest {

    @Mock
    private UserDAO userDaoMock;

    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        registerService = new RegisterService(userDaoMock);
    }

    @Test
    void testRegister_success() {
        // given
        String firstName = "Adam";
        String lastName = "Kowalski";
        String email = "ab@cd";
        String password = "pass@123";
        String confirm = "pass@123";

        // Zakładamy, że userDAO.insertUser(...) się powiedzie
        when(userDaoMock.insertUser(any(User.class))).thenReturn(true);

        // when
        RegistrationResult result = registerService.register(firstName, lastName, email, password, confirm);

        // then
        assertTrue(result.isSuccess(), "Rejestracja powinna się udać przy poprawnych danych");
        verify(userDaoMock, times(1)).insertUser(any(User.class));
    }

    @Test
    void testRegister_failWrongCaseForFirstName() {
        String firstName = "adam"; // z małej litery
        String lastName = "Kowalski";
        String email = "ab@cd";
        String password = "pass@123";
        String confirm = "pass@123";

        // when
        RegistrationResult result = registerService.register(firstName, lastName, email, password, confirm);

        // then
        assertFalse(result.isSuccess());
        assertEquals("Imię musi zaczynać się od wielkiej litery!", result.getMessage());
        verify(userDaoMock, never()).insertUser(any(User.class));
    }

    @Test
    void testRegister_failEmailRegex() {
        // Brak znaku '@', albo za mało znaków
        String firstName = "Adam";
        String lastName = "Kowalski";
        String email = "a@";
        String password = "xyz@";
        String confirm = "xyz@";

        RegistrationResult result = registerService.register(firstName, lastName, email, password, confirm);

        assertFalse(result.isSuccess());
        assertEquals("Email musi zawierać znak '@' z co najmniej dwoma znakami przed i po nim!", result.getMessage());
        verify(userDaoMock, never()).insertUser(any(User.class));
    }

    @Test
    void testRegister_failPasswordsDifferent() {
        String firstName = "Adam";
        String lastName = "Kowalski";
        String email = "ab@cd";
        String password = "pass@123";
        String confirm = "pass@999"; // inne hasło

        RegistrationResult result = registerService.register(firstName, lastName, email, password, confirm);

        assertFalse(result.isSuccess());
        assertEquals("Hasła nie są takie same!", result.getMessage());
        verify(userDaoMock, never()).insertUser(any(User.class));
    }

    // itp. Możesz dodać test sprawdzający, gdy insertUser zwraca false
}