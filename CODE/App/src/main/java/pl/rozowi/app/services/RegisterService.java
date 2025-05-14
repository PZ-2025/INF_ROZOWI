package pl.rozowi.app.services;

import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterService {

    private final UserDAO userDAO;

    public RegisterService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Metoda rejestruje nowego użytkownika z przekazanymi danymi.
     * Zwraca obiekt z informacją o wyniku (np. status, komunikat).
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

    private boolean isCapitalized(String text) {
        if (text == null || text.isEmpty()) return false;
        return Character.isUpperCase(text.charAt(0));
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String regex = "^.{2,}@.{2,}$";
        return email.matches(regex);
    }

    private boolean hasSpecialChar(String password) {
        if (password == null) return false;
        return password.matches(".*[^A-Za-z0-9].*");
    }

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