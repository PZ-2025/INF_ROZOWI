package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController {

    @FXML
    private TextField usernameField; // email
    @FXML
    private PasswordField passwordField;

    private UserDAO userDAO = new UserDAO();
    private TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

    public void initialize() {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    public void handleLogin() {
        String email = usernameField.getText();
        String pass = passwordField.getText();

        // Hashowanie hasła
        String hashedPassword = hashPassword(pass);

        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getPassword().equals(hashedPassword)) {
            MainApplication.setCurrentUser(user);
            Session.currentUserId = user.getId();
            int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
            Session.currentUserTeam = String.valueOf(teamId);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logowanie udane");
            alert.setHeaderText(null);
            alert.setContentText("Logowanie udane!");
            alert.getDialogPane().setStyle("-fx-background-color: green;");
            alert.showAndWait();

            try {
                switch (user.getRoleId()) {
                    case 1: // Admin
                        MainApplication.switchScene("/fxml/admin/adminDashboard.fxml", "TaskApp - Admin");
                        break;
                    case 2: // Manager
                        MainApplication.switchScene("/fxml/manager/managerDashboard.fxml", "TaskApp - Manager");
                        break;
                    case 3: // Team Leader
                        MainApplication.switchScene("/fxml/teamleader/teamLeaderDashboard.fxml", "TaskApp - Team Leader");
                        break;
                    case 4: // User (Employee)
                        MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - User");
                        break;
                    default:
                        MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - Dashboard");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd logowania");
            alert.setHeaderText(null);
            alert.setContentText("Błędne dane logowania!");
            alert.getDialogPane().setStyle("-fx-background-color: red;");
            alert.showAndWait();
        }
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

    @FXML
    private void goToRegister() throws IOException {
        MainApplication.switchScene("/fxml/register.fxml", "TaskApp - Rejestracja");
    }

    @FXML
    private void forgotPassword() {
        String email = usernameField.getText();
        if (email == null || email.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Brak danych");
            alert.setHeaderText(null);
            alert.setContentText("Podaj swój email w polu Nazwa użytkownika!");
            alert.showAndWait();
            return;
        }
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText(null);
            alert.setContentText("Nie znaleziono użytkownika o podanym emailu!");
            alert.showAndWait();
        } else {
            String hint = user.getPasswordHint();
            if (hint == null || hint.isEmpty()) {
                hint = "Brak podpowiedzi hasła.";
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Podpowiedź hasła");
            alert.setHeaderText(null);
            alert.setContentText("Twoja podpowiedź: " + hint);
            alert.showAndWait();
        }
    }
}