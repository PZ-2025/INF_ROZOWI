package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class ManagerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane mainPane;

    public void setManagerName(String managerName) {
        welcomeLabel.setText("Panel Kierownika: " + managerName);
    }

    @FXML
    private void goToTasks() throws IOException {
        loadView("/fxml/tasksOverview.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/manager/managerReports.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
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
