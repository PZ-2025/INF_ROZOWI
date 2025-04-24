package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.User;

import java.io.IOException;

public class TeamLeaderDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;


    public void setUser(User user) throws IOException {
        welcomeLabel.setText("Witaj, " + user.getName());

        String def = user.getDefaultView();
        if (def != null) {
            switch (def) {
                case "Moje zadania":
                    goToMyTasks();
                    break;
                case "Zadania":
                    goToTasks();
                    break;
                case "Ustawienia":
                    goToSettings();
                    break;
                case "Powiadomienia":
                    goToNotifications();
                    break;
                default:
                    goToMyTasks();
                    break;
            }
            return;
        }
        goToMyTasks();
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void goToNotifications() throws IOException {
        loadView("/fxml/notifications.fxml");
    }

    @FXML
    private void goToMyTasks() throws IOException {
        loadView("/fxml/teamleader/teamLeaderMyTasks.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        loadView("/fxml/teamleader/teamLeaderTasks.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/teamleader/teamLeaderReports.fxml");
    }

    @FXML
    private void goToEmployees() throws IOException {
        loadView("/fxml/teamleader/teamLeaderEmployees.fxml");
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

        var ctrl = loader.getController();
        if (ctrl instanceof SettingsController) {
            ((SettingsController) ctrl).setUser(MainApplication.getCurrentUser());
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
