package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;

import java.io.IOException;

public class TeamLeaderDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private void initialize() {
        // Na starcie ładujemy widok "Moje Zadania" dla team leadera
        try {
            goToMyTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTeamLeaderName(String name) {
        welcomeLabel.setText("Witaj, " + name);
    }

    @FXML
    private void goToMyTasks() throws IOException {
        // Ładujemy widok "Moje Zadania"
        loadView("/fxml/teamleader/teamLeaderMyTasks.fxml");
    }

    @FXML
    private void goToTasks() throws IOException {
        // Ładujemy widok "Zadania"
        loadView("/fxml/teamleader/teamLeaderTasks.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        // Ładujemy widok "Raporty"
        loadView("/fxml/teamleader/teamLeaderReports.fxml");
    }

    @FXML
    private void goToEmployees() throws IOException {
        // Ładujemy widok "Pracownicy"
        loadView("/fxml/teamleader/teamLeaderEmployees.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        // Używamy widoku ustawień z panelu użytkownika
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
        // Ustawienie, aby widok wypełniał cały obszar mainPane
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
