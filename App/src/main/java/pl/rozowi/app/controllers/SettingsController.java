package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SettingsController {

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField passwordHintField;
    @FXML
    private Button saveSettingsButton;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;
    
    public void setUser(User user) {
        this.currentUser = user;
        passwordHintField.setText(user.getPasswordHint() != null ? user.getPasswordHint() : "");
    }

    @FXML
    private void initialize() {
        // saveSettingsButton.setOnAction(e -> handleSaveSettings());
    }

    @FXML
    private void handleSaveSettings() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono danych użytkownika!");
            return;
        }
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String newHint = passwordHintField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Pole hasła nie może być puste!");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Hasła nie są takie same!");
            return;
        }

        String hashedPassword = hashPassword(newPassword);
        if (hashedPassword == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd przy hashowaniu hasła!");
            return;
        }

        currentUser.setPassword(hashedPassword);
        currentUser.setPasswordHint(newHint);

        boolean success = userDAO.updateUser(currentUser);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zmiany zapisane pomyślnie!");
            try {
                MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - Dashboard");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zapisać zmian!");
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}