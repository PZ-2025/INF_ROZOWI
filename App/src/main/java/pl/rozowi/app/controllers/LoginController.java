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
import pl.rozowi.app.services.LoginService;
import pl.rozowi.app.util.Session;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField; // email
    @FXML
    private PasswordField passwordField;

    // Zamieniamy userDAO i teamMemberDAO na loginService
    private LoginService loginService;

    public LoginController() {
        // Tworzymy nowy LoginService z DAO
        this.loginService = new LoginService(new UserDAO(), new TeamMemberDAO());
    }

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

        // Używamy loginService do autoryzacji
        User user = loginService.authenticate(email, pass);
        if (user != null) {
            MainApplication.setCurrentUser(user);
            Session.currentUserId = user.getId();
            int teamId = loginService.findTeamIdForUser(user.getId());
            Session.currentUserTeam = String.valueOf(teamId);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logowanie udane");
            alert.setHeaderText(null);
            alert.setContentText("Logowanie udane!");
            alert.showAndWait();

            try {
                switch (user.getRoleId()) {
                    case 1:
                        MainApplication.switchScene("/fxml/admin/adminDashboard.fxml", "TaskApp - Admin");
                        break;
                    case 2:
                        MainApplication.switchScene("/fxml/manager/managerDashboard.fxml", "TaskApp - Manager");
                        break;
                    case 3:
                        MainApplication.switchScene("/fxml/teamleader/teamLeaderDashboard.fxml", "TaskApp - Team Leader");
                        break;
                    case 4:
                        MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - User");
                        break;
                    default:
                        MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - Dashboard");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd logowania");
            alert.setHeaderText(null);
            alert.setContentText("Błędne dane logowania!");
            alert.showAndWait();
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

        // Zamiast userDAO.getUserByEmail(), korzystamy z metody w loginService
        // Musisz więc utworzyć analogiczną metodę w LoginService (np. getUserByEmail).
        User user = loginService.findUserByEmail(email);

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