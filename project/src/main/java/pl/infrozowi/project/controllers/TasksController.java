package pl.infrozowi.project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class TasksController {

    @FXML
    private BorderPane root; // fx:id="root" w tasksOverview.fxml

    @FXML
    private TableView<?> tasksTable;

    @FXML
    private TextField filterField;

    @FXML
    private void handleAddTask() {
        System.out.println("Dodawanie nowego zadania...");
    }

    @FXML
    private void handleFilter() {
        String filterText = filterField.getText();
        System.out.println("Filtruj zadania po: " + filterText);
    }

    @FXML
    private void goBack() throws IOException {
        // W zależności skąd przyszedłeś, możesz wrócić do Admin/Manager/User.
        // Na przykład na poprzedni widok (trzeba samemu ustalić logikę).
        // Tu "na sztywno" wrócimy do managera:
        switchTo("/fxml/manager/managerDashboard.fxml");
    }

    private void switchTo(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = (Stage) root.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
