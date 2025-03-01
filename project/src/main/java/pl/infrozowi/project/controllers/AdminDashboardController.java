package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private VBox root; // fx:id="root" w adminDashboard.fxml

    @FXML
    private void goToUserManagement() throws IOException {
        switchTo("/fxml/admin/userManagement.fxml");
    }

    @FXML
    private void goToConfig() throws IOException {
        switchTo("/fxml/admin/config.fxml");
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
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
