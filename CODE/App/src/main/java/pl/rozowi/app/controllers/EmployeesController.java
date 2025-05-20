package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kontroler odpowiedzialny za zarządzanie widokiem listy pracowników.
 * Umożliwia przeglądanie pracowników z podziałem na zespoły z możliwością wyszukiwania.
 */
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
    private TableColumn<User, String> colTeam;

    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    private ObservableList<User> allEmployees;

    /**
     * Inicjalizuje kontroler. Konfiguruje tabelę pracowników i mechanizm wyszukiwania.
     * Wywoływana automatycznie po załadowaniu pliku FXML.
     * @throws SQLException w przypadku problemów z dostępem do bazy danych
     */
    @FXML
    private void initialize() throws SQLException {
        colId.setCellValueFactory(c -> {
            int index = allEmployees.indexOf(c.getValue()) + 1;
            return new SimpleIntegerProperty(index);
        });

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
                        || user.getName().toLowerCase().contains(lower)
                        || user.getLastName().toLowerCase().contains(lower)
                        || user.getEmail().toLowerCase().contains(lower)
                        || user.getTeamName().toLowerCase().contains(lower);
            });
        });
        searchButton.setOnAction(e -> searchField.setText(searchField.getText()));
    }

    /**
     * Ładuje listę pracowników w zależności od roli obecnie zalogowanego użytkownika:
     * - Administrator widzi wszystkich pracowników
     * - Kierownik widzi pracowników ze swoich zespołów
     * - Team Leader widzi członków swoich zespołów (z wyłączeniem siebie)
     * @throws SQLException w przypadku problemów z dostępem do bazy danych
     */
    private void loadEmployees() throws SQLException {
        User current = MainApplication.getCurrentUser();
        if (current == null) {
            allEmployees = FXCollections.observableArrayList();
            return;
        }

        int role = current.getRoleId();
        List<User> users = new ArrayList<>();

        switch (role) {
            case 3:
                List<Integer> teamIds = teamDAO.getTeamsByLeaderId(current.getId());

                Set<Integer> processedUserIds = new HashSet<>();

                for (Integer teamId : teamIds) {
                    List<User> teamMembers = teamDAO.getTeamMembers(teamId);

                    for (User user : teamMembers) {
                        if (!processedUserIds.contains(user.getId())) {
                            processedUserIds.add(user.getId());
                            users.add(user);
                        }
                    }
                }

                int currentUserId = current.getId();
                users = users.stream()
                        .filter(user -> user.getId() != currentUserId)
                        .collect(Collectors.toList());
                break;

            case 2:
                List<Team> managerTeams = teamDAO.getTeamsForManager(current.getId());
                Set<Integer> managersUserIds = new HashSet<>();

                for (Team team : managerTeams) {
                    List<User> teamMembers = teamDAO.getTeamMembers(team.getId());
                    for (User user : teamMembers) {
                        if (!managersUserIds.contains(user.getId())) {
                            managersUserIds.add(user.getId());
                            users.add(user);
                        }
                    }
                }
                break;

            case 1:
                users = userDAO.getAllUsers();
                break;

            default:
                users = List.of();
        }

        for (User u : users) {
            int tId = teamMemberDAO.getTeamIdForUser(u.getId());
            String tName = tId > 0 ? teamDAO.getTeamNameById(tId) : "";
            u.setTeamName(tName);
        }

        allEmployees = FXCollections.observableArrayList(users);
    }
}