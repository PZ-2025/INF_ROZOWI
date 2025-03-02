package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane mainPane; // Główna przestrzeń dla zmieniających się widoków

    // Wywoływane np. przy przechodzeniu z LoginController, jeśli chcesz przekazać nazwę użytkownika
    public void setUser(String username) {
        welcomeLabel.setText("Witaj, " + username);
    }

    @FXML
    private void goToMyTasks() throws IOException {
        loadView("/fxml/myTasks.fxml");
    }

    @FXML
    private void goToAllTasks() throws IOException {
        loadView("/fxml/tasksOverview.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        loadView("/fxml/settings.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();
        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);

        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
