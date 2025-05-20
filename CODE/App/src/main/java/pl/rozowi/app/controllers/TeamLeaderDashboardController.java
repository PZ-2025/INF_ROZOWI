package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;

/**
 * Kontroler panelu głównego dla lidera zespołu.
 * Rozszerza funkcjonalność BaseDashboardController, dostosowując ją do potrzeb lidera zespołu.
 * Zawiera nawigację po modułach dostępnych dla lidera zespołu.
 */
public class TeamLeaderDashboardController extends BaseDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private Button myTasksButton;
    @FXML
    private Button tasksButton;
    @FXML
    private Button employeesButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    private static final String ACTIVE_BUTTON_STYLE = "sidebar-button-active";

    /**
     * Inicjalizacja kontrolera.
     * Metoda wywoływana automatycznie po załadowaniu pliku FXML.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Ustawia styl aktywnego przycisku w pasku bocznym.
     * @param activeButton Przycisk, który ma zostać oznaczony jako aktywny
     */
    private void setActiveButton(Button activeButton) {
        myTasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        tasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        employeesButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        reportsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        settingsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);

        if (activeButton != null) {
            activeButton.getStyleClass().add(ACTIVE_BUTTON_STYLE);
        }
    }

    /**
     * Metoda wywoływana po ustawieniu użytkownika.
     * Inicjalizuje panel na podstawie domyślnego widoku użytkownika.
     * @param user Obiekt użytkownika zalogowanego do systemu
     */
    @Override
    protected void onUserSet(User user) {
        welcomeLabel.setText("Witaj, " + user.getName());

        try {
            String def = user.getDefaultView();
            if (def != null) {
                switch (def) {
                    case "Moje zadania":
                        goToMyTasks();
                        break;
                    case "Zadania zespołu":
                        goToTasks();
                        break;
                    case "Pracownicy":
                        goToEmployees();
                        break;
                    case "Raporty":
                        goToReports();
                        break;
                    case "Ustawienia":
                        goToSettings();
                        break;
                    default:
                        goToMyTasks();
                        break;
                }
            } else {
                goToMyTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pobiera scenę główną panelu.
     * @return Obiekt sceny
     */
    @Override
    protected Scene getScene() {
        return mainPane != null ? mainPane.getScene() : null;
    }

    /**
     * Przechodzi do widoku moich zadań.
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    @FXML
    private void goToMyTasks() throws IOException {
        setActiveButton(myTasksButton);
        loadView("/fxml/teamleader/teamLeaderMyTasks.fxml");
    }

    /**
     * Przechodzi do widoku zadań zespołu.
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    @FXML
    private void goToTasks() throws IOException {
        setActiveButton(tasksButton);
        loadView("/fxml/teamleader/teamLeaderTasks.fxml");
    }

    /**
     * Przechodzi do widoku raportów.
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    @FXML
    private void goToReports() throws IOException {
        setActiveButton(reportsButton);
        try {
            loadView("/fxml/teamleader/teamLeaderReports.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText(null);
            alert.setContentText("Nie można załadować widoku raportów: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Przechodzi do widoku pracowników.
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    @FXML
    private void goToEmployees() throws IOException {
        setActiveButton(employeesButton);
        loadView("/fxml/teamleader/teamLeaderEmployees.fxml");
    }

    /**
     * Przechodzi do widoku ustawień.
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    @FXML
    private void goToSettings() throws IOException {
        setActiveButton(settingsButton);
        loadView("/fxml/user/settings.fxml");
    }

    /**
     * Wylogowuje użytkownika i przekierowuje do ekranu logowania.
     * @throws IOException gdy wystąpi błąd ładowania widoku logowania
     */
    @FXML
    private void logout() throws IOException {
        MainApplication.setCurrentUser(null);
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    /**
     * Ładuje widok w głównym panelu.
     * @param fxmlPath Ścieżka do pliku FXML z definicją widoku
     * @throws IOException gdy wystąpi błąd ładowania widoku
     */
    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent view = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SettingsController) {
            ((SettingsController) controller).setUser(currentUser);
        } else if (controller instanceof UserAwareController) {
            ((UserAwareController) controller).setUser(currentUser);
        }

        mainPane.getChildren().setAll(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);

        Platform.runLater(() -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.sizeToScene();
        });
    }
}