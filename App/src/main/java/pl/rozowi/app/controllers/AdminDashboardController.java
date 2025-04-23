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

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    private User currentUser;

    @FXML
    private void initialize() {
        // Na starcie ładujemy domyślny widok – "Pracownicy"
        try {
            goToEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) throws IOException {
        this.currentUser = user;
        welcomeLabel.setText("Witaj, " + user.getName() + " (Administrator)");

        String defaultView = user.getDefaultView();
        if (defaultView != null && !defaultView.isEmpty()) {
            switch (defaultView) {
                case "Pracownicy":
                    goToEmployees();
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
                case "Ustawienia":
                    goToSettings();
                    break;
                default:
                    goToEmployees();
                    break;
            }
        } else {
            goToEmployees();
        }
    }

    @FXML
    private void goToEmployees() throws IOException {
        loadView("/fxml/admin/adminEmployees.fxml");
    }

    @FXML
    private void goToTeams() throws IOException {
        loadView("/fxml/admin/adminTeams.fxml");
    }

    @FXML
    private void goToProjects() throws IOException {
        loadView("/fxml/admin/adminProjects.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        loadView("/fxml/admin/adminTasks.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/admin/adminReports.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        loadView("/fxml/user/settings.fxml");
    }

    @FXML
    private void goToSystemConfig() throws IOException {
        loadView("/fxml/admin/config.fxml");
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