package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class UserDashboardController {

    @FXML
    private VBox root; // fx:id="root" w userDashboard.fxml

    @FXML
    private void goToTasks() throws IOException {
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
