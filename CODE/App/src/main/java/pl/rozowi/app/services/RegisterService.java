package pl.rozowi.app.services;

import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Klasa odpowiedzialna za proces rejestracji nowych użytkowników w systemie.
 * Zapewnia walidację danych wejściowych, tworzenie nowych kont użytkowników
 * i zwracanie wyników operacji rejestracji.
 */
public class RegisterService {

    private final UserDAO userDAO;

    /**
     * Konstruktor inicjalizujący serwis rejestracji.
     *
     * @param userDAO obiekt DAO do komunikacji z warstwą danych użytkowników
     */
    public RegisterService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Rejestruje nowego użytkownika w systemie po przeprowadzeniu walidacji danych.
     *
     * @param firstName imię użytkownika
     * @param lastName nazwisko użytkownika
     * @param email adres email użytkownika
     * @param password hasło użytkownika
     * @param confirmPassword potwierdzenie hasła
     * @return obiekt RegistrationResult zawierający status i komunikat wyniku operacji
     */
    public RegistrationResult register(String firstName, String lastName, String email,
                                       String password, String confirmPassword) {
        if (!isCapitalized(firstName)) {
            return RegistrationResult.fail("Imię musi zaczynać się od wielkiej litery!");
        }
        if (!isCapitalized(lastName)) {
            return RegistrationResult.fail("Nazwisko musi zaczynać się od wielkiej litery!");
        }
        if (!isValidEmail(email)) {
            return RegistrationResult.fail("Email musi zawierać znak '@' z co najmniej dwoma znakami przed i po nim!");
        }
        if (!hasSpecialChar(password)) {
            return RegistrationResult.fail("Hasło musi zawierać przynajmniej jeden znak specjalny!");
        }
        if (!password.equals(confirmPassword)) {
            return RegistrationResult.fail("Hasła nie są takie same!");
        }

        User newUser = new User();
        newUser.setName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassword(hashPassword(password));
        newUser.setRoleId(3);
        newUser.setGroupId(1);
        newUser.setPasswordHint("");

        boolean inserted = userDAO.insertUser(newUser);
        if (!inserted) {
            return RegistrationResult.fail("Rejestracja nie powiodła się! Sprawdź, czy email nie jest już zajęty.");
        }

        return RegistrationResult.success("Rejestracja udana!");
    }

    /**
     * Sprawdza czy tekst zaczyna się od wielkiej litery.
     *
     * @param text tekst do sprawdzenia
     * @return true jeśli tekst zaczyna się od wielkiej litery, false w przeciwnym wypadku
     */
    private boolean isCapitalized(String text) {
        if (text == null || text.isEmpty()) return false;
        return Character.isUpperCase(text.charAt(0));
    }

    /**
     * Weryfikuje poprawność formatu adresu email.
     *
     * @param email adres email do walidacji
     * @return true jeśli email jest w poprawnym formacie, false w przeciwnym wypadku
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String regex = "^.{2,}@.{2,}$";
        return email.matches(regex);
    }

    /**
     * Sprawdza czy hasło zawiera przynajmniej jeden znak specjalny.
     *
     * @param password hasło do sprawdzenia
     * @return true jeśli hasło zawiera znak specjalny, false w przeciwnym wypadku
     */
    private boolean hasSpecialChar(String password) {
        if (password == null) return false;
        return password.matches(".*[^A-Za-z0-9].*");
    }

    /**
     * Haszuje podane hasło algorytmem SHA-256.
     *
     * @param password hasło do zaszyfrowania
     * @return zahashowane hasło jako ciąg znaków hex lub null w przypadku błędu
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}