package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.PasswordChangeService;

import java.io.IOException;

public class SettingsController {

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField passwordHintField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private ComboBox<String> defaultViewComboBox;
    @FXML
    private Button saveSettingsButton;


    private final UserDAO userDAO = new UserDAO();
    private final PasswordChangeService passwordService = new PasswordChangeService();
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        emailField.setText(user.getEmail());
        passwordHintField.setText(user.getPasswordHint() != null ? user.getPasswordHint() : "");

        themeComboBox.getItems().addAll("Light", "Dark");
        themeComboBox.setValue(user.getTheme());

        defaultViewComboBox.getItems().addAll("Moje zadania", "Zadania");
        defaultViewComboBox.setValue(user.getDefaultView());
    }

    @FXML
    private void initialize() {
        saveSettingsButton.setOnAction(e -> handleSaveSettings());
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
        String newEmail = emailField.getText().trim();
        String newTheme = themeComboBox.getValue();
        String newDefaultView = defaultViewComboBox.getValue();

        if (!newEmail.equals(currentUser.getEmail()) && !isValidEmail(newEmail)) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Niepoprawny adres e-mail!");
            return;
        }

        // Obsługa hasła z serwisu
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            try {
                String hashedPassword = passwordService.validateAndHashPassword(newPassword, confirmPassword);
                currentUser.setPassword(hashedPassword);
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", e.getMessage());
                return;
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd przy hashowaniu hasła!");
                return;
            }
        }

        if (!newEmail.equals(currentUser.getEmail())) {
            currentUser.setEmail(newEmail);
        }

        if (!newTheme.equals(currentUser.getTheme())) {
            currentUser.setTheme(newTheme);
        }

        if (!newDefaultView.equals(currentUser.getDefaultView())) {
            currentUser.setDefaultView(newDefaultView);
        }

        if (!newHint.equals(currentUser.getPasswordHint())) {
            currentUser.setPasswordHint(newHint);
        }

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

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
