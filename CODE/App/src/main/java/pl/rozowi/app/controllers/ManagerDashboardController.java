package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;

/**
 * Kontroler panelu zarządzania dla roli Kierownika.
 * Rozszerza funkcjonalność klasy bazowej BaseDashboardController,
 * dostosowując interfejs do potrzeb użytkowników z uprawnieniami kierowniczymi.
 */
public class ManagerDashboardController extends BaseDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private Button employeesButton;
    @FXML
    private Button projectsButton;
    @FXML
    private Button tasksButton;
    @FXML
    private Button teamsButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    private static final String ACTIVE_BUTTON_STYLE = "sidebar-button-active";

    /**
     * Inicjalizuje kontroler, domyślnie ładując widok zadań.
     */
    @FXML
    private void initialize() {
        try {
            goToTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ustawia aktywny przycisk w pasku bocznym poprzez zmianę stylu.
     * @param activeButton Przycisk, który ma zostać oznaczony jako aktywny
     */
    private void setActiveButton(Button activeButton) {
        employeesButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        projectsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        tasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        teamsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        reportsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        settingsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);

        if (activeButton != null) {
            activeButton.getStyleClass().add(ACTIVE_BUTTON_STYLE);
        }
    }

    /**
     * Metoda wywoływana po ustawieniu użytkownika.
     * Aktualizuje powitanie i ładuje domyślny widok zgodnie z preferencjami użytkownika.
     * @param user Obiekt zalogowanego użytkownika
     */
    @Override
    protected void onUserSet(User user) {
        welcomeLabel.setText("Witaj, " + user.getName());

        try {
            String def = user.getDefaultView();
            if (def != null) {
                switch (def) {
                    case "Pracownicy":
                        goToEmployees();
                        break;
                    case "Projekty":
                        goToProjects();
                        break;
                    case "Zespoły":
                        goToTeams();
                        break;
                    case "Raporty":
                        goToReports();
                        break;
                    case "Ustawienia":
                        goToSettings();
                        break;
                    default:
                        goToProjects();
                        break;
                }
            } else {
                goToProjects();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pobiera scenę główną panelu.
     * @return Obiekt sceny lub null jeśli nie został jeszcze zainicjalizowany
     */
    @Override
    protected Scene getScene() {
        return mainPane != null ? mainPane.getScene() : null;
    }

    /**
     * Ładuje widok zarządzania pracownikami.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToEmployees() throws IOException {
        setActiveButton(employeesButton);
        loadView("/fxml/manager/managerEmployees.fxml");
    }

    /**
     * Ładuje widok zarządzania zadaniami.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToTasks() throws IOException {
        setActiveButton(tasksButton);
        loadView("/fxml/manager/managerTasks.fxml");
    }

    /**
     * Ładuje widok zarządzania zespołami.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToTeams() throws IOException {
        setActiveButton(teamsButton);
        loadView("/fxml/manager/managerTeams.fxml");
    }

    /**
     * Ładuje widok zarządzania projektami.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToProjects() throws IOException {
        setActiveButton(projectsButton);
        loadView("/fxml/manager/managerProjects.fxml");
    }

    /**
     * Ładuje widok raportów.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToReports() throws IOException {
        setActiveButton(reportsButton);
        loadView("/fxml/manager/managerReports.fxml");
    }

    /**
     * Ładuje widok ustawień użytkownika.
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
     */
    @FXML
    private void goToSettings() throws IOException {
        setActiveButton(settingsButton);
        loadView("/fxml/user/settings.fxml");
    }

    /**
     * Wylogowuje użytkownika i przekierowuje do ekranu logowania.
     * @throws IOException w przypadku problemów z załadowaniem widoku logowania
     */
    @FXML
    private void logout() throws IOException {
        MainApplication.setCurrentUser(null);
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    /**
     * Ładuje określony widok w głównym obszarze panelu.
     * @param fxmlPath Ścieżka do pliku FXML z definicją widoku
     * @throws IOException w przypadku problemów z załadowaniem pliku FXML
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

        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}