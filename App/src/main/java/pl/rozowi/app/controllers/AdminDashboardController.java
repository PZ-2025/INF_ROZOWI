package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane mainPane;

    public void setAdminName(String adminName) {
        welcomeLabel.setText("Panel Administratora: " + adminName);
    }

    @FXML
    private void goToUserManagement() throws IOException {
        loadView("/fxml/admin/userManagement.fxml");
    }

    @FXML
    private void goToTasksManagement() throws IOException {
        loadView("/fxml/admin/tasksManagement.fxml");
    }

    @FXML
    private void goToConfig() throws IOException {
        loadView("/fxml/admin/config.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/admin/adminReports.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent view = loader.load();

        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);

        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
