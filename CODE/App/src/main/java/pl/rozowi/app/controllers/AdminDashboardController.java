package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;

public class AdminDashboardController extends BaseDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private Button usersButton;
    @FXML
    private Button teamsButton;
    @FXML
    private Button projectsButton;
    @FXML
    private Button tasksButton;
    @FXML
    private Button reportsButton;
    @FXML
    private Button activitiesButton;
    @FXML
    private Button systemButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    private static final String ACTIVE_BUTTON_STYLE = "sidebar-button-active";

    @FXML
    private void initialize() {
        try {
            goToUsers();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd podczas ładowania widoku");
        }
    }

    private void setActiveButton(Button activeButton) {
        usersButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        teamsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        projectsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        tasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        reportsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        activitiesButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        systemButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        settingsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);

        if (activeButton != null) {
            activeButton.getStyleClass().add(ACTIVE_BUTTON_STYLE);
        }
    }

    @Override
    protected void onUserSet(User user) {
        welcomeLabel.setText("Witaj, " + user.getName());

        try {
            String def = user.getDefaultView();
            if (def != null) {
                switch (def) {
                    case "Użytkownicy":
                        goToUsers();
                        break;
                    case "Zespoły":
                        goToTeams();
                        break;
                    case "Projekty":
                        goToProjects();
                        break;
                    case "Zadania":
                        goToTasks();
                        break;
                    case "Raporty":
                        goToReports();
                        break;
                    case "Aktywność":
                        goToActivities();
                        break;
                    case "System":
                        goToSystem();
                        break;
                    case "Ustawienia":
                        goToSettings();
                        break;
                    default:
                        goToUsers();
                        break;
                }
            } else {
                goToUsers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Scene getScene() {
        return mainPane != null ? mainPane.getScene() : null;
    }

    @FXML
    private void goToUsers() throws IOException {
        setActiveButton(usersButton);
        loadView("/fxml/admin/adminUsers.fxml");
    }

    @FXML
    private void goToTeams() throws IOException {
        setActiveButton(teamsButton);
        loadView("/fxml/admin/adminTeams.fxml");
    }

    @FXML
    private void goToProjects() throws IOException {
        setActiveButton(projectsButton);
        loadView("/fxml/admin/adminProjects.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        setActiveButton(tasksButton);
        loadView("/fxml/admin/adminTasks.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        setActiveButton(reportsButton);
        loadView("/fxml/admin/adminReports.fxml");
    }

    @FXML
    private void goToActivities() throws IOException {
        setActiveButton(activitiesButton);
        loadView("/fxml/admin/adminActivities.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        setActiveButton(settingsButton);
        loadView("/fxml/user/settings.fxml");
    }

    @FXML
    private void goToSystem() throws IOException {
        setActiveButton(systemButton);
        loadView("/fxml/admin/adminSystem.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.setCurrentUser(null);
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

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

        Platform.runLater(() -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.sizeToScene();
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}