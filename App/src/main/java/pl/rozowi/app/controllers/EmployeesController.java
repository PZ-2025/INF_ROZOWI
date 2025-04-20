package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.Session;

import java.util.List;

public class EmployeesController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private TableView<User> employeesTable;
    @FXML private TableColumn<User, Number> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTeam;

    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    private ObservableList<User> allEmployees;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colLastName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colTeam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamName()));

        loadEmployees();
        FilteredList<User> filtered = new FilteredList<>(allEmployees, u -> true);
        employeesTable.setItems(filtered);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = (newVal == null ? "" : newVal.toLowerCase().trim());
            filtered.setPredicate(user -> {
                if (lower.isEmpty()) return true;
                return String.valueOf(user.getId()).contains(lower)
                        || user.getEmail().toLowerCase().contains(lower);
            });
        });
        searchButton.setOnAction(e -> searchField.setText(searchField.getText()));
    }

    private void loadEmployees() {
        int teamId = Integer.parseInt(Session.currentUserTeam);
        List<User> members = teamMemberDAO.getTeamMembers(teamId);
        String teamName = teamDAO.getTeamNameById(teamId);
        for (User u : members) {
            u.setTeamName(teamName);
        }
        allEmployees = FXCollections.observableArrayList(members);
    }
}
