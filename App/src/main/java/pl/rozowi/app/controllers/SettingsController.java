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

    // Metoda wywoływana z dashboardu, aby ustawić aktualnego użytkownika
    public void setUser(User user) {
        this.currentUser = user;
        // Jeśli istnieje zapisana podpowiedź, ustaw ją w polu
        passwordHintField.setText(user.getPasswordHint() != null ? user.getPasswordHint() : "");
    }

    @FXML
    private void initialize() {
        saveSettingsButton.setOnAction(e -> handleSaveSettings());
    }

    private void handleSaveSettings() {
        if(currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie znaleziono danych użytkownika!");
            return;
        }
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String newHint = passwordHintField.getText().trim();

        if(newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Pole hasła nie może być puste!");
            return;
        }
        if(!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Hasła nie są takie same!");
            return;
        }

        // Tutaj można dodać dodatkową walidację (np. minimalna długość, znak specjalny, itd.)

        // Aktualizacja danych użytkownika
        currentUser.setPassword(newPassword);
        currentUser.setPasswordHint(newHint);

        boolean success = userDAO.updateUser(currentUser);
        if(success) {
            showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zmiany zapisane pomyślnie!");
            try {
                MainApplication.switchScene("/fxml/user/userDashboard.fxml", "TaskApp - Dashboard");
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zapisać zmian!");
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
