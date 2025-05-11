package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ManagerTeamsController {

    @FXML
    private TableView<TeamWithOrdinal> teamsTable;
    @FXML
    private TableColumn<TeamWithOrdinal, Number> colId;
    @FXML
    private TableColumn<TeamWithOrdinal, String> colName;

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

    private final ObservableList<TeamWithOrdinal> teamData = FXCollections.observableArrayList();
    private final ObservableList<User> memberData = FXCollections.observableArrayList();
    private final ObservableList<Task> taskData = FXCollections.observableArrayList();

    public static class TeamWithOrdinal {
        private final Team team;
        private final SimpleIntegerProperty ordinalNumber = new SimpleIntegerProperty();

        public TeamWithOrdinal(Team team, int ordinalNumber) {
            this.team = team;
            this.ordinalNumber.set(ordinalNumber);
        }

        public Team getTeam() {
            return team;
        }

        public int getId() {
            return team.getId();
        }

        public String getTeamName() {
            return team.getTeamName();
        }

        public int getProjectId() {
            return team.getProjectId();
        }

        public SimpleIntegerProperty ordinalProperty() {
            return ordinalNumber;
        }

        public int getOrdinalNumber() {
            return ordinalNumber.get();
        }
    }

    private static class UserWithOrdinal {
        private final User user;
        private final int ordinalNumber;

        public UserWithOrdinal(User user, int ordinalNumber) {
            this.user = user;
            this.ordinalNumber = ordinalNumber;
        }

        public User getUser() {
            return user;
        }

        public int getOrdinalNumber() {
            return ordinalNumber;
        }
    }

    @FXML
    public void initialize() throws SQLException {
        colId.setCellValueFactory(c -> c.getValue().ordinalProperty());
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTeamName()));
        teamsTable.setItems(teamData);

        teamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            try {
                if (newT != null) {
                    onTeamSelected(newT.getTeam());
                } else {
                    memberData.clear();
                    taskData.clear();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        colMemberId.setCellValueFactory(c -> {
            int index = memberData.indexOf(c.getValue()) + 1;
            return new SimpleIntegerProperty(index);
        });
        colMemberEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        membersTable.setItems(memberData);

        colTaskId.setCellValueFactory(c -> {
            int index = taskData.indexOf(c.getValue()) + 1;
            return new SimpleIntegerProperty(index);
        });
        colTaskTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colTaskStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colTaskPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colTaskAssignee.setCellValueFactory(c -> c.getValue().assignedEmailProperty());
        tasksTable.setItems(taskData);

        loadAll();
    }

    private void loadAll() throws SQLException {
        teamData.clear();
        Set<Integer> mgrProjIds = projectDAO.getProjectsForManager(Session.currentUserId)
                .stream()
                .map(Project::getId)
                .collect(Collectors.toSet());

        List<Team> allTeams = teamDAO.getAllTeams();
        List<Team> filteredTeams = allTeams.stream()
                .filter(t -> mgrProjIds.contains(t.getProjectId()))
                .collect(Collectors.toList());

        AtomicInteger counter = new AtomicInteger(1);
        for (Team team : filteredTeams) {
            teamData.add(new TeamWithOrdinal(team, counter.getAndIncrement()));
        }
    }

    private void onTeamSelected(Team team) throws SQLException {
        if (team == null) {
            memberData.clear();
            taskData.clear();
        } else {
            memberData.setAll(teamDAO.getTeamMembers(team.getId()));
            taskData.setAll(taskDAO.getTasksByTeamId(team.getId()));
            membersTable.refresh();
            tasksTable.refresh();
        }
    }

    @FXML
    private void onAddTeam() throws SQLException {
        List<Project> mgrProjects = projectDAO.getProjectsForManager(Session.currentUserId);

        Dialog<Team> dlg = new Dialog<>();
        dlg.setTitle("Nowy zespół");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        ComboBox<Project> cbProject = new ComboBox<>(FXCollections.observableArrayList(mgrProjects));
        cbProject.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project p) {
                return p == null ? "" : p.getId() + " – " + p.getProjectName();
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
        TeamWithOrdinal selectedWithOrdinal = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedWithOrdinal == null) return;

        Team sel = selectedWithOrdinal.getTeam();
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
                setText(empty || u == null ? null : (memberItems.indexOf(u) + 1) + ". " + u.getEmail());
            }
        });
        listView.setPrefHeight(150);

        Button btnRemove = new Button("Usuń zaznaczonego");
        btnRemove.setOnAction(evt -> {
            User u = listView.getSelectionModel().getSelectedItem();
            if (u != null) {
                teamDAO.deleteTeamMember(sel.getId(), u.getId());
                listView.getItems().remove(u);
                listView.refresh();
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
        TeamWithOrdinal selectedWithOrdinal = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedWithOrdinal == null) return;

        Team selected = selectedWithOrdinal.getTeam();
        List<User> all = userDAO.getAllUsers();
        List<User> filteredUsers = all.stream()
                .filter(user -> user.getRoleId() == 3 || user.getRoleId() == 4)
                .collect(Collectors.toList());

        List<User> members = teamDAO.getTeamMembers(selected.getId());
        Dialog<List<User>> dlg = new Dialog<>();
        dlg.setTitle("Przypisz członków do zespołu");

        ObservableList<UserWithOrdinal> listItems = FXCollections.observableArrayList();
        AtomicInteger counter = new AtomicInteger(1);
        filteredUsers.forEach(user -> {
            listItems.add(new UserWithOrdinal(user, counter.getAndIncrement()));
        });

        ListView<UserWithOrdinal> listView = new ListView<>(listItems);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        for (User u : members) {
            listItems.stream()
                    .filter(uw -> uw.getUser().getId() == u.getId())
                    .findFirst()
                    .ifPresent(uw -> listView.getSelectionModel().select(uw));
        }

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserWithOrdinal uw, boolean empty) {
                super.updateItem(uw, empty);
                if (empty || uw == null) {
                    setText(null);
                } else {
                    User u = uw.getUser();
                    String role = u.getRoleId() == 3 ? "Team Leader" : "Pracownik";
                    setText(uw.getOrdinalNumber() + ". " + u.getEmail() + " (" + role + ")");
                }
            }
        });

        dlg.getDialogPane().setContent(listView);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> btn == ButtonType.OK
                ? listView.getSelectionModel().getSelectedItems()
                .stream()
                .map(UserWithOrdinal::getUser)
                .collect(Collectors.toList())
                : null);

        Optional<List<User>> res = dlg.showAndWait();
        res.ifPresent(chosen -> {
            TeamDAO dao = new TeamDAO();
            for (User u : members) {
                dao.deleteTeamMember(selected.getId(), u.getId());
            }
            for (User u : chosen) {
                try {
                    boolean isLeader = u.getRoleId() == 3;
                    dao.insertTeamMember(selected.getId(), u.getId(), isLeader);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            memberData.setAll(teamDAO.getTeamMembers(selected.getId()));
            membersTable.refresh();
        });
    }
}