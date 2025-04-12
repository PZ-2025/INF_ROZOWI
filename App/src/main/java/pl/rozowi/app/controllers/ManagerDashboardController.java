package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class ManagerDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private void initialize() {
        // Na starcie ładujemy domyślny widok – "Zadania"
        try {
            goToTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setManagerName(String managerName) {
        welcomeLabel.setText("Panel Kierownika: " + managerName);
    }

    @FXML
    private void goToTasks() throws IOException {
        // Ładujemy widok z zadaniami (dla kierownika)
        loadView("/fxml/manager/managerTasks.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        // Ładujemy widok raportów
        loadView("/fxml/manager/managerReports.fxml");
    }

    @FXML
    private void goToEmployees() throws IOException {
        // Ładujemy widok pracowników
        loadView("/fxml/manager/managerEmployees.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        // Używamy widoku ustawień (możesz użyć tego samego co w panelu użytkownika)
        loadView("/fxml/user/settings.fxml");
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
        // Ustawienie widoku, aby wypełniał cały obszar mainPane
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
