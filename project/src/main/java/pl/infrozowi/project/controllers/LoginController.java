package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() throws IOException {
        // Prosta weryfikacja "na sztywno"
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Zakładamy, że admin -> rola administratora, manager -> rola kierownik, user -> pracownik
        if (username.equals("admin") && password.equals("admin")) {
            // Przejdź do ekranu Administratora
            switchTo("/fxml/admin/adminDashboard.fxml");
        } else if (username.equals("manager") && password.equals("manager")) {
            // Przejdź do ekranu Kierownika
            switchTo("/fxml/manager/managerDashboard.fxml");
        } else if (username.equals("user") && password.equals("user")) {
            // Przejdź do ekranu Użytkownika
            switchTo("/fxml/user/userDashboard.fxml");
        } else {
            System.out.println("Błędny login lub hasło!");
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        switchTo("/fxml/register.fxml");
    }

    private void switchTo(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
