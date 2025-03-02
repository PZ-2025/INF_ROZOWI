package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.rozowi.app.MainApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> roles = new HashMap<>();

    public void initialize() {
        users.put("admin1", "password");
        users.put("admin2", "password");
        users.put("admin3", "password");

        roles.put("admin1", "/fxml/userDashboard.fxml");
        roles.put("admin2", "/fxml/managerDashboard.fxml");
        roles.put("admin3", "/fxml/adminDashboard.fxml");

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            errorLabel.setText("Logowanie udane!");
            errorLabel.setStyle("-fx-text-fill: green;");
            try {
                MainApplication.switchScene(roles.get(username), "TaskApp - Dashboard");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Błędne dane logowania!");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        MainApplication.switchScene("/fxml/register.fxml", "TaskApp - Rejestracja");
    }
}
