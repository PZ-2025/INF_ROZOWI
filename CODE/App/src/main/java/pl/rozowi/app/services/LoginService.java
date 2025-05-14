package pl.rozowi.app.services;

import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginService {

    private final UserDAO userDAO;
    private final TeamMemberDAO teamMemberDAO;

    public LoginService(UserDAO userDAO, TeamMemberDAO teamMemberDAO) {
        this.userDAO = userDAO;
        this.teamMemberDAO = teamMemberDAO;
    }

    /**
     * Sprawdza, czy użytkownik istnieje oraz czy zahashowane hasło odpowiada.
     * Zwraca obiekt User w przypadku powodzenia lub null, jeśli autoryzacja się nie powiodła.
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
     * Pobiera ID teamu przypisanego do danego usera.
     */
    public int findTeamIdForUser(int userId) {
        return teamMemberDAO.getTeamIdForUser(userId);
    }

    /**
     * Znajduje użytkownika po emailu.
     */
    public User findUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return userDAO.getUserByEmail(email);
    }

    /**
     * Metoda do hashowania hasła (SHA-256).
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