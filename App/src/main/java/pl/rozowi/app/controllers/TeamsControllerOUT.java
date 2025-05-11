package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TeamsControllerOUT {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<?> teamsTable;
    @FXML
    private TableColumn<?, ?> colTeamId;
    @FXML
    private TableColumn<?, ?> colTeamName;
    @FXML
    private TableColumn<?, ?> colMembersCount;


    @FXML
    private void initialize() {
    }

    @FXML
    private void handleSearch() {
        String filter = searchField.getText();
        System.out.println("Szukaj zespołu: " + filter);
    }
}
