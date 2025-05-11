package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private void initialize() {
        try {
            goToTasks();
        } catch (IOException e) {
            e.printStackTrace();
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
        loadView("/fxml/manager/managerEmployees.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        loadView("/fxml/manager/managerTasks.fxml");
    }

    @FXML
    private void goToTeams() throws IOException {
        loadView("/fxml/manager/managerTeams.fxml");
    }

    @FXML
    private void goToProjects() throws IOException {
        loadView("/fxml/manager/managerProjects.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/manager/managerReports.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
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