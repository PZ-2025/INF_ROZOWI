package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;

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
    private TableView<UserWithRole> membersTable;
    @FXML
    private TableColumn<UserWithRole, Number> colMemberId;
    @FXML
    private TableColumn<UserWithRole, String> colMemberEmail;
    @FXML
    private TableColumn<UserWithRole, String> colMemberRole; // Kolumna dla roli

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
    private final RoleDAO roleDAO = new RoleDAO();

    private Map<Integer, String> roleNames = new HashMap<>();

    private final ObservableList<Team> teamData = FXCollections.observableArrayList();
    private final ObservableList<UserWithRole> memberData = FXCollections.observableArrayList();
    private final ObservableList<Task> taskData = FXCollections.observableArrayList();

    // Klasa pomocnicza do reprezentowania użytkownika z jego rolą i statusem lidera
    public static class UserWithRole {
        private final User user;
        private final String roleName;
        private final boolean isLeader;

        public UserWithRole(User user, String roleName, boolean isLeader) {
            this.user = user;
            this.roleName = roleName;
            this.isLeader = isLeader;
        }

        public User getUser() {
            return user;
        }

        public int getId() {
            return user.getId();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getName() {
            return user.getName();
        }

        public String getLastName() {
            return user.getLastName();
        }

        public String getFullName() {
            return getName() + " " + getLastName();
        }

        public String getRoleName() {
            return roleName;
        }

        public boolean isLeader() {
            return isLeader;
        }
    }

    // Klasa dla modelu wiersza w tabeli wyboru członków
    public static class UserSelectionModel {
        private final User user;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private final SimpleBooleanProperty leader = new SimpleBooleanProperty(false);
        private final SimpleStringProperty roleProperty = new SimpleStringProperty();
        private final SimpleStringProperty nameProperty = new SimpleStringProperty();
        private final SimpleStringProperty emailProperty = new SimpleStringProperty();

        public UserSelectionModel(User user, String roleName, boolean isSelected, boolean isLeader) {
            this.user = user;
            this.selected.set(isSelected);
            this.leader.set(isLeader);
            this.roleProperty.set(roleName);
            this.nameProperty.set(user.getName() + " " + user.getLastName());
            this.emailProperty.set(user.getEmail());
        }

        public User getUser() {
            return user;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public boolean isLeader() {
            return leader.get();
        }

        public SimpleBooleanProperty leaderProperty() {
            return leader;
        }

        public void setLeader(boolean leader) {
            this.leader.set(leader);
        }

        public String getRole() {
            return roleProperty.get();
        }

        public SimpleStringProperty roleProperty() {
            return roleProperty;
        }

        public String getName() {
            return nameProperty.get();
        }

        public SimpleStringProperty nameProperty() {
            return nameProperty;
        }

        public String getEmail() {
            return emailProperty.get();
        }

        public SimpleStringProperty emailProperty() {
            return emailProperty;
        }
    }

    @FXML
    public void initialize() throws SQLException {
        // Pobierz mapę ról dla wszystkich użytkowników
        loadRoleNames();

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

        // Konfiguracja kolumn tabeli członków zespołu
        colMemberId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colMemberEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFullName() + " (" + c.getValue().getEmail() + ")"
        ));
        colMemberRole.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRoleName() + (c.getValue().isLeader() ? " (Lider)" : "")
        ));
        membersTable.setItems(memberData);

        // Konfiguracja kolumn tabeli zadań
        colTaskId.setCellValueFactory(c -> c.getValue().idProperty());
        colTaskTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colTaskStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colTaskPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colTaskAssignee.setCellValueFactory(c -> c.getValue().assignedEmailProperty());
        tasksTable.setItems(taskData);

        // Obsługa wyboru zespołu
        teamsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldT, newT) -> {
                    try {
                        onTeamSelected(newT);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

        loadAll();
    }

    // Pobieranie nazw ról z bazy danych
    private void loadRoleNames() {
        try {
            // Najpierw spróbuj pobrać przez UserDAO
            Map<Integer, String> roles = userDAO.getAllRolesMap();
            if (!roles.isEmpty()) {
                roleNames = roles;
                return;
            }

            // Jeśli to nie zadziała, użyj RoleDAO
            List<Role> rolesList = roleDAO.getAllRoles();
            for (Role role : rolesList) {
                roleNames.put(role.getId(), role.getRoleName());
            }

            // Jeśli nadal brak ról, dodaj domyślne wartości
            if (roleNames.isEmpty()) {
                roleNames.put(1, "Administrator");
                roleNames.put(2, "Kierownik");
                roleNames.put(3, "Team Leader");
                roleNames.put(4, "Pracownik");
            }
        } catch (Exception e) {
            // W przypadku błędu, dodaj domyślne wartości
            roleNames.put(1, "Administrator");
            roleNames.put(2, "Kierownik");
            roleNames.put(3, "Team Leader");
            roleNames.put(4, "Pracownik");
            e.printStackTrace();
        }
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
            loadTeamMembers(team.getId());
            taskData.setAll(taskDAO.getTasksByTeamId(team.getId()));
        }
    }

    // Ładowanie członków zespołu wraz z ich rolami
    private void loadTeamMembers(int teamId) throws SQLException {
        memberData.clear();
        List<User> members = teamDAO.getTeamMembers(teamId);

        for (User member : members) {
            // Pobierz rolę użytkownika
            int roleId = member.getRoleId();
            String roleName = roleNames.getOrDefault(roleId, "Rola " + roleId);

            // Sprawdź, czy użytkownik jest liderem zespołu
            boolean isLeader = teamMemberDAO.isTeamLeader(teamId, member.getId());

            // Dodaj użytkownika z informacją o roli do listy
            memberData.add(new UserWithRole(member, roleName, isLeader));
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

        // Konwertuj UserWithRole z powrotem na User dla kompatybilności z istniejącym kodem
        List<User> members = memberData.stream().map(UserWithRole::getUser).collect(Collectors.toList());
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
                setText(empty || u == null ? null : u.getId() + " – " + u.getName() + " " + u.getLastName() + " (" + u.getEmail() + ")");
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
        if (selected == null) {
            showWarning("Wybierz zespół do przypisania członków");
            return;
        }

        // Przygotuj listę wszystkich użytkowników
        List<User> allUsers = userDAO.getAllUsers();
        List<User> currentMembers = teamDAO.getTeamMembers(selected.getId());

        // Utwórz dialog
        Dialog<List<UserSelectionModel>> dlg = new Dialog<>();
        dlg.setTitle("Przypisz członków do zespołu: " + selected.getTeamName());
        dlg.getDialogPane().setPrefWidth(800);
        dlg.getDialogPane().setPrefHeight(600);

        // Przyciski
        ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(assignButtonType, cancelButtonType);

        // Przygotuj model danych dla tabeli
        ObservableList<UserSelectionModel> usersToAssign = FXCollections.observableArrayList();
        for (User user : allUsers) {
            boolean isCurrentMember = currentMembers.stream().anyMatch(m -> m.getId() == user.getId());
            boolean isLeader = isCurrentMember && teamMemberDAO.isTeamLeader(selected.getId(), user.getId());
            String roleName = roleNames.getOrDefault(user.getRoleId(), "Rola " + user.getRoleId());

            usersToAssign.add(new UserSelectionModel(user, roleName, isCurrentMember, isLeader));
        }

        // Utwórz tabelę
        TableView<UserSelectionModel> usersTable = new TableView<>();
        usersTable.setEditable(true);
        usersTable.setItems(usersToAssign);

        // Kolumna wyboru
        TableColumn<UserSelectionModel, Boolean> selectedColumn = new TableColumn<>("Wybierz");
        selectedColumn.setCellValueFactory(p -> p.getValue().selectedProperty());
        selectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectedColumn));
        selectedColumn.setEditable(true);

        // Kolumna lidera
        TableColumn<UserSelectionModel, Boolean> leaderColumn = new TableColumn<>("Lider");
        leaderColumn.setCellValueFactory(p -> p.getValue().leaderProperty());
        leaderColumn.setCellFactory(CheckBoxTableCell.forTableColumn(leaderColumn));
        leaderColumn.setEditable(true);

        // Kolumna imienia i nazwiska
        TableColumn<UserSelectionModel, String> nameColumn = new TableColumn<>("Imię i Nazwisko");
        nameColumn.setCellValueFactory(p -> p.getValue().nameProperty());

        // Kolumna email
        TableColumn<UserSelectionModel, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(p -> p.getValue().emailProperty());

        // Kolumna roli
        TableColumn<UserSelectionModel, String> roleColumn = new TableColumn<>("Rola");
        roleColumn.setCellValueFactory(p -> p.getValue().roleProperty());

        // Dodaj kolumny do tabeli
        usersTable.getColumns().addAll(selectedColumn, leaderColumn, nameColumn, emailColumn, roleColumn);

        // Ustaw szerokości kolumn
        selectedColumn.setPrefWidth(70);
        leaderColumn.setPrefWidth(70);
        nameColumn.setPrefWidth(200);
        emailColumn.setPrefWidth(250);
        roleColumn.setPrefWidth(150);

        // Przyciski Zaznacz wszystkie/Odznacz wszystkie
        Button selectAllButton = new Button("Zaznacz wszystkie");
        selectAllButton.setOnAction(e -> {
            for (UserSelectionModel model : usersToAssign) {
                model.setSelected(true);
            }
        });

        Button deselectAllButton = new Button("Odznacz wszystkie");
        deselectAllButton.setOnAction(e -> {
            for (UserSelectionModel model : usersToAssign) {
                model.setSelected(false);
                model.setLeader(false);
            }
        });

        // Układamy kontrolki
        HBox buttonBar = new HBox(10, selectAllButton, deselectAllButton);
        Label instructionLabel = new Label("Zaznacz użytkowników, których chcesz dodać do zespołu. Możesz również oznaczyć liderów zespołu.");

        VBox content = new VBox(10, instructionLabel, usersTable, buttonBar);
        content.setPadding(new javafx.geometry.Insets(10));

        dlg.getDialogPane().setContent(content);

        // Konwerter rezultatu
        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return usersToAssign.stream()
                    .filter(UserSelectionModel::isSelected)
                    .collect(Collectors.toList());
            }
            return null;
        });

        // Wyświetl dialog i obsłuż rezultat
        Optional<List<UserSelectionModel>> result = dlg.showAndWait();
        result.ifPresent(selectedUsers -> {
            try {
                // Najpierw usuń wszystkich obecnych członków
                for (User member : currentMembers) {
                    teamDAO.deleteTeamMember(selected.getId(), member.getId());
                }

                // Dodaj wybranych członków
                for (UserSelectionModel userModel : selectedUsers) {
                    User user = userModel.getUser();
                    boolean isLeader = userModel.isLeader();
                    teamDAO.insertTeamMember(selected.getId(), user.getId(), isLeader);
                }

                // Odśwież listę członków
                loadTeamMembers(selected.getId());

                showInfo("Członkowie zespołu zostali zaktualizowani");
            } catch (SQLException ex) {
                showError("Błąd podczas przypisywania członków zespołu", ex.getMessage());
            }
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
            showError("Błąd odświeżania danych", e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}