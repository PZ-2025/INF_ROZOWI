package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class ManagerDashboardController {

    // Główny kontener z FXML (fx:id="root")
    @FXML
    private VBox root;

    @FXML
    private void goToTasks() throws IOException {
        switchTo("/fxml/tasks/tasksOverview.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        // np. inny widok raportów
        switchTo("/fxml/tasks/tasksOverview.fxml");
    }

    @FXML
    private void logout() throws IOException {
        switchTo("/fxml/login.fxml");
    }

    private void switchTo(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = (Stage) root.getScene().getWindow(); // pobieramy Stage z bieżącego ekranu
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
