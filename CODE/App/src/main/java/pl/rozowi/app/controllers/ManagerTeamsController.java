package pl.rozowi.app.controllers;

import javafx.application.Platform;
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
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.dao.RoleDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;
import pl.rozowi.app.models.Role;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Kontroler zarządzający zespołami dla użytkowników z rolą Kierownika.
 * Umożliwia tworzenie, edycję i przypisywanie członków do zespołów,
 * a także przeglądanie zadań przypisanych do zespołów.
 */
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
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    private Map<Integer, String> roleNames = new HashMap<>();
    private Set<Integer> allowedRoleIds = new HashSet<>();

    private final ObservableList<TeamWithOrdinal> teamData = FXCollections.observableArrayList();
    private final ObservableList<User> memberData = FXCollections.observableArrayList();
    private final ObservableList<Task> taskData = FXCollections.observableArrayList();

    /**
     * Klasa pomocnicza reprezentująca zespół z numerem porządkowym.
     */
    public static class TeamWithOrdinal {
        private final Team team;
        private final SimpleIntegerProperty ordinalNumber = new SimpleIntegerProperty();

        /**
         * Tworzy nowy obiekt TeamWithOrdinal.
         * @param team Obiekt zespołu
         * @param ordinalNumber Numer porządkowy
         */
        public TeamWithOrdinal(Team team, int ordinalNumber) {
            this.team = team;
            this.ordinalNumber.set(ordinalNumber);
        }

        /**
         * Zwraca obiekt zespołu.
         * @return Obiekt Team
         */
        public Team getTeam() {
            return team;
        }

        /**
         * Zwraca ID zespołu.
         * @return ID zespołu
         */
        public int getId() {
            return team.getId();
        }

        /**
         * Zwraca nazwę zespołu.
         * @return Nazwa zespołu
         */
        public String getTeamName() {
            return team.getTeamName();
        }

        /**
         * Zwraca ID projektu.
         * @return ID projektu
         */
        public int getProjectId() {
            return team.getProjectId();
        }

        /**
         * Zwraca właściwość z numerem porządkowym.
         * @return Właściwość SimpleIntegerProperty
         */
        public SimpleIntegerProperty ordinalProperty() {
            return ordinalNumber;
        }

        /**
         * Zwraca numer porządkowy.
         * @return Numer porządkowy
         */
        public int getOrdinalNumber() {
            return ordinalNumber.get();
        }
    }

    /**
     * Klasa modelu wyboru użytkownika do przypisania do zespołu.
     */
    public static class UserSelectionModel {
        private final User user;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
        private final SimpleBooleanProperty leader = new SimpleBooleanProperty(false);
        private final SimpleStringProperty roleProperty = new SimpleStringProperty();
        private final SimpleStringProperty nameProperty = new SimpleStringProperty();
        private final SimpleStringProperty emailProperty = new SimpleStringProperty();
        private boolean isEligibleForLeader;

        /**
         * Tworzy nowy model wyboru użytkownika.
         * @param user Obiekt użytkownika
         * @param roleName Nazwa roli
         * @param isSelected Czy użytkownik jest wybrany
         * @param isLeader Czy użytkownik jest liderem
         * @param isEligibleForLeader Czy użytkownik może być liderem
         */
        public UserSelectionModel(User user, String roleName, boolean isSelected, boolean isLeader, boolean isEligibleForLeader) {
            this.user = user;
            this.selected.set(isSelected);
            this.leader.set(isLeader);
            this.roleProperty.set(roleName);
            this.nameProperty.set(user.getName() + " " + user.getLastName());
            this.emailProperty.set(user.getEmail());
            this.isEligibleForLeader = isEligibleForLeader;
        }

        /**
         * Zwraca obiekt użytkownika.
         * @return Obiekt User
         */
        public User getUser() {
            return user;
        }

        /**
         * Sprawdza czy użytkownik jest wybrany.
         * @return true jeśli wybrany, false w przeciwnym przypadku
         */
        public boolean isSelected() {
            return selected.get();
        }

        /**
         * Zwraca właściwość wyboru użytkownika.
         * @return Właściwość SimpleBooleanProperty
         */
        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        /**
         * Ustawia wybór użytkownika.
         * @param selected Czy użytkownik jest wybrany
         */
        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        /**
         * Sprawdza czy użytkownik jest liderem.
         * @return true jeśli lider, false w przeciwnym przypadku
         */
        public boolean isLeader() {
            return leader.get();
        }

        /**
         * Zwraca właściwość lidera.
         * @return Właściwość SimpleBooleanProperty
         */
        public SimpleBooleanProperty leaderProperty() {
            return leader;
        }

        /**
         * Ustawia lidera.
         * @param leader Czy użytkownik jest liderem
         */
        public void setLeader(boolean leader) {
            this.leader.set(leader);
        }

        /**
         * Zwraca nazwę roli.
         * @return Nazwa roli
         */
        public String getRole() {
            return roleProperty.get();
        }

        /**
         * Zwraca właściwość z nazwą roli.
         * @return Właściwość SimpleStringProperty
         */
        public SimpleStringProperty roleProperty() {
            return roleProperty;
        }

        /**
         * Zwraca imię i nazwisko.
         * @return Imię i nazwisko
         */
        public String getName() {
            return nameProperty.get();
        }

        /**
         * Zwraca właściwość z imieniem i nazwiskiem.
         * @return Właściwość SimpleStringProperty
         */
        public SimpleStringProperty nameProperty() {
            return nameProperty;
        }

        /**
         * Zwraca email.
         * @return Email
         */
        public String getEmail() {
            return emailProperty.get();
        }

        /**
         * Zwraca właściwość z emailem.
         * @return Właściwość SimpleStringProperty
         */
        public SimpleStringProperty emailProperty() {
            return emailProperty;
        }

        /**
         * Sprawdza czy użytkownik może być liderem.
         * @return true jeśli może być liderem, false w przeciwnym przypadku
         */
        public boolean isEligibleForLeader() {
            return isEligibleForLeader;
        }
    }

    /**
     * Inicjalizuje kontroler, ładując dane i konfigurując widoki tabel.
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
    @FXML
    public void initialize() throws SQLException {
        allowedRoleIds.add(3);
        allowedRoleIds.add(4);

        loadRoleNames();

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

    /**
     * Ładuje nazwy ról z bazy danych.
     */
    private void loadRoleNames() {
        try {
            Map<Integer, String> roles = userDAO.getAllRolesMap();
            if (!roles.isEmpty()) {
                roleNames = roles;
                return;
            }

            List<Role> rolesList = roleDAO.getAllRoles();
            for (Role role : rolesList) {
                roleNames.put(role.getId(), role.getRoleName());
            }

            if (roleNames.isEmpty()) {
                roleNames.put(1, "Administrator");
                roleNames.put(2, "Kierownik");
                roleNames.put(3, "Team Lider");
                roleNames.put(4, "Pracownik");
            }
        } catch (Exception e) {
            roleNames.put(1, "Administrator");
            roleNames.put(2, "Kierownik");
            roleNames.put(3, "Team Lider");
            roleNames.put(4, "Pracownik");
            e.printStackTrace();
        }
    }

    /**
     * Ładuje wszystkie zespoły przypisane do projektów kierownika.
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
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

    /**
     * Obsługuje wybór zespołu z tabeli, ładując jego członków i zadania.
     * @param team Wybrany zespół
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
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

    /**
     * Obsługuje dodawanie nowego zespołu.
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
    @FXML
    private void onAddTeam() throws SQLException {
        List<Project> mgrProjects = projectDAO.getProjectsForManager(Session.currentUserId);

        Dialog<Team> dlg = new Dialog<>();
        dlg.setTitle("Nowy zespół");
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        ComboBox<Project> cbProject = new ComboBox<>(FXCollections.observableArrayList(mgrProjects));
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

        Button saveButton = (Button) dlg.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = !newVal.trim().isEmpty() && cbProject.getValue() != null;
            saveButton.setDisable(!isValid);
        });

        cbProject.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !nameField.getText().trim().isEmpty();
            saveButton.setDisable(!isValid);
        });

        dlg.setResultConverter(btn -> {
            if (btn == saveButtonType) {
                String name = nameField.getText().trim();
                Project proj = cbProject.getValue();
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
                showInfo("Zespół został pomyślnie utworzony");
            } catch (SQLException ex) {
                showError("Błąd tworzenia zespołu", ex.getMessage());
            }
        });
    }

    /**
     * Obsługuje edycję istniejącego zespołu.
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
    @FXML
    private void onEditTeam() throws SQLException {
        TeamWithOrdinal selectedWithOrdinal = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedWithOrdinal == null) {
            showWarning("Wybierz zespół do edycji");
            return;
        }

        Team selected = selectedWithOrdinal.getTeam();

        Dialog<Team> dlg = new Dialog<>();
        dlg.setTitle("Edytuj zespół");
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(selected.getTeamName());
        ComboBox<Project> cbProject = new ComboBox<>();

        List<Project> mgrProjects = projectDAO.getProjectsForManager(Session.currentUserId);
        cbProject.setItems(FXCollections.observableArrayList(mgrProjects));

        cbProject.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project p) {
                return p == null ? "" : p.getName();
            }

            @Override
            public Project fromString(String s) {
                return null;
            }
        });

        for (Project project : mgrProjects) {
            if (project.getId() == selected.getProjectId()) {
                cbProject.setValue(project);
                break;
            }
        }

        grid.add(new Label("Nazwa zespołu:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Projekt:"), 0, 1);
        grid.add(cbProject, 1, 1);

        dlg.getDialogPane().setContent(grid);

        Button saveButton = (Button) dlg.getDialogPane().lookupButton(saveButtonType);

        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = !newVal.trim().isEmpty() && cbProject.getValue() != null;
            saveButton.setDisable(!isValid);
        });

        cbProject.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isValid = newVal != null && !nameField.getText().trim().isEmpty();
            saveButton.setDisable(!isValid);
        });

        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setTeamName(nameField.getText().trim());
                Project proj = cbProject.getValue();
                if (proj != null) {
                    selected.setProjectId(proj.getId());
                }
                return selected;
            }
            return null;
        });

        Optional<Team> result = dlg.showAndWait();
        result.ifPresent(team -> {
            try {
                teamDAO.updateTeam(team);
                loadAll();
                showInfo("Zespół został zaktualizowany");
            } catch (SQLException ex) {
                showError("Błąd aktualizacji zespołu", ex.getMessage());
            }
        });
    }

    /**
     * Obsługuje przypisywanie członków do zespołu.
     * @throws SQLException w przypadku błędu dostępu do bazy danych
     */
    @FXML
    private void onAssignMembers() throws SQLException {
        TeamWithOrdinal selectedWithOrdinal = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedWithOrdinal == null) {
            showWarning("Wybierz zespół do przypisania członków");
            return;
        }

        Team selected = selectedWithOrdinal.getTeam();

        List<User> allUsers = userDAO.getAllUsers();
        List<User> eligibleUsers = allUsers.stream()
                .filter(user -> allowedRoleIds.contains(user.getRoleId()))
                .collect(Collectors.toList());

        List<User> currentMembers = teamDAO.getTeamMembers(selected.getId());

        Dialog<List<UserSelectionModel>> dlg = new Dialog<>();
        dlg.setTitle("Przypisz członków do zespołu: " + selected.getTeamName());
        dlg.getDialogPane().setPrefWidth(800);
        dlg.getDialogPane().setPrefHeight(600);

        ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(assignButtonType, cancelButtonType);

        ObservableList<UserSelectionModel> usersToAssign = FXCollections.observableArrayList();
        for (User user : eligibleUsers) {
            boolean isCurrentMember = currentMembers.stream().anyMatch(m -> m.getId() == user.getId());
            boolean isLeader = isCurrentMember && teamMemberDAO.isTeamLeader(selected.getId(), user.getId());

            String roleName = roleNames.getOrDefault(user.getRoleId(), "");
            boolean eligibleForLeader = user.getRoleId() == 3;

            usersToAssign.add(new UserSelectionModel(user, roleName, isCurrentMember, isLeader, eligibleForLeader));
        }

        TableView<UserSelectionModel> usersTable = new TableView<>();
        usersTable.setEditable(true);
        usersTable.setItems(usersToAssign);

        TableColumn<UserSelectionModel, Boolean> selectedColumn = new TableColumn<>("Wybierz");
        selectedColumn.setCellValueFactory(p -> p.getValue().selectedProperty());
        selectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectedColumn));
        selectedColumn.setEditable(true);

        for (UserSelectionModel model : usersToAssign) {
            model.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue && model.isLeader()) {
                    model.setLeader(false);
                }
                usersTable.refresh();
            });
        }

        TableColumn<UserSelectionModel, Boolean> leaderColumn = new TableColumn<>("Lider");
        leaderColumn.setCellValueFactory(p -> p.getValue().leaderProperty());
        leaderColumn.setCellFactory(tc -> new CheckBoxTableCell<>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    TableRow<UserSelectionModel> row = getTableRow();
                    if (row != null && row.getItem() != null) {
                        UserSelectionModel rowData = row.getItem();
                        if (!rowData.isEligibleForLeader()) {
                            this.setDisable(true);
                            this.setStyle("-fx-opacity: 0.3;");
                        } else {
                            this.setDisable(false);
                            this.setStyle("");
                        }
                    }
                }
            }
        });
        leaderColumn.setEditable(true);

        TableColumn<UserSelectionModel, String> nameColumn = new TableColumn<>("Imię i Nazwisko");
        nameColumn.setCellValueFactory(p -> p.getValue().nameProperty());

        TableColumn<UserSelectionModel, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(p -> p.getValue().emailProperty());

        TableColumn<UserSelectionModel, String> roleColumn = new TableColumn<>("Rola");
        roleColumn.setCellValueFactory(p -> p.getValue().roleProperty());

        usersTable.getColumns().addAll(selectedColumn, leaderColumn, nameColumn, emailColumn, roleColumn);

        selectedColumn.setPrefWidth(70);
        leaderColumn.setPrefWidth(70);
        nameColumn.setPrefWidth(200);
        emailColumn.setPrefWidth(250);
        roleColumn.setPrefWidth(150);

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

        for (UserSelectionModel model : usersToAssign) {
            model.leaderProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (UserSelectionModel otherModel : usersToAssign) {
                        if (otherModel != model && otherModel.isLeader()) {
                            otherModel.setLeader(false);
                        }
                    }
                    if (!model.isSelected()) {
                        model.setSelected(true);
                    }
                }
            });
        }

        HBox buttonBar = new HBox(10, selectAllButton, deselectAllButton);
        Label instructionLabel = new Label("Zaznacz użytkowników, których chcesz dodać do zespołu. Możesz wybrać tylko jednego lidera zespołu.");
        Label hintLabel = new Label("UWAGA: Tylko osoby z rolą 'Team Lider' mogą być liderem zespołu. Zaznaczenie lidera automatycznie dodaje go do zespołu.");
        hintLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555555;");

        VBox content = new VBox(10, instructionLabel, hintLabel, usersTable, buttonBar);
        content.setPadding(new javafx.geometry.Insets(10));

        dlg.getDialogPane().setContent(content);

        dlg.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return usersToAssign.stream()
                        .filter(UserSelectionModel::isSelected)
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<UserSelectionModel>> result = dlg.showAndWait();
        result.ifPresent(selectedUsers -> {
            try {
                long leaderCount = selectedUsers.stream().filter(UserSelectionModel::isLeader).count();
                if (leaderCount > 1) {
                    showError("Błąd", "W zespole może być tylko jeden lider. Proszę wybrać ponownie.");
                    return;
                }

                for (User member : currentMembers) {
                    teamDAO.deleteTeamMember(selected.getId(), member.getId());
                }

                for (UserSelectionModel userModel : selectedUsers) {
                    User user = userModel.getUser();
                    boolean isLeader = userModel.isLeader();
                    teamDAO.insertTeamMember(selected.getId(), user.getId(), isLeader);
                }

                memberData.setAll(teamDAO.getTeamMembers(selected.getId()));
                membersTable.refresh();

                showInfo("Członkowie zespołu zostali zaktualizowani");
            } catch (SQLException ex) {
                showError("Błąd podczas przypisywania członków zespołu", ex.getMessage());
            }
        });
    }

    /**
     * Wyświetla okno dialogowe z informacją.
     * @param message Treść wiadomości
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla okno dialogowe z ostrzeżeniem.
     * @param message Treść ostrzeżenia
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla okno dialogowe z błędem.
     * @param title Tytuł błędu
     * @param message Treść błędu
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}