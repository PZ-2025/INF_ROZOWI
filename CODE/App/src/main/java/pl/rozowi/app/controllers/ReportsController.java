package pl.rozowi.app.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.ReportService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML
    private VBox groupsContainer;

    @FXML
    private Label groupsLabel;

    @FXML
    private ComboBox<String> groupsComboBox;

    private String selectedGroup = null;

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private TextArea reportsArea;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveAsPdfButton;

    @FXML
    private Button filterOptionsButton;

    @FXML
    private VBox filterOptionsPane;

    @FXML
    private ListView<Team> teamsListView;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private CheckBox showTasksCheckbox;

    @FXML
    private CheckBox showMembersCheckbox;

    @FXML
    private CheckBox showStatisticsCheckbox;

    @FXML private HBox teamsContainer;
    @FXML private HBox teamsButtonsContainer;

    @FXML
    private ListView<Project> projectsListView;
    @FXML
    private Label teamsLabel;

    @FXML
    private Label projectsLabel;
    @FXML
    private Label dateRangeLabel;
    @FXML private HBox userTypesContainer;
    @FXML private CheckBox adminCheckbox;
    @FXML private CheckBox managerCheckbox;
    @FXML private CheckBox teamLeaderCheckbox;
    @FXML private CheckBox userCheckbox;

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

    private ReportService reportService;
    private String currentReportContent = "";
    private String currentReportType = "";

    private ObservableList<Team> selectedTeams = FXCollections.observableArrayList();
    private ObservableList<Project> selectedProjects = FXCollections.observableArrayList();
    @FXML private Button selectAllProjectsBtn;
    @FXML private Button deselectAllProjectsBtn;
    private LocalDate startDate = null;
    private LocalDate endDate = null;
    private boolean showTasks = true;
    private boolean showMembers = true;
    private boolean showStatistics = true;
    private boolean showAdmins = true;
    private boolean showManagers = true;
    private boolean showTeamLeaders = true;
    private boolean showUsers = true;

    @FXML
    private void initialize() {
        reportService = new ReportService();
        reportsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        showTasksCheckbox.setSelected(true);
        showMembersCheckbox.setSelected(true);
        showStatisticsCheckbox.setSelected(true);
        adminCheckbox.setSelected(true);
        managerCheckbox.setSelected(true);
        teamLeaderCheckbox.setSelected(true);
        userCheckbox.setSelected(true);

        loadReportTypes();
        initFilterOptions();

        generateButton.setDisable(true);
        filterOptionsButton.setDisable(true);
        saveAsPdfButton.setDisable(true);

        selectedTeams.addListener((ListChangeListener<Team>) change -> updateGenerateButtonState());

        selectedProjects.addListener((ListChangeListener<Project>) change -> updateGenerateButtonState());

        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        reportsArea.clear();
                        currentReportContent = "";
                        saveAsPdfButton.setDisable(true);

                        currentReportType = newValue;
                        generateButton.setDisable(false);
                        filterOptionsButton.setDisable(false);
                        updateFilterVisibility(newValue);
                    }
                });

        filterOptionsPane.setVisible(false);
        filterOptionsPane.setManaged(false);

        adminCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showAdmins = newVal;
            updateGenerateButtonState();
        });
        managerCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showManagers = newVal;
            updateGenerateButtonState();
        });
        teamLeaderCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showTeamLeaders = newVal;
            updateGenerateButtonState();
        });
        userCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showUsers = newVal;
            updateGenerateButtonState();
        });

        groupsComboBox.getItems().add("Wszystkie grupy");
        groupsComboBox.getItems().addAll(userDAO.getAllGroupNames());
        groupsComboBox.getSelectionModel().selectFirst();

        groupsComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if ("Wszystkie grupy".equals(newVal)) {
                    selectedGroup = null;
                } else {
                    selectedGroup = newVal;
                }
                updateGenerateButtonState();
            }
        });
    }

    @FXML
    private void handleSelectAllTeams() {
        selectedTeams.setAll(teamsListView.getItems());
        refreshTeamsListView();
        updateGenerateButtonState();
    }

    @FXML
    private void handleDeselectAllTeams() {
        selectedTeams.clear();
        refreshTeamsListView();
        updateGenerateButtonState();
    }

    @FXML
    private void handleSelectAllProjects() {
        selectedProjects.setAll(projectsListView.getItems());
        refreshProjectsListView();
        updateGenerateButtonState();
    }

    @FXML
    private void handleDeselectAllProjects() {
        selectedProjects.clear();
        refreshProjectsListView();
        updateGenerateButtonState();
    }

    private void refreshTeamsListView() {
        teamsListView.refresh();
    }

    private void refreshProjectsListView() {
        projectsListView.refresh();
    }

    private void updateGenerateButtonState() {
        boolean disableGenerateButton = false;

        if ("Użytkownicy Systemu".equals(currentReportType)) {
            disableGenerateButton = !adminCheckbox.isSelected()
                    && !managerCheckbox.isSelected()
                    && !teamLeaderCheckbox.isSelected()
                    && !userCheckbox.isSelected();

            if (disableGenerateButton) {
                generateButton.setTooltip(new Tooltip("Wybierz przynajmniej jeden typ użytkownika"));
            }
        }
        else if ("Struktura Zespołów".equals(currentReportType)) {
            disableGenerateButton = selectedTeams.isEmpty();

            if (disableGenerateButton) {
                generateButton.setTooltip(new Tooltip("Wybierz przynajmniej jeden zespół"));
            }
        }
        else if ("Przegląd Projektów".equals(currentReportType)) {
            disableGenerateButton = selectedProjects.isEmpty();

            if (disableGenerateButton) {
                generateButton.setTooltip(new Tooltip("Wybierz przynajmniej jeden projekt"));
            }
        }

        if (currentReportType == null || currentReportType.isEmpty()) {
            disableGenerateButton = true;
            generateButton.setTooltip(new Tooltip("Wybierz typ raportu"));
        }

        generateButton.setDisable(disableGenerateButton);

        if (!disableGenerateButton) {
            generateButton.setTooltip(null);
        }
    }

    private void initFilterOptions() {
        try {
            User currentUser = MainApplication.getCurrentUser();
            ObservableList<Team> teams;

            if (currentUser != null && currentUser.getRoleId() == 2) {
                teams = FXCollections.observableArrayList(teamDAO.getTeamsForManager(currentUser.getId()));
            } else {
                teams = FXCollections.observableArrayList(teamDAO.getAllTeams());
            }
            selectedTeams.setAll(teams);

            teamsListView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Team> call(ListView<Team> param) {
                    return new ListCell<>() {
                        private final CheckBox checkBox = new CheckBox();

                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            if (empty || team == null) {
                                setGraphic(null);
                            } else {
                                checkBox.setText(team.getTeamName());
                                checkBox.setSelected(selectedTeams.contains(team));
                                checkBox.setOnAction(e -> {
                                    if (checkBox.isSelected()) {
                                        selectedTeams.add(team);
                                    } else {
                                        selectedTeams.remove(team);
                                    }
                                    updateGenerateButtonState();
                                });
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });

            projectsListView.setCellFactory(param -> new ListCell<>() {
                private final CheckBox checkBox = new CheckBox();

                @Override
                protected void updateItem(Project project, boolean empty) {
                    super.updateItem(project, empty);
                    if (empty || project == null) {
                        setGraphic(null);
                    } else {
                        checkBox.setText(project.getName());
                        checkBox.setSelected(selectedProjects.contains(project));
                        checkBox.setStyle("-fx-text-fill: -fx-text-base-color;");
                        checkBox.setOnAction(e -> {
                            if (checkBox.isSelected()) {
                                selectedProjects.add(project);
                            } else {
                                selectedProjects.remove(project);
                            }
                            updateGenerateButtonState();
                        });
                        setGraphic(checkBox);
                    }
                }
            });

            teamsListView.setItems(FXCollections.observableArrayList(teams));

            if (projectsListView != null) {
                ObservableList<Project> projects;
                if (currentUser != null && currentUser.getRoleId() == 2) {
                    projects = FXCollections.observableArrayList(projectDAO.getProjectsForManager(currentUser.getId()));
                } else {
                    projects = FXCollections.observableArrayList(projectDAO.getAllProjects());
                }

                selectedProjects.setAll(projects);
                projectsListView.setItems(FXCollections.observableArrayList(projects));
            }

            startDatePicker.setValue(null);
            endDatePicker.setValue(null);


            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> startDate = newVal);

            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> endDate = newVal);

            showTasksCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> showTasks = newVal);

            showMembersCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> showMembers = newVal);

            showStatisticsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> showStatistics = newVal);

        } catch (SQLException e) {
            showError("Błąd inicjalizacji filtrów", e.getMessage());
        }
        if (projectsListView != null) {
            List<Project> projects;
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser != null && currentUser.getRoleId() == 2) {
                projects = projectDAO.getProjectsForManager(currentUser.getId());
            } else {
                projects = projectDAO.getAllProjects();
            }

            projectsListView.setItems(FXCollections.observableArrayList(projects));
        }

    }
    @FXML
    private void handleShowFilterOptions() {
        if (currentReportType == null || currentReportType.isEmpty()) {
            showWarning("Najpierw wybierz typ raportu.");
            return;
        }

        filterOptionsPane.setVisible(!filterOptionsPane.isVisible());
        filterOptionsPane.setManaged(filterOptionsPane.isVisible());

        if (filterOptionsPane.isVisible()) {
            filterOptionsButton.setText("Ukryj opcje filtrowania");
        } else {
            filterOptionsButton.setText("Opcje filtrowania");
        }
    }

    private void updateFilterVisibility(String reportType) {
        boolean isProjectsReport = "Przegląd Projektów".equals(reportType);
        boolean isTeamStructureReport = "Struktura Zespołów".equals(reportType);
        boolean isUsersReport = "Użytkownicy Systemu".equals(reportType);

        groupsContainer.setVisible(isUsersReport);
        groupsContainer.setManaged(isUsersReport);
        groupsLabel.setVisible(isUsersReport);
        groupsLabel.setManaged(isUsersReport);
        groupsComboBox.setVisible(isUsersReport);
        groupsComboBox.setManaged(isUsersReport);

        setTeamSectionVisibility(isTeamStructureReport);

        setProjectSectionVisibility(isProjectsReport);

        boolean showDateFields = isProjectsReport;
        setDateAndStatsVisibility(showDateFields);

        setTasksOptionsVisibility(!isUsersReport);

        userTypesContainer.setVisible(isUsersReport);
        userTypesContainer.setManaged(isUsersReport);

        restrictOptionsForManager();

        setVisibility(showMembersCheckbox, !isProjectsReport);
        if (showMembersCheckbox != null) {
            showMembersCheckbox.setDisable(isProjectsReport);
        }

        handleAutoSelection(isTeamStructureReport, isProjectsReport);
        resetDatePickers();

        updateGenerateButtonState();
    }

    private void setTeamSectionVisibility(boolean visible) {
        setVisibility(teamsContainer, visible);
        setVisibility(teamsButtonsContainer, visible);
        setVisibility(teamsLabel, visible);

        if (teamsListView != null) {
            teamsListView.setVisible(visible);
            teamsListView.setManaged(visible);
            teamsListView.setDisable(!visible);
        }
    }

    private void setProjectSectionVisibility(boolean visible) {
        setVisibility(projectsListView, visible);
        setVisibility(projectsLabel, visible);
        setVisibility(selectAllProjectsBtn, visible);
        setVisibility(deselectAllProjectsBtn, visible);
    }

    private void setDateAndStatsVisibility(boolean visible) {
        setVisibility(dateRangeLabel, visible);
        setVisibility(startDatePicker, visible);
        setVisibility(endDatePicker, visible);
        setVisibility(showStatisticsCheckbox, visible);

        if (startDatePicker != null) {
            startDatePicker.setDisable(!visible);
        }
        if (endDatePicker != null) {
            endDatePicker.setDisable(!visible);
        }
        if (showStatisticsCheckbox != null) {
            showStatisticsCheckbox.setDisable(!visible);
        }
    }

    private void setTasksOptionsVisibility(boolean visible) {
        setVisibility(showTasksCheckbox, visible);
        if (showTasksCheckbox != null) {
            showTasksCheckbox.setDisable(!visible);
        }
    }

    private void restrictOptionsForManager() {
        User currentUser = MainApplication.getCurrentUser();
        if (currentUser != null && currentUser.getRoleId() == 2) {
            setVisibility(adminCheckbox, false);
            setVisibility(managerCheckbox, false);
        }
    }

    private void handleAutoSelection(boolean isTeamStructureReport, boolean isProjectsReport) {
        if (isTeamStructureReport && teamsListView != null) {
            selectedTeams.setAll(teamsListView.getItems());
        }

        if (isProjectsReport && projectsListView != null) {
            selectedProjects.setAll(projectsListView.getItems());
        }
    }

    private void resetDatePickers() {
        if (startDatePicker != null) {
            startDatePicker.setValue(null);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(null);
        }
    }

    private void setVisibility(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void loadReportTypes() {
        User currentUser = MainApplication.getCurrentUser();
        if (currentUser == null) return;

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Struktura Zespołów",
                "Użytkownicy Systemu",
                "Przegląd Projektów"
        ));
        reportTypeComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleGenerateReport() {
        try {
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser == null) {
                showError("Błąd", "Nie można zidentyfikować bieżącego użytkownika");
                return;
            }

            int roleId = currentUser.getRoleId();
            boolean isAdmin = (roleId == 1);
            int managerId = currentUser.getId();

            reportsArea.clear();

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                showWarning("Data początkowa nie może być późniejsza niż data końcowa");
                return;
            }

            Map<String, Object> filterOptions = new HashMap<>();
            filterOptions.put("selectedTeams", selectedTeams);
            filterOptions.put("selectedProjects", selectedProjects);
            filterOptions.put("startDate", startDate);
            filterOptions.put("endDate", endDate);
            filterOptions.put("showTasks", showTasks);
            filterOptions.put("showMembers", showMembers);
            filterOptions.put("showStatistics", showStatistics);
            filterOptions.put("showAdmins", showAdmins);
            filterOptions.put("showManagers", showManagers);
            filterOptions.put("showTeamLeaders", showTeamLeaders);
            filterOptions.put("showUsers", showUsers);
            filterOptions.put("selectedGroup", selectedGroup);

            switch (currentReportType) {
                case "Struktura Zespołów" -> generateTeamsStructureReport(filterOptions);
                case "Użytkownicy Systemu" -> generateUsersReport(filterOptions);
                case "Przegląd Projektów" -> generateProjectsOverviewReport(isAdmin, managerId, filterOptions);
                default -> {
                    showWarning("Wybierz typ raportu");
                    return;
                }
            }

            saveAsPdfButton.setDisable(false);

        } catch (Exception e) {
            showError("Błąd generowania raportu", e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateTeamsStructureReport(Map<String, Object> filterOptions) throws SQLException {
        List<Team> teamsToShow;
        List<Team> selectedTeams = (List<Team>) filterOptions.get("selectedTeams");
        boolean showTasks = (boolean) filterOptions.get("showTasks");
        boolean showMembers = (boolean) filterOptions.get("showMembers");

        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            teamsToShow = selectedTeams;
        } else {
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser != null && currentUser.getRoleId() == 2) {
                teamsToShow = teamDAO.getTeamsForManager(currentUser.getId()); 
            } else {
                teamsToShow = teamDAO.getAllTeams();
            }

        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: STRUKTURA ZESPOŁÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        report.append("Zastosowane filtry:\n");
        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            report.append("- Wybrane zespoły: ");
            for (int i = 0; i < selectedTeams.size(); i++) {
                report.append(selectedTeams.get(i).getTeamName());
                if (i < selectedTeams.size() - 1) report.append(", ");
            }
            report.append("\n");
        } else {
            report.append("- Wszystkie zespoły\n");
        }
        report.append("- Pokaż członków zespołów: ").append(showMembers ? "Tak" : "Nie").append("\n");
        report.append("- Pokaż zadania zespołów: ").append(showTasks ? "Tak" : "Nie").append("\n\n");

        if (teamsToShow.isEmpty()) {
            report.append("Brak zespołów spełniających kryteria raportu.\n");
        } else {
            for (Team team : teamsToShow) {
                report.append("=== ZESPÓŁ: ").append(team.getTeamName()).append(" (ID: ").append(team.getId()).append(") ===\n");

                int projectId = team.getProjectId();
                String projectName = "Brak przypisania";
                try {
                    for (Project project : projectDAO.getAllProjects()) {
                        if (project.getId() == projectId) {
                            projectName = project.getName();
                            break;
                        }
                    }
                } catch (Exception e) {
                    projectName = "Błąd pobierania projektu";
                }
                report.append("Projekt: ").append(projectName).append("\n");

                if (showMembers) {
                    List<User> members = teamDAO.getTeamMembers(team.getId());
                    report.append("Liczba członków: ").append(members.size()).append("\n");

                    if (members.isEmpty()) {
                        report.append("Brak członków zespołu.\n");
                    } else {
                        report.append("\nCZŁONKOWIE ZESPOŁU:\n");
                        for (User member : members) {
                            report.append("- ")
                                    .append(member.getName())
                                    .append(" ")
                                    .append(member.getLastName())
                                    .append(" (")
                                    .append(member.getEmail())
                                    .append(")\n");
                        }
                    }
                }

                if (showTasks) {
                    List<Task> tasks = taskDAO.getTasksByTeamId(team.getId());
                    report.append("\nZADANIA ZESPOŁU (").append(tasks.size()).append("):\n");

                    if (tasks.isEmpty()) {
                        report.append("Brak zadań przypisanych do zespołu.\n");
                    } else {
                        for (Task task : tasks) {
                            report.append("- ")
                                    .append(task.getTitle())
                                    .append(" (Status: ")
                                    .append(task.getStatus())
                                    .append(", Priorytet: ")
                                    .append(task.getPriority())
                                    .append(")\n");
                        }
                    }
                }

                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);

        User currentUser = MainApplication.getCurrentUser();
        if (currentUser != null && currentUser.getRoleId() == 2) {
            List<Team> allowedTeams = teamDAO.getTeamsForManager(currentUser.getId());
            teamsToShow.removeIf(team -> allowedTeams.stream().noneMatch(t -> t.getId() == team.getId()));
        }

    }

    private void generateUsersReport(Map<String, Object> filterOptions) throws SQLException {
        List<User> allUsers = userDAO.getAllUsers();
        List<User> users = new ArrayList<>();
        boolean showMembers = (boolean) filterOptions.get("showMembers");
        User currentUser = MainApplication.getCurrentUser();
        String selectedGroup = (String) filterOptions.get("selectedGroup");

        List<Integer> userIdsInGroup = null;
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            userIdsInGroup = userDAO.getUsersByGroupName(selectedGroup);
        }

        for (User user : allUsers) {
            if (userIdsInGroup != null && !userIdsInGroup.contains(user.getId())) {
                continue;
            }

            if (currentUser != null && currentUser.getRoleId() == 2) { 
                List<Project> managerProjects = projectDAO.getProjectsForManager(currentUser.getId());

                boolean isInManagerProjects = false;
                if (user.getRoleId() == 3) {
                    List<Integer> teamIds = teamMemberDAO.getTeamIdsForTeamLeader(user.getId());
                    for (Integer teamId : teamIds) {
                        if (teamId > 0) {
                            Team team = teamDAO.getTeamById(teamId);
                            if (team != null) {
                                for (Project project : managerProjects) {
                                    if (project.getId() == team.getProjectId()) {
                                        isInManagerProjects = true;
                                        break;
                                    }
                                }
                            }
                            if (isInManagerProjects) break;
                        }
                    }
                } else if (user.getRoleId() == 4) { 
                    int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
                    if (teamId > 0) {
                        Team team = teamDAO.getTeamById(teamId);
                        if (team != null) {
                            for (Project project : managerProjects) {
                                if (project.getId() == team.getProjectId()) {
                                    isInManagerProjects = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!isInManagerProjects) continue;

                if ((user.getRoleId() == 3 && showTeamLeaders) || (user.getRoleId() == 4 && showUsers)) {
                    users.add(user);
                }
            } else {
                if ((user.getRoleId() == 1 && showAdmins) ||
                        (user.getRoleId() == 2 && showManagers) ||
                        (user.getRoleId() == 3 && showTeamLeaders) ||
                        (user.getRoleId() == 4 && showUsers)) {
                    users.add(user);
                }
            }
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: UŻYTKOWNICY SYSTEMU\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        report.append("Zastosowane filtry:\n");
        if (currentUser != null && currentUser.getRoleId() == 2) {
            report.append("- Tylko użytkownicy z moich projektów\n");
        }
        if (selectedGroup != null && !selectedGroup.isEmpty()) {
            report.append("- Grupa: ").append(selectedGroup).append("\n");
        }
        report.append("- Typy użytkowników: ");
        List<String> selectedTypes = new ArrayList<>();
        if (showAdmins) selectedTypes.add("Administratorzy");
        if (showManagers) selectedTypes.add("Kierownicy");
        if (showTeamLeaders) selectedTypes.add("Team liderzy");
        if (showUsers) selectedTypes.add("Pracownicy");
        report.append(String.join(", ", selectedTypes)).append("\n");
        report.append("- Pokaż przynależność do zespołów: ").append(showMembers ? "Tak" : "Nie").append("\n\n");

        Map<Integer, String> roleNames = userDAO.getAllRolesMap();

        users.sort(Comparator.comparingInt(User::getRoleId));

        int currentRole = -1;
        for (User user : users) {
            if (user.getRoleId() != currentRole) {
                currentRole = user.getRoleId();
                report.append("\n=== ROLA: ").append(roleNames.getOrDefault(currentRole, "Nieznana (" + currentRole + ")")).append(" ===\n");
            }

            report.append("- ")
                    .append(user.getName())
                    .append(" ")
                    .append(user.getLastName())
                    .append(" (")
                    .append(user.getEmail())
                    .append(")\n");

            if (showMembers) {
                if (user.getRoleId() == 3) {
                    List<Integer> teamIds = teamMemberDAO.getTeamIdsForTeamLeader(user.getId());
                    if (!teamIds.isEmpty()) {
                        for (Integer teamId : teamIds) {
                            String teamName = teamDAO.getTeamNameById(teamId);
                            report.append("  Zespół: ").append(teamName).append("\n");
                        }
                    } else {
                        report.append("  Zespół: Brak przypisania\n");
                    }
                } else { 
                    int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
                    if (teamId > 0) {
                        String teamName = teamDAO.getTeamNameById(teamId);
                        report.append("  Zespół: ").append(teamName).append("\n");
                    } else {
                        report.append("  Zespół: Brak przypisania\n");
                    }
                }
            }

            if (selectedGroup != null && !selectedGroup.isEmpty()) {
                report.append("  Grupa: ").append(selectedGroup).append("\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    private void generateProjectsOverviewReport(boolean isAdmin, int managerId, Map<String, Object> filterOptions) throws SQLException {
        List<Project> allProjects;
        List<Project> filteredProjects = new ArrayList<>();
        LocalDate startDate = (LocalDate) filterOptions.get("startDate");
        LocalDate endDate = (LocalDate) filterOptions.get("endDate");
        boolean showTasks = (boolean) filterOptions.get("showTasks");
        boolean showStatistics = (boolean) filterOptions.get("showStatistics");

        if (isAdmin) {
            allProjects = projectDAO.getAllProjects();
        } else {
            allProjects = projectDAO.getProjectsForManager(managerId);
        }

        List<Project> selectedProjects = (List<Project>) filterOptions.get("selectedProjects");

        List<Project> baseProjects;

        if (selectedProjects != null && !selectedProjects.isEmpty()) {
            baseProjects = selectedProjects;
        } else {
            baseProjects = allProjects;
        }

        filteredProjects = baseProjects.stream()
                .filter(project -> startDate == null || !project.getEndDate().isBefore(startDate))
                .filter(project -> endDate == null || !project.getStartDate().isAfter(endDate))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: PRZEGLĄD PROJEKTÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        report.append("Zastosowane filtry:\n");
        if (startDate != null) {
            report.append("- Data początkowa: ").append(startDate).append("\n");
        }
        if (endDate != null) {
            report.append("- Data końcowa: ").append(endDate).append("\n");
        }
        report.append("- Pokaż zadania projektów: ").append(showTasks ? "Tak" : "Nie").append("\n");
        report.append("- Pokaż statystyki projektów: ").append(showStatistics ? "Tak" : "Nie").append("\n\n");

        report.append("Liczba projektów: ").append(filteredProjects.size()).append("\n\n");

        if (filteredProjects.isEmpty()) {
            report.append("Brak projektów spełniających kryteria raportu.\n");
        } else {
            for (Project project : filteredProjects) {
                report.append("=== PROJEKT: ").append(project.getName()).append(" (ID: ").append(project.getId()).append(") ===\n");
                report.append("Opis: ").append(project.getDescription()).append("\n");
                report.append("Data rozpoczęcia: ").append(project.getStartDate()).append("\n");
                report.append("Data zakończenia: ").append(project.getEndDate()).append("\n");

                int projectManagerId = project.getManagerId();
                String managerName = "Brak przypisania";
                if (projectManagerId > 0) {
                    User manager = userDAO.getUserById(projectManagerId);
                    if (manager != null) {
                        managerName = manager.getName() + " " + manager.getLastName();
                    }
                }
                report.append("Kierownik: ").append(managerName).append("\n");

                List<Team> projectTeams = new ArrayList<>();
                for (Team team : teamDAO.getAllTeams()) {
                    if (team.getProjectId() == project.getId()) {
                        projectTeams.add(team);
                    }
                }

                report.append("Liczba zespołów: ").append(projectTeams.size()).append("\n");

                if (!projectTeams.isEmpty()) {
                    report.append("Zespoły:\n");
                    for (Team team : projectTeams) {
                        report.append("- ").append(team.getTeamName()).append("\n");
                    }
                }

                List<Task> tasks = taskDAO.getTasksByProjectId(project.getId());

                if (showTasks) {
                    report.append("Liczba zadań: ").append(tasks.size()).append("\n");
                    for (Task task : tasks) {
                        report.append("- ").append(task.getTitle())
                                .append(" (Status: ").append(task.getStatus())
                                .append(", Priorytet: ").append(task.getPriority())
                                .append(")\n");
                    }
                }

                if (showStatistics) {
                    report.append("Statystyki zadań:\n");
                    if (tasks.isEmpty()) {
                        report.append("Brak danych do obliczenia statystyk.\n");
                    } else {
                        long newTasks = tasks.stream()
                                .filter(t -> "Nowe".equalsIgnoreCase(t.getStatus()))
                                .count();
                        long inProgressTasks = tasks.stream()
                                .filter(t -> "W toku".equalsIgnoreCase(t.getStatus()))
                                .count();
                        long completedTasks = tasks.stream()
                                .filter(t -> "Zakończone".equalsIgnoreCase(t.getStatus()))
                                .count();

                        report.append("- Nowe: ").append(newTasks).append("\n");
                        report.append("- W toku: ").append(inProgressTasks).append("\n");
                        report.append("- Zakończone: ").append(completedTasks).append("\n");

                        double completionPercentage = (double) completedTasks / tasks.size() * 100;
                        report.append("- Procent ukończenia: ").append(String.format("%.2f", completionPercentage)).append("%\n");
                    }
                }
                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    @FXML
    private void handleSaveAsPdf() {
        if (currentReportContent.isEmpty()) {
            showWarning("Najpierw wygeneruj raport");
            return;
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Wybierz folder do zapisu PDF");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        Stage stage = (Stage) reportsArea.getScene().getWindow();
        File selectedDir = dirChooser.showDialog(stage);

        if (selectedDir != null) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String sanitizedReportType = currentReportType.replaceAll("\\s+", "_").toLowerCase();
                String filename = selectedDir.getAbsolutePath() + File.separator +
                        "raport_" + sanitizedReportType + "_" + timestamp + ".pdf";

                Map<String, Object> filterOptions = new HashMap<>();
                filterOptions.put("selectedTeams", selectedTeams);
                filterOptions.put("startDate", startDate);
                filterOptions.put("endDate", endDate);
                filterOptions.put("showTasks", showTasks);
                filterOptions.put("showMembers", showMembers);
                filterOptions.put("showStatistics", showStatistics);
                filterOptions.put("showAdmins", showAdmins);
                filterOptions.put("showManagers", showManagers);
                filterOptions.put("showTeamLeaders", showTeamLeaders);
                filterOptions.put("showUsers", showUsers);
                filterOptions.put("selectedGroup", selectedGroup);

                switch (currentReportType) {
                    case "Struktura Zespołów" ->
                            reportService.generateTeamsStructurePdf(filename, currentReportContent, filterOptions);
                    case "Użytkownicy Systemu" ->
                            reportService.generateUsersReportPdf(filename, currentReportContent, filterOptions);
                    case "Przegląd Projektów" ->
                            reportService.generateProjectsOverviewPdf(filename, currentReportContent, filterOptions);
                    default -> {
                        showError("Błąd", "Nieznany typ raportu");
                        return;
                    }
                }

                showInfo("Raport zapisany", "Raport PDF został pomyślnie zapisany:\n" + filename);

                try {
                    java.awt.Desktop.getDesktop().open(new File(filename));
                } catch (Exception e) {
                    System.out.println("Nie można automatycznie otworzyć pliku PDF: " + e.getMessage());
                }

            } catch (IOException e) {
                showError("Błąd zapisu", "Nie udało się zapisać raportu PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda pomocnicza do wyświetlania komunikatów informacyjnych
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Metoda pomocnicza do wyświetlania ostrzeżeń
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Metoda pomocnicza do wyświetlania błędów
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}