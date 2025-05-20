package pl.rozowi.app.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.TeamLeaderReportService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kontroler odpowiedzialny za generowanie i zarządzanie raportami dla liderów zespołów.
 * Umożliwia tworzenie raportów dotyczących członków zespołu oraz zadań przypisanych do zespołów.
 */
public class TeamLeaderReportsController {

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
    private ComboBox<String> groupsComboBox;
    @FXML
    private ComboBox<User> usersComboBox;
    @FXML
    private Label usersLabel;
    @FXML
    private HBox teamsContainer;
    @FXML
    private HBox teamsButtonsContainer;
    @FXML
    private VBox groupsContainer;
    @FXML
    private Label groupsLabel;
    @FXML
    private CheckBox showTasksCheckbox;
    @FXML
    private CheckBox showMembersCheckbox;
    @FXML
    private CheckBox showStatisticsCheckbox;

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();

    private TeamLeaderReportService reportService;
    private String currentReportContent = "";
    private String currentReportType = "";

    private ObservableList<Team> selectedTeams = FXCollections.observableArrayList();
    private String selectedGroup = null;
    private User selectedUser = null;
    private boolean showTasks = true;
    private boolean showMembers = true;
    private boolean showStatistics = true;

    /**
     * Inicjalizacja kontrolera.
     * Konfiguruje dostępne typy raportów, ustawia style i inicjalizuje filtry.
     */
    @FXML
    private void initialize() {
        reportService = new TeamLeaderReportService();
        reportsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Członkowie Zespołu",
                "Zadania Zespołu"
        ));

        saveAsPdfButton.setDisable(true);
        filterOptionsPane.setVisible(false);
        filterOptionsPane.setManaged(false);

        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentReportType = newValue;
                        reportsArea.clear();
                        currentReportContent = "";
                        saveAsPdfButton.setDisable(true);
                        updateFilterVisibility(newValue);
                        updateGenerateButtonState();
                    }
                });

        reportTypeComboBox.getSelectionModel().selectFirst();

        initFilterOptions();

        showTasksCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showTasks = newVal;
        });

        showMembersCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showMembers = newVal;
        });

        showStatisticsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showStatistics = newVal;
        });

        selectedTeams.addListener((ListChangeListener<Team>) change -> updateGenerateButtonState());

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

        usersComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
            updateGenerateButtonState();
        });
        updateGenerateButtonState();
    }

    /**
     * Inicjalizuje opcje filtrów dla raportów.
     * Ładuje listę zespołów, grup i użytkowników dostępnych dla aktualnego lidera.
     */
    private void initFilterOptions() {
        try {
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser == null || currentUser.getRoleId() != 3) {
                return;
            }

            List<Team> teamsLedByUser = teamDAO.getTeamsByLeaderIdAsList(currentUser.getId());

            if (teamsLedByUser.isEmpty()) {
                teamsListView.setItems(FXCollections.observableArrayList());
                return;
            }

            selectedTeams.setAll(teamsLedByUser);

            teamsListView.setCellFactory(param -> new ListCell<>() {
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
            });

            teamsListView.setItems(FXCollections.observableArrayList(teamsLedByUser));

            groupsComboBox.getItems().add("Wszystkie grupy");
            groupsComboBox.getItems().addAll(userDAO.getAllGroupNames());
            groupsComboBox.getSelectionModel().selectFirst();

            List<User> teamMembers = new ArrayList<>();
            for (Team team : teamsLedByUser) {
                List<User> members = teamDAO.getTeamMembers(team.getId());
                members = members.stream()
                        .filter(user -> user.getRoleId() == 4)
                        .collect(Collectors.toList());
                teamMembers.addAll(members);
            }

            usersComboBox.setItems(FXCollections.observableArrayList(teamMembers));
            usersComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getName() + " " + user.getLastName());
                    }
                }
            });
            usersComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText("Wszyscy użytkownicy");
                    } else {
                        setText(user.getName() + " " + user.getLastName());
                    }
                }
            });

        } catch (SQLException e) {
            showError("Błąd inicjalizacji filtrów", e.getMessage());
        }
    }

    /**
     * Obsługuje pokazywanie/ukrywanie panelu z opcjami filtrowania.
     */
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

    /**
     * Aktualizuje widoczność sekcji filtrów w zależności od wybranego typu raportu.
     * @param reportType Typ raportu wybrany przez użytkownika
     */
    private void updateFilterVisibility(String reportType) {
        boolean isMembersReport = "Członkowie Zespołu".equals(reportType);
        boolean isTasksReport = "Zadania Zespołu".equals(reportType);

        setTeamSectionVisibility(true);
        setVisibility(groupsContainer, isMembersReport);
        setVisibility(groupsLabel, isMembersReport);
        setVisibility(usersLabel, isMembersReport);
        setVisibility(usersComboBox, isMembersReport);

        setVisibility(showTasksCheckbox, isTasksReport);
        setVisibility(showStatisticsCheckbox, isTasksReport);
        setVisibility(showMembersCheckbox, isMembersReport);

        updateGenerateButtonState();
    }

    /**
     * Ustawia widoczność sekcji zespołów.
     * @param visible Czy sekcja ma być widoczna
     */
    private void setTeamSectionVisibility(boolean visible) {
        setVisibility(teamsContainer, visible);
        setVisibility(teamsButtonsContainer, visible);

        if (teamsListView != null) {
            teamsListView.setVisible(visible);
            teamsListView.setManaged(visible);
            teamsListView.setDisable(!visible);
        }
    }

    /**
     * Ustawia widoczność elementu interfejsu.
     * @param node Element interfejsu
     * @param visible Czy element ma być widoczny
     */
    private void setVisibility(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    /**
     * Aktualizuje stan przycisku generowania raportu.
     */
    private void updateGenerateButtonState() {
        boolean reportTypeSelected = currentReportType != null && !currentReportType.isEmpty();
        boolean teamsSelected = !selectedTeams.isEmpty();

        generateButton.setDisable(!(reportTypeSelected && teamsSelected));
    }

    /**
     * Zaznacza wszystkie zespoły na liście.
     */
    @FXML
    private void handleSelectAllTeams() {
        selectedTeams.setAll(teamsListView.getItems());
        teamsListView.refresh();
        updateGenerateButtonState();
    }

    /**
     * Odznacza wszystkie zespoły na liście.
     */
    @FXML
    private void handleDeselectAllTeams() {
        selectedTeams.clear();
        teamsListView.refresh();
        updateGenerateButtonState();
    }

    /**
     * Generuje raport na podstawie wybranych filtrów i opcji.
     */
    @FXML
    private void handleGenerateReport() {
        try {
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser == null) {
                showError("Błąd", "Nie można zidentyfikować bieżącego użytkownika");
                return;
            }

            if (currentUser.getRoleId() != 3) {
                showError("Brak uprawnień", "Tylko Team Leaderzy mogą generować te raporty");
                return;
            }

            reportsArea.clear();
            reportsArea.setText("Generowanie raportu...");

            Map<String, Object> filterOptions = new HashMap<>();
            filterOptions.put("selectedTeams", selectedTeams);
            filterOptions.put("selectedGroup", selectedGroup);
            filterOptions.put("selectedUser", selectedUser);
            filterOptions.put("showTasks", showTasks);
            filterOptions.put("showMembers", showMembers);
            filterOptions.put("showStatistics", showStatistics);

            switch (currentReportType) {
                case "Członkowie Zespołu":
                    generateTeamMembersReport(currentUser.getId(), filterOptions);
                    break;
                case "Zadania Zespołu":
                    generateTeamTasksReport(currentUser.getId(), filterOptions);
                    break;
                default:
                    showWarning("Wybierz typ raportu");
                    return;
            }

            saveAsPdfButton.setDisable(false);

        } catch (Exception e) {
            showError("Błąd generowania raportu", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generuje raport dotyczący członków zespołu.
     * @param teamLeaderId ID lidera zespołu
     * @param filterOptions Mapa zawierająca opcje filtrowania
     * @throws SQLException W przypadku błędu dostępu do bazy danych
     */
    private void generateTeamMembersReport(int teamLeaderId, Map<String, Object> filterOptions) throws SQLException {
        List<Team> teamsByLeader = teamDAO.getTeamsByLeaderIdAsList(teamLeaderId);
        List<Team> teamsToShow = new ArrayList<>();
        String selectedGroup = (String) filterOptions.get("selectedGroup");
        User selectedUser = (User) filterOptions.get("selectedUser");
        boolean showMembers = (boolean) filterOptions.get("showMembers");

        List<Team> selectedTeams = (List<Team>) filterOptions.get("selectedTeams");

        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            for (Team leaderTeam : teamsByLeader) {
                if (selectedTeams.stream().anyMatch(t -> t.getId() == leaderTeam.getId())) {
                    teamsToShow.add(leaderTeam);
                }
            }
        } else {
            teamsToShow.addAll(teamsByLeader);
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: CZŁONKOWIE ZESPOŁU\n");
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
        }
        if (selectedGroup != null && !selectedGroup.isEmpty() && !"Wszystkie grupy".equals(selectedGroup)) {
            report.append("- Grupa: ").append(selectedGroup).append("\n");
        }
        if (selectedUser != null) {
            report.append("- Użytkownik: ").append(selectedUser.getName()).append(" ").append(selectedUser.getLastName()).append("\n");
        }
        report.append("- Pokaż członków zespołów: ").append(showMembers ? "Tak" : "Nie").append("\n\n");

        if (teamsToShow.isEmpty()) {
            report.append("Nie jesteś liderem żadnego zespołu lub brak zespołów spełniających kryteria.\n");
        } else {
            for (Team team : teamsToShow) {
                report.append("=== ZESPÓŁ: ").append(team.getTeamName()).append(" (ID: ").append(team.getId()).append(") ===\n");

                String projectName = "Brak przypisania";
                try {
                    Project project = projectDAO.getProjectById(team.getProjectId());
                    if (project != null) {
                        projectName = project.getName();
                    }
                } catch (Exception e) {
                    projectName = "Błąd pobierania projektu";
                }
                report.append("Projekt: ").append(projectName).append("\n\n");

                if (showMembers) {
                    List<User> members = teamDAO.getTeamMembers(team.getId());

                    members = members.stream()
                            .filter(user -> user.getRoleId() == 4)
                            .collect(Collectors.toList());

                    if (selectedGroup != null && !selectedGroup.isEmpty() && !"Wszystkie grupy".equals(selectedGroup)) {
                        List<Integer> userIdsInGroup = userDAO.getUsersByGroupName(selectedGroup);
                        members = members.stream()
                                .filter(user -> userIdsInGroup.contains(user.getId()))
                                .collect(Collectors.toList());
                    }

                    if (selectedUser != null) {
                        members = members.stream()
                                .filter(user -> user.getId() == selectedUser.getId())
                                .collect(Collectors.toList());
                    }

                    report.append("Liczba członków: ").append(members.size()).append("\n");

                    if (members.isEmpty()) {
                        report.append("Brak członków zespołu spełniających kryteria.\n");
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

                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    /**
     * Generuje raport dotyczący zadań zespołu.
     * @param teamLeaderId ID lidera zespołu
     * @param filterOptions Mapa zawierająca opcje filtrowania
     * @throws SQLException W przypadku błędu dostępu do bazy danych
     */
    private void generateTeamTasksReport(int teamLeaderId, Map<String, Object> filterOptions) throws SQLException {
        List<Team> teamsByLeader = teamDAO.getTeamsByLeaderIdAsList(teamLeaderId);
        List<Team> teamsToShow = new ArrayList<>();
        boolean showTasks = (boolean) filterOptions.get("showTasks");
        boolean showStatistics = (boolean) filterOptions.get("showStatistics");
        User selectedUser = (User) filterOptions.get("selectedUser");

        List<Team> selectedTeams = (List<Team>) filterOptions.get("selectedTeams");

        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            for (Team leaderTeam : teamsByLeader) {
                if (selectedTeams.stream().anyMatch(t -> t.getId() == leaderTeam.getId())) {
                    teamsToShow.add(leaderTeam);
                }
            }
        } else {
            teamsToShow.addAll(teamsByLeader);
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: ZADANIA ZESPOŁU\n");
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
        }
        if (selectedUser != null) {
            report.append("- Użytkownik: ").append(selectedUser.getName()).append(" ").append(selectedUser.getLastName()).append("\n");
        }
        report.append("- Pokaż zadania: ").append(showTasks ? "Tak" : "Nie").append("\n");
        report.append("- Pokaż statystyki: ").append(showStatistics ? "Tak" : "Nie").append("\n\n");

        if (teamsToShow.isEmpty()) {
            report.append("Nie jesteś liderem żadnego zespołu lub brak zespołów spełniających kryteria.\n");
        } else {
            for (Team team : teamsToShow) {
                report.append("=== ZESPÓŁ: ").append(team.getTeamName()).append(" (ID: ").append(team.getId()).append(") ===\n");

                String projectName = "Brak przypisania";
                try {
                    Project project = projectDAO.getProjectById(team.getProjectId());
                    if (project != null) {
                        projectName = project.getName();
                    }
                } catch (Exception e) {
                    projectName = "Błąd pobierania projektu";
                }
                report.append("Projekt: ").append(projectName).append("\n\n");

                List<Task> tasks = taskDAO.getTasksByTeamId(team.getId());

                if (selectedUser != null) {
                    tasks = tasks.stream()
                            .filter(task -> task.getAssignedTo() == selectedUser.getId())
                            .collect(Collectors.toList());
                }

                report.append("ZADANIA ZESPOŁU (").append(tasks.size()).append("):\n");

                if (tasks.isEmpty()) {
                    report.append("Brak zadań przypisanych do zespołu spełniających kryteria.\n");
                } else {
                    int newTasks = 0;
                    int inProgressTasks = 0;
                    int completedTasks = 0;

                    if (showTasks) {
                        for (Task task : tasks) {
                            String status = task.getStatus();
                            if (status == null) continue;

                            if (status.equalsIgnoreCase("Nowe")) {
                                newTasks++;
                            } else if (status.equalsIgnoreCase("W toku")) {
                                inProgressTasks++;
                            } else if (status.equalsIgnoreCase("Zakończone")) {
                                completedTasks++;
                            }

                            report.append("- ")
                                    .append(task.getTitle())
                                    .append(" (Status: ")
                                    .append(task.getStatus())
                                    .append(", Priorytet: ")
                                    .append(task.getPriority())
                                    .append(")\n");

                            if (selectedUser != null) {
                                User assignee = userDAO.getUserById(task.getAssignedTo());
                                if (assignee != null) {
                                    report.append("  Przypisane do: ")
                                            .append(assignee.getName())
                                            .append(" ")
                                            .append(assignee.getLastName())
                                            .append("\n");
                                }
                            }
                        }
                    }

                    if (showStatistics) {
                        report.append("\nPODSUMOWANIE STATUSÓW:\n");
                        report.append("- Nowe: ").append(newTasks).append("\n");
                        report.append("- W toku: ").append(inProgressTasks).append("\n");
                        report.append("- Zakończone: ").append(completedTasks).append("\n");

                        if (tasks.size() > 0) {
                            double completionPercentage = (double) completedTasks / tasks.size() * 100;
                            report.append("- Procent ukończenia: ").append(String.format("%.2f", completionPercentage)).append("%\n");
                        }
                    }
                }

                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    /**
     * Zapisuje wygenerowany raport do pliku PDF.
     */
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

                switch (currentReportType) {
                    case "Członkowie Zespołu":
                        reportService.generateTeamMembersReportPdf(filename, currentReportContent);
                        break;
                    case "Zadania Zespołu":
                        reportService.generateTeamTasksReportPdf(filename, currentReportContent);
                        break;
                    default:
                        showError("Błąd", "Nieznany typ raportu");
                        return;
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
     * Wyświetla okno dialogowe z informacją.
     * @param title Tytuł okna dialogowego
     * @param message Treść wiadomości
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
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
     * @param title Tytuł okna dialogowego
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