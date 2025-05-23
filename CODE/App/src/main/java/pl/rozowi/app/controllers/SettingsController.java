package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.models.User;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.services.PasswordChangeService;
import pl.rozowi.app.services.ActivityService;
import pl.rozowi.app.util.ThemeManager;
import pl.rozowi.app.util.DefaultViewManager;

import java.io.IOException;

/**
 * Kontroler odpowiedzialny za obsługę panelu ustawień użytkownika.
 * Pozwala na zmianę hasła, podpowiedzi do hasła, adresu email, motywu oraz domyślnego widoku.
 */
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
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final PasswordChangeService passwordService = new PasswordChangeService();
    private User currentUser;

    /**
     * Ustawia aktualnego użytkownika i inicjalizuje formularz jego danymi.
     *
     * @param user obiekt użytkownika, którego ustawienia będą edytowane
     */
    public void setUser(User user) {
        this.currentUser = user;
        emailField.setText(user.getEmail());
        passwordHintField.setText(user.getPasswordHint() != null ? user.getPasswordHint() : "");

        themeComboBox.getItems().setAll("Light", "Dark");
        themeComboBox.setValue(user.getTheme() != null ? user.getTheme() : "Light");

        defaultViewComboBox.getItems().clear();

        switch (user.getRoleId()) {
            case 1:
                defaultViewComboBox.getItems().addAll(
                        "Użytkownicy",
                        "Zespoły",
                        "Projekty",
                        "Zadania",
                        "Raporty",
                        "Aktywność",
                        "System",
                        "Ustawienia"
                );
                break;
            case 2:
                defaultViewComboBox.getItems().addAll(
                        "Pracownicy",
                        "Projekty",
                        "Monitor",
                        "Zespoły",
                        "Raporty",
                        "Ustawienia"
                );
                break;
            case 3:
                defaultViewComboBox.getItems().addAll(
                        "Moje zadania",
                        "Zadania zespołu",
                        "Pracownicy",
                        "Raporty",
                        "Ustawienia"
                );
                break;
            case 4:
                defaultViewComboBox.getItems().addAll(
                        "Moje zadania",
                        "Zadania",
                        "Ustawienia"
                );
                break;
        }

        if (user.getDefaultView() != null && defaultViewComboBox.getItems().contains(user.getDefaultView())) {
            defaultViewComboBox.setValue(user.getDefaultView());
        } else {
            if (!defaultViewComboBox.getItems().isEmpty()) {
                defaultViewComboBox.setValue(defaultViewComboBox.getItems().get(0));
            }
        }

        themeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                Scene currentScene = themeComboBox.getScene();
                if (currentScene != null) {
                    ThemeManager.changeTheme(currentScene, newVal);
                }
            }
        });
    }

    /**
     * Inicjalizacja kontrolera - ustawia obsługę zdarzenia dla przycisku zapisywania.
     */
    @FXML
    private void initialize() {
        saveSettingsButton.setOnAction(e -> handleSaveSettings());
    }

    /**
     * Obsługuje zdarzenie zapisywania ustawień.
     * Waliduje dane, aktualizuje użytkownika i przełącza na odpowiedni widok w zależności od roli.
     */
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

        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            try {
                String hashed = passwordService.validateAndHashPassword(newPassword, confirmPassword);
                currentUser.setPassword(hashed);
                if (!newPassword.isEmpty()) {
                    ActivityService.logPasswordChange(currentUser.getId(), false);
                }
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Błąd", ex.getMessage());
                return;
            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Błąd", "Wystąpił błąd przy hashowaniu hasła!");
                return;
            }
        }

        currentUser.setEmail(newEmail);
        currentUser.setPasswordHint(newHint);

        ThemeManager.saveThemeToDatabase(currentUser, newTheme);
        DefaultViewManager.saveDefaultView(currentUser, newDefaultView);

        boolean success = userDAO.updateUser(currentUser);
        if (!success) {
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się zapisać zmian użytkownika!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zmiany zapisane pomyślnie!");

        MainApplication.setCurrentUser(currentUser);

        try {
            Stage stage = (Stage) saveSettingsButton.getScene().getWindow();
            switch (currentUser.getRoleId()) {
                case 1 -> MainApplication.switchScene(
                        "/fxml/admin/adminDashboard.fxml", "TaskApp - Admin");
                case 2 -> MainApplication.switchScene(
                        "/fxml/manager/managerDashboard.fxml", "TaskApp - Kierownik");
                case 3 -> {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/teamleader/teamLeaderDashboard.fxml"));
                    Parent root = loader.load();
                    TeamLeaderDashboardController ctrl =
                            loader.getController();
                    ctrl.setUser(currentUser);

                    Scene scene = new Scene(root);
                    ThemeManager.applyTheme(scene, currentUser);

                    stage.setScene(scene);
                    stage.setTitle("TaskApp - Team Leader");
                }
                case 4 -> {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/user/userDashboard.fxml"));
                    Parent root = loader.load();
                    UserDashboardController ctrl =
                            loader.getController();
                    ctrl.setUser(currentUser);

                    Scene scene = new Scene(root);
                    ThemeManager.applyTheme(scene, currentUser);

                    stage.setScene(scene);
                    stage.setTitle("TaskApp - User");
                }
                default -> MainApplication.switchScene(
                        "/fxml/user/userDashboard.fxml", "TaskApp - Panel");
            }
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Sprawdza poprawność formatu adresu email.
     *
     * @param email adres email do walidacji
     * @return true jeśli email jest poprawny, false w przeciwnym wypadku
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Wyświetla okno dialogowe z komunikatem.
     *
     * @param type typ komunikatu (ERROR, INFORMATION itp.)
     * @param title tytuł okna dialogowego
     * @param content treść komunikatu
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}