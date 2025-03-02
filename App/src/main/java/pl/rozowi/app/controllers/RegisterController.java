package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField firstNameField, lastNameField, emailField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private Label messageLabel;

    public void initialize() {
        firstNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        lastNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
    }

    @FXML
    private void handleRegister() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            messageLabel.setText("Hasła nie są takie same!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        messageLabel.setText("Rejestracja udana!");
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void goBack() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
    }
}
