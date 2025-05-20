package pl.rozowi.app.services;

import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Klasa odpowiedzialna za logowanie użytkowników do systemu.
 * Zapewnia funkcjonalność uwierzytelniania użytkowników oraz zarządzania ich sesjami.
 */
public class LoginService {

    private final UserDAO userDAO;
    private final TeamMemberDAO teamMemberDAO;

    /**
     * Konstruktor tworzący serwis logowania.
     *
     * @param userDAO DAO do obsługi użytkowników
     * @param teamMemberDAO DAO do obsługi członków zespołów
     */
    public LoginService(UserDAO userDAO, TeamMemberDAO teamMemberDAO) {
        this.userDAO = userDAO;
        this.teamMemberDAO = teamMemberDAO;
    }

    /**
     * Uwierzytelnia użytkownika na podstawie adresu email i hasła.
     *
     * @param email adres email użytkownika
     * @param rawPassword hasło w postaci niezaszyfrowanej
     * @return obiekt User w przypadku poprawnego uwierzytelnienia, null w przeciwnym wypadku
     */
    public User authenticate(String email, String rawPassword) {
        if (email == null || email.isEmpty() || rawPassword == null) {
            return null;
        }
        String hashed = hashPassword(rawPassword);

        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getPassword() != null && user.getPassword().equals(hashed)) {
            return user;
        }
        return null;
    }

    /**
     * Pobiera identyfikator zespołu przypisanego do użytkownika.
     *
     * @param userId identyfikator użytkownika
     * @return identyfikator zespołu lub wartość domyślna jeśli użytkownik nie należy do żadnego zespołu
     */
    public int findTeamIdForUser(int userId) {
        return teamMemberDAO.getTeamIdForUser(userId);
    }

    /**
     * Wyszukuje użytkownika na podstawie adresu email.
     *
     * @param email adres email użytkownika
     * @return obiekt User jeśli znaleziono, null w przeciwnym wypadku
     */
    public User findUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return userDAO.getUserByEmail(email);
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
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}