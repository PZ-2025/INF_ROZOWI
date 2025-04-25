package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;




import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AdminTeamsController {

    @FXML
    private TableView<Team> teamsTable;
    @FXML
    private TableColumn<Team, Number> colId;
    @FXML
    private TableColumn<Team, String> colName;
    @FXML
    private TableColumn<Team, String> colProjectName;
    @FXML
    private TableColumn<Team, Number> colMembersCount;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> membersTable;
    @FXML
    private TableColumn<User, Number> colMemberId;
    @FXML
    private TableColumn<User, String> colMemberEmail;

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colTaskId;
    @FXML
    private TableColumn<Task, String> colTaskTitle;
    @FXML
    private TableColumn<Task, String> colTaskStatus;
    @FXML
    private TableColumn<Task, String> colTaskPriority;
    @FXML
    private TableColumn<Task, String> colTaskAssignee;

    private final TeamDAO teamDAO = new TeamDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();


    private final ObservableList<Team> teamData = FXCollections.observableArrayList();
    private final ObservableList<User> memberData = FXCollections.observableArrayList();
    private final ObservableList<Task> taskData = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        // Konfiguracja kolumn tabeli zespołów
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().teamNameProperty());
        colProjectName.setCellValueFactory(c -> {
            int projectId = c.getValue().getProjectId();
            try {
                List<Project> projects = projectDAO.getAllProjects();
                for (Project p : projects) {
                    if (p.getId() == projectId) {
                        return new SimpleStringProperty(p.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty("Brak projektu");
        });
        colMembersCount.setCellValueFactory(c -> {
            int teamId = c.getValue().getId();
            try {
                List<User> members = teamDAO.getTeamMembers(teamId);
                return new SimpleIntegerProperty(members.size());
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleIntegerProperty(0);
            }
        });

        teamsTable.setItems(teamData);

        // Konfiguracja kolumn tabeli członków
        colMemberId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colMemberEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        membersTable.setItems(memberData);

        // Konfiguracja kolumn tabeli zadań
        colTaskId.setCellValueFactory(c -> c.getValue().idProperty());
        colTaskTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colTaskStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colTaskPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colTaskAssignee.setCellValueFactory(c -> c.getValue().assignedEmailProperty());
        tasksTable.setItems(taskData);

        // Obsługa wyboru zespołu - aktualizacja tabel szczegółowych
        teamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            try {
                onTeamSelected(newT);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        loadAll();

        // Konfiguracja wyszukiwania
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String search = newVal.toLowerCase().trim();
            if (search.isEmpty()) {
                teamsTable.setItems(teamData);
            } else {
                ObservableList<Team> filtered = FXCollections.observableArrayList();
                for (Team team : teamData) {
                    if (String.valueOf(team.getId()).contains(search) ||
                        team.getTeamName().toLowerCase().contains(search)) {
                        filtered.add(team);
                    }
                }
                teamsTable.setItems(filtered);
            }
        });
    }

    private void loadAll() throws SQLException {
        List<Team> allTeams = teamDAO.getAllTeams();
        teamData.setAll(allTeams);
    }

    private void onTeamSelected(Team team) throws SQLException {
        if (team == null) {
            memberData.clear();
            taskData.clear();
        } else {
            memberData.setAll(teamDAO.getTeamMembers(team.getId()));
            taskData.setAll(taskDAO.getTasksByTeamId(team.getId()));
        }
    }

    @FXML
    private void onAddTeam() throws SQLException {
        Dialog<Team> dlg = new Dialog<>();
        dlg.setTitle("Nowy zespół");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        ComboBox<Project> cbProject = new ComboBox<>(FXCollections.observableArrayList(projectDAO.getAllProjects()));
        cbProject.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project p) {
                return p == null ? "" : p.getId() + " – " + p.getName();
            }

            @Override
            public Project fromString(String s) {
                return null;
            }
        });

        grid.add(new Label("Nazwa zespołu:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Projekt:"), 0, 1);
        grid.add(cbProject, 1, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = nameField.getText().trim();
                Project proj = cbProject.getValue();
                if (name.isEmpty() || proj == null) {
                    new Alert(Alert.AlertType.WARNING, "Wypełnij nazwę i wybierz projekt!").showAndWait();
                    return null;
                }
                Team t = new Team();
                t.setTeamName(name);
                t.setProjectId(proj.getId());
                return t;
            }
            return null;
        });

        Optional<Team> res = dlg.showAndWait();
        res.ifPresent(t -> {
            try {
                teamDAO.insertTeam(t);
                loadAll();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Błąd tworzenia zespołu:\n" + ex.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    private void onEditTeam() throws SQLException {
        Team sel = teamsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        List<User> members = teamDAO.getTeamMembers(sel.getId());
        ObservableList<User> memberItems = FXCollections.observableArrayList(members);
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Edytuj zespół");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(sel.getTeamName());
        ListView<User> listView = new ListView<>(memberItems);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getId() + " – " + u.getEmail());
            }
        });
        listView.setPrefHeight(150);

        Button btnRemove = new Button("Usuń zaznaczonego");
        btnRemove.setOnAction(evt -> {
            User u = listView.getSelectionModel().getSelectedItem();
            if (u != null) {
                teamDAO.deleteTeamMember(sel.getId(), u.getId());
                listView.getItems().remove(u);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nazwa zespołu:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Członkowie:"), 0, 1);
        grid.add(listView, 1, 1);
        grid.add(btnRemove, 1, 2);

        dlg.getDialogPane().setContent(grid);
        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            sel.setTeamName(nameField.getText().trim());
            try {
                teamDAO.updateTeam(sel);
                loadAll();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Błąd aktualizacji zespołu:\n" + ex.getMessage())
                        .showAndWait();
            }
        }
    }

    @FXML
    private void onAssignMembers() throws SQLException {
        Team selected = teamsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        List<User> all = userDAO.getAllUsers();
        List<User> members = teamDAO.getTeamMembers(selected.getId());
        Dialog<List<User>> dlg = new Dialog<>();
        dlg.setTitle("Przypisz członków do zespołu");
        ListView<User> listView = new ListView<>(FXCollections.observableArrayList(all));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (User u : members) listView.getSelectionModel().select(u);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getId() + " – " + u.getEmail());
            }
        });

        dlg.getDialogPane().setContent(listView);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> btn == ButtonType.OK
                ? listView.getSelectionModel().getSelectedItems()
                : null);

        Optional<List<User>> res = dlg.showAndWait();
        res.ifPresent(chosen -> {
            // Najpierw pobierz aktualny stan członkostwa dla każdego użytkownika
            Map<Integer, Integer> userTeamMap = new HashMap<>();
            for (User u : all) {
                int currentTeamId = teamMemberDAO.getTeamIdForUser(u.getId());
                userTeamMap.put(u.getId(), currentTeamId);
            }

            // Usuń wszystkich poprzednich członków tego zespołu
            for (User u : members) {
                teamMemberDAO.deleteTeamMember(selected.getId(), u.getId());
            }

            // Dodaj nowych członków do zespołu
            for (User u : chosen) {
                try {
                    teamMemberDAO.insertTeamMember(selected.getId(), u.getId(), false);
                } catch (SQLException ex) {
                    new Alert(Alert.AlertType.ERROR, "Błąd przypisania użytkownika: " + ex.getMessage()).showAndWait();
                }
            }

            memberData.setAll(teamDAO.getTeamMembers(selected.getId()));
        });
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            teamsTable.setItems(teamData);
        } else {
            ObservableList<Team> filtered = FXCollections.observableArrayList();
            for (Team team : teamData) {
                if (String.valueOf(team.getId()).contains(searchText) ||
                    team.getTeamName().toLowerCase().contains(searchText)) {
                    filtered.add(team);
                }
            }
            teamsTable.setItems(filtered);
        }
    }

    @FXML
    private void handleRefresh() {
        try {
            loadAll();
            Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
            if (selectedTeam != null) {
                onTeamSelected(selectedTeam);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Błąd odświeżania danych: " + e.getMessage()).showAndWait();
        }
    }
}