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

    @FXML
    private void initialize() {
        try {
            goToTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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


    @Override
    protected Scene getScene() {
        return mainPane != null ? mainPane.getScene() : null;
    }

    @FXML
    private void goToEmployees() throws IOException {
        setActiveButton(employeesButton);
        loadView("/fxml/manager/managerEmployees.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        setActiveButton(tasksButton);
        loadView("/fxml/manager/managerTasks.fxml");
    }

    @FXML
    private void goToTeams() throws IOException {
        setActiveButton(teamsButton);
        loadView("/fxml/manager/managerTeams.fxml");
    }

    @FXML
    private void goToProjects() throws IOException {
        setActiveButton(projectsButton);
        loadView("/fxml/manager/managerProjects.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        setActiveButton(reportsButton);
        loadView("/fxml/manager/managerReports.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        setActiveButton(settingsButton);
        loadView("/fxml/user/settings.fxml");
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
    }
}