package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.ReportService;
import pl.rozowi.app.util.Session;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsController {

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

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

    private ReportService reportService;
    private String currentReportContent = "";
    private String currentReportType = "";

    // Ustawienia raportów
    private List<Team> selectedTeams = new ArrayList<>();
    private LocalDate startDate = null;
    private LocalDate endDate = null;
    private boolean showTasks = true;
    private boolean showMembers = true;
    private boolean showStatistics = true;

    @FXML
    private void initialize() {
        reportService = new ReportService();

        // Ustaw styl monospace dla pola tekstowego raportu
        reportsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        // Dodaj typy raportów
        loadReportTypes();

        // Inicjalizacja opcji filtrowania
        initFilterOptions();

        // Wyłącz przycisk zapisu PDF dopóki nie zostanie wygenerowany raport
        saveAsPdfButton.setDisable(true);

        // Nasłuchuj zmiany typu raportu
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentReportType = newValue;
                        generateButton.setDisable(false);

                        // Aktualizuj widoczność odpowiednich filtrów dla wybranego typu raportu
                        updateFilterVisibility(newValue);
                    }
                });

        // Domyślnie panel filtrów jest ukryty
        filterOptionsPane.setVisible(false);
        filterOptionsPane.setManaged(false);
    }

    private void initFilterOptions() {
        try {
            // Inicjalizacja listy zespołów
            List<Team> teams = teamDAO.getAllTeams();
            teamsListView.setCellFactory(new Callback<ListView<Team>, ListCell<Team>>() {
                @Override
                public ListCell<Team> call(ListView<Team> param) {
                    return new ListCell<Team>() {
                        private final CheckBox checkBox = new CheckBox();

                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            if (empty || team == null) {
                                setGraphic(null);
                            } else {
                                checkBox.setText(team.getTeamName());
                                checkBox.setSelected(selectedTeams.contains(team));
                                checkBox.setStyle("-fx-text-fill: white;");
                                checkBox.setOnAction(e -> {
                                    if (checkBox.isSelected()) {
                                        selectedTeams.add(team);
                                    } else {
                                        selectedTeams.remove(team);
                                    }
                                });
                                setGraphic(checkBox);
                            }
                        }
                    };
                }
            });
            teamsListView.setItems(FXCollections.observableArrayList(teams));

            // Obsługa pól dat
            LocalDate now = LocalDate.now();
            startDatePicker.setValue(now.minusMonths(1));
            endDatePicker.setValue(now);

            startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                startDate = newVal;
            });

            endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                endDate = newVal;
            });

            // Obsługa checkboxów
            showTasksCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showTasks = newVal;
            });

            showMembersCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showMembers = newVal;
            });

            showStatisticsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showStatistics = newVal;
            });

        } catch (SQLException e) {
            showError("Błąd inicjalizacji filtrów", e.getMessage());
        }
    }

    @FXML
    private void handleShowFilterOptions() {
        // Przełącz widoczność panelu filtrów
        filterOptionsPane.setVisible(!filterOptionsPane.isVisible());
        filterOptionsPane.setManaged(filterOptionsPane.isVisible());

        // Zmień tekst przycisku
        if (filterOptionsPane.isVisible()) {
            filterOptionsButton.setText("Ukryj opcje filtrowania");
        } else {
            filterOptionsButton.setText("Opcje filtrowania");
        }
    }

    private void updateFilterVisibility(String reportType) {
        // Dostosuj widoczność filtrów w zależności od typu raportu
        switch (reportType) {
            case "Struktura Zespołów":
                teamsListView.setDisable(false);
                startDatePicker.setDisable(true);
                endDatePicker.setDisable(true);
                showTasksCheckbox.setDisable(false);
                showMembersCheckbox.setDisable(false);
                showStatisticsCheckbox.setDisable(true);
                break;
            case "Użytkownicy Systemu":
                teamsListView.setDisable(true);
                startDatePicker.setDisable(true);
                endDatePicker.setDisable(true);
                showTasksCheckbox.setDisable(true);
                showMembersCheckbox.setDisable(false);
                showStatisticsCheckbox.setDisable(true);
                break;
            case "Przegląd Projektów":
                teamsListView.setDisable(true);
                startDatePicker.setDisable(false);
                endDatePicker.setDisable(false);
                showTasksCheckbox.setDisable(false);
                showMembersCheckbox.setDisable(true);
                showStatisticsCheckbox.setDisable(false);
                break;
            default:
                teamsListView.setDisable(false);
                startDatePicker.setDisable(false);
                endDatePicker.setDisable(false);
                showTasksCheckbox.setDisable(false);
                showMembersCheckbox.setDisable(false);
                showStatisticsCheckbox.setDisable(false);
        }
    }

    private void loadReportTypes() {
        User currentUser = MainApplication.getCurrentUser();
        if (currentUser == null) return;

        // Wszystkie typy raportów dostępne dla wszystkich użytkowników
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Struktura Zespołów",
                "Użytkownicy Systemu",
                "Przegląd Projektów"
        ));

        // Wybierz pierwszy element jako domyślny
        reportTypeComboBox.getSelectionModel().selectFirst();
        currentReportType = reportTypeComboBox.getValue();
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

            // Wyczyść poprzedni raport
            reportsArea.clear();

            // Pokaż informację o generowaniu raportu
            reportsArea.setText("Generowanie raportu...");

            // Sprawdź czy filtry są poprawne
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                showWarning("Data początkowa nie może być późniejsza niż data końcowa");
                return;
            }

            Map<String, Object> filterOptions = new HashMap<>();
            filterOptions.put("selectedTeams", selectedTeams);
            filterOptions.put("startDate", startDate);
            filterOptions.put("endDate", endDate);
            filterOptions.put("showTasks", showTasks);
            filterOptions.put("showMembers", showMembers);
            filterOptions.put("showStatistics", showStatistics);

            switch (currentReportType) {
                case "Struktura Zespołów":
                    generateTeamsStructureReport(filterOptions);
                    break;
                case "Użytkownicy Systemu":
                    generateUsersReport(filterOptions);
                    break;
                case "Przegląd Projektów":
                    generateProjectsOverviewReport(isAdmin, managerId, filterOptions);
                    break;
                default:
                    showWarning("Wybierz typ raportu");
                    return;
            }

            // Włącz przycisk zapisu PDF po pomyślnym wygenerowaniu
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

        // Jeśli wybrano konkretne zespoły, pokaż tylko je
        if (selectedTeams != null && !selectedTeams.isEmpty()) {
            teamsToShow = selectedTeams;
        } else {
            teamsToShow = teamDAO.getAllTeams();
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: STRUKTURA ZESPOŁÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        // Dodaj informację o zastosowanych filtrach
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

                // Pobierz nazwę projektu
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

                // Pobierz członków zespołu (jeśli filtr włączony)
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

                // Pobierz zadania przypisane do tego zespołu (jeśli filtr włączony)
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
    }

    private void generateUsersReport(Map<String, Object> filterOptions) throws SQLException {
        List<User> users = userDAO.getAllUsers();
        boolean showMembers = (boolean) filterOptions.get("showMembers");

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: UŻYTKOWNICY SYSTEMU\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        // Dodaj informację o zastosowanych filtrach
        report.append("Zastosowane filtry:\n");
        report.append("- Pokaż przynależność do zespołów: ").append(showMembers ? "Tak" : "Nie").append("\n\n");

        report.append("Liczba użytkowników: ").append(users.size()).append("\n\n");

        // Pobierz nazwy ról
        Map<Integer, String> roleNames = userDAO.getAllRolesMap();

        // Sortuj użytkowników według roli
        users.sort((u1, u2) -> Integer.compare(u1.getRoleId(), u2.getRoleId()));

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

            // Pobierz przypisanie do zespołu (jeśli filtr włączony)
            if (showMembers) {
                int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
                if (teamId > 0) {
                    String teamName = teamDAO.getTeamNameById(teamId);
                    report.append("  Zespół: ").append(teamName).append("\n");
                } else {
                    report.append("  Zespół: Brak przypisania\n");
                }
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    private LocalDate getProjectDateAsLocalDate(String dateStr) {
        try {
            // Jeśli data jest w formacie YYYY-MM-DD, parsujemy ją
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            // W przypadku błędu (nieprawidłowy format, null, itp.) zwracamy null
            System.out.println("Błąd parsowania daty: " + dateStr + " - " + e.getMessage());
            return null;
        }
    }

    //private void generateProjectsOverviewReport(boolean isAdmin, int managerId, Map<String, Object> filterOptions) throws SQLException {
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

        // Zastosuj filtr dat (jeśli podano)
        if (startDate != null || endDate != null) {
            for (Project project : allProjects) {
                // Tutaj daty są już w formacie LocalDate
                LocalDate projectStart = project.getStartDate(); // Zakładamy, że zwraca LocalDate
                LocalDate projectEnd = project.getEndDate();     // Zakładamy, że zwraca LocalDate

                boolean matchesFilter = true;
                if (startDate != null && projectEnd.isBefore(startDate)) {
                    matchesFilter = false;
                }
                if (endDate != null && projectStart.isAfter(endDate)) {
                    matchesFilter = false;
                }

                if (matchesFilter) {
                    filteredProjects.add(project);
                }
            }
        } else {
            filteredProjects = allProjects;
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: PRZEGLĄD PROJEKTÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        // Dodaj informację o zastosowanych filtrach
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

                // Pobierz kierownika
                int projectManagerId = project.getManagerId();
                String managerName = "Brak przypisania";
                if (projectManagerId > 0) {
                    User manager = userDAO.getUserById(projectManagerId);
                    if (manager != null) {
                        managerName = manager.getName() + " " + manager.getLastName();
                    }
                }
                report.append("Kierownik: ").append(managerName).append("\n");

                // Pobierz zespoły dla tego projektu
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

                // Pobierz zadania dla tego projektu (jeśli filtr włączony)
                if (showTasks) {
                    List<Task> tasks = taskDAO.getTasksByProjectId(project.getId());
                    report.append("Liczba zadań: ").append(tasks.size()).append("\n");

                    if (!tasks.isEmpty() && showStatistics) {
                        // Zlicz zadania według statusu
                        int newTasks = 0;
                        int inProgressTasks = 0;
                        int completedTasks = 0;

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
                        }

                        report.append("Status zadań:\n");
                        report.append("- Nowe: ").append(newTasks).append("\n");
                        report.append("- W toku: ").append(inProgressTasks).append("\n");
                        report.append("- Zakończone: ").append(completedTasks).append("\n");

                        // Oblicz procent ukończenia
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

        // Otwórz okno wyboru lokalizacji zapisu
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Wybierz folder do zapisu PDF");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        Stage stage = (Stage) reportsArea.getScene().getWindow();
        File selectedDir = dirChooser.showDialog(stage);

        if (selectedDir != null) {
            try {
                // Generuj nazwę pliku na podstawie typu raportu i znacznika czasu
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String sanitizedReportType = currentReportType.replaceAll("\\s+", "_").toLowerCase();
                String filename = selectedDir.getAbsolutePath() + File.separator +
                        "raport_" + sanitizedReportType + "_" + timestamp + ".pdf";

                // Pobierz aktualne opcje filtrowania
                Map<String, Object> filterOptions = new HashMap<>();
                filterOptions.put("selectedTeams", selectedTeams);
                filterOptions.put("startDate", startDate);
                filterOptions.put("endDate", endDate);
                filterOptions.put("showTasks", showTasks);
                filterOptions.put("showMembers", showMembers);
                filterOptions.put("showStatistics", showStatistics);

                // Generuj raport PDF przy użyciu ulepszonej usługi
                switch (currentReportType) {
                    case "Struktura Zespołów":
                        reportService.generateTeamsStructurePdf(filename, currentReportContent, filterOptions);
                        break;
                    case "Użytkownicy Systemu":
                        reportService.generateUsersReportPdf(filename, currentReportContent, filterOptions);
                        break;
                    case "Przegląd Projektów":
                        reportService.generateProjectsOverviewPdf(filename, currentReportContent, filterOptions);
                        break;
                    default:
                        showError("Błąd", "Nieznany typ raportu");
                        return;
                }

                showInfo("Raport zapisany", "Raport PDF został pomyślnie zapisany:\n" + filename);

                // Próba otwarcia pliku w domyślnym programie
                try {
                    java.awt.Desktop.getDesktop().open(new File(filename));
                } catch (Exception e) {
                    // Jeśli nie udało się otworzyć, po prostu pomijamy tę akcję
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