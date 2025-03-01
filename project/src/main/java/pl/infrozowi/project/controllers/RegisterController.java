package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField firstNameField, lastNameField, emailField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private void handleRegister() {
        // Walidacja na sztywno
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            System.out.println("Hasła nie są takie same!");
            return;
        }
        System.out.println("Zarejestrowano: " + firstNameField.getText() + " " + lastNameField.getText()
                + ", email: " + emailField.getText());
        // Tu w docelowej wersji zapis do bazy itp.
    }

    @FXML
    private void goBack() throws IOException {
        switchTo("/fxml/login.fxml");
    }

    private void switchTo(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
