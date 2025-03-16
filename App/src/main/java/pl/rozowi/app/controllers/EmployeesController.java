package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.models.User;

public class EmployeesController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML

    private TableView<User> employeesTable;
    @FXML
    private TableColumn<User, Number> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colLastName;
    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private void initialize() {
        // Tutaj ustawiasz cellValueFactory itp.
        // colId.setCellValueFactory(...);
        // colName.setCellValueFactory(...);
        // colLastName.setCellValueFactory(...);
        // colEmail.setCellValueFactory(...);
    }

    @FXML
    private void handleSearch() {
        String filter = searchField.getText();
        System.out.println("Szukaj pracownika: " + filter);
        // Tutaj możesz zaimplementować logikę wyszukiwania w bazie
    }
}
