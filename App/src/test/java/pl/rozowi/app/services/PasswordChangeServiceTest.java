package pl.rozowi.app.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordChangeServiceTest {

    private final PasswordChangeService service = new PasswordChangeService();

    /**
     * Test sprawdza poprawne działanie metody validateAndHashPassword
     * gdy hasło i jego potwierdzenie są poprawne i zgodne.
     * Sprawdza, czy wynik nie jest nullem i czy hashowanie jest spójne.
     */
    @Test
    public void testValidPasswordChange() {
        String password = "Test1234!";
        String confirm = "Test1234!";
        String hashed = service.validateAndHashPassword(password, confirm);
        assertNotNull(hashed);
        assertEquals(service.hashPassword(password), hashed); 
    }

    /**
     * Test sprawdza, czy metoda rzuca wyjątek, gdy hasło jest puste.
     * Taki przypadek powinien zostać odrzucony z komunikatem walidacyjnym.
     */
    @Test
    public void testEmptyPasswordThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.validateAndHashPassword("", "abc")
        );
        assertEquals("Pole hasła nie może być puste!", exception.getMessage());
    }

    /**
     * Test sprawdza, czy metoda odpowiednio reaguje na brak hasła (null).
     * Null powinien być traktowany jako niepoprawne dane wejściowe.
     */
    @Test
    public void testNullPasswordThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.validateAndHashPassword(null, null)
        );
        assertEquals("Pole hasła nie może być puste!", exception.getMessage());
    }

    /**
     * Test waliduje sytuację, w której hasło i jego potwierdzenie nie są takie same.
     * Oczekiwany rezultat to wyjątek z odpowiednim komunikatem.
     */
    @Test
    public void testMismatchedPasswordsThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.validateAndHashPassword("password1", "password2")
        );
        assertEquals("Hasła nie są takie same!", exception.getMessage());
    }

    /**
     * Test sprawdza, czy metoda hashPassword generuje taki sam hash
     * dla dwóch wywołań z tym samym hasłem.
     * Dzięki temu wiemy, że hash jest deterministyczny.
     */
    @Test
    public void testHashPasswordConsistency() {
        String pass = "abc123";
        String hash1 = service.hashPassword(pass);
        String hash2 = service.hashPassword(pass);
        assertEquals(hash1, hash2);
    }

    /**
     * Test sprawdza, czy hashowanie hasła zwraca niepusty, poprawny hash.
     * To zabezpieczenie przed sytuacjami, gdzie funkcja zwraca null lub pusty string.
     */
    @Test
    public void testHashPasswordNotNull() {
        String pass = "secure";
        String hashed = service.hashPassword(pass);
        assertNotNull(hashed); 
        assertTrue(hashed.length() > 0); 
    }
}