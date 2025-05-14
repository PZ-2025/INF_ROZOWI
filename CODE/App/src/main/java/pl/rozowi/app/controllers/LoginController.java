package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.LoginService;
import pl.rozowi.app.util.Session;
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private LoginService loginService;

    public LoginController() {
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

        User user = loginService.authenticate(email, pass);
        if (user == null) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Błędne dane logowania!");
            err.setHeaderText(null);
            err.showAndWait();
            return;
        }

        MainApplication.setCurrentUser(user);
        Session.currentUserId = user.getId();
        Session.currentUserTeam = String.valueOf(loginService.findTeamIdForUser(user.getId()));

        Alert ok = new Alert(Alert.AlertType.INFORMATION, "Logowanie udane!");
        ok.setHeaderText(null);
        ok.showAndWait();

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene;
            Parent root;

            switch (user.getRoleId()) {
                case 2 -> {  
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/manager/managerDashboard.fxml"));
                    root = loader.load();
                    ManagerDashboardController ctrl = loader.getController();
                    ctrl.setUser(user);
                    scene = new Scene(root);
                    ThemeManager.applyTheme(scene, user);
                    stage.setScene(scene);
                    stage.setTitle("TaskApp - Kierownik");
                    stage.show();
                }
                case 3 -> { 
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/teamleader/teamLeaderDashboard.fxml"));
                    root = loader.load();
                    TeamLeaderDashboardController ctrl = loader.getController();
                    ctrl.setUser(user);
                    scene = new Scene(root);
                    ThemeManager.applyTheme(scene, user);
                    stage.setScene(scene);
                    stage.setTitle("TaskApp - Team Leader");
                    stage.show();
                }
                case 4 -> { 
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/user/userDashboard.fxml"));
                    root = loader.load();
                    UserDashboardController ctrl = loader.getController();
                    ctrl.setUser(user);
                    scene = new Scene(root);
                    ThemeManager.applyTheme(scene, user);
                    stage.setScene(scene);
                    stage.setTitle("TaskApp - User");
                    stage.show();
                }
                case 1 -> {
                    Scene currentScene = stage.getScene();
                    MainApplication.switchScene("/fxml/admin/adminDashboard.fxml", "TaskApp - Admin");
                }
                default -> {
                    MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - Panel");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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