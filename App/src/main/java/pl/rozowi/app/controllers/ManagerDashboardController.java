package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.User;

import java.io.IOException;

public class ManagerDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    private User currentUser;

    @FXML
    private void initialize() {
        // Na starcie ładujemy domyślny widok – "Zadania"
        try {
            goToTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setUser(User user) throws IOException {
        this.currentUser = user;
        welcomeLabel.setText("Witaj, " + user.getName());

        String def = user.getDefaultView();
        if (def != null) {
            switch (def) {
                case "Pracownicy":
                    goToEmployees();
                    return;
                case "Zadania":
                    goToTasks();
                    return;
                case "Ustawienia":
                    goToSettings();
                    return;
                case "Zespoły":
                    goToTeams();
                    return;
                default:
                    goToProjects();
                    return;
            }
        }
        goToProjects();
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
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent view = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SettingsController) {
            ((SettingsController) controller).setUser(currentUser);
        }

        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
