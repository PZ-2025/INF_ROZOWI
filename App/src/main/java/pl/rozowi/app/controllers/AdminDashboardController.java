package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private void initialize() {
        // Opcjonalnie możesz wczytać domyślny widok (np. Pracownicy) od razu po starcie:
        try {
            goToEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Jeśli chcesz ustawić nazwę administratora dynamicznie
    public void setAdminName(String adminName) {
        welcomeLabel.setText("Witaj, " + adminName);
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
    private void goToReports() throws IOException {
        loadView("/fxml/admin/adminReports.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        // Możesz wczytać np. /fxml/settings.fxml
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

        // Kotwiczenie w AnchorPane, by wypełnić cały obszar
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
