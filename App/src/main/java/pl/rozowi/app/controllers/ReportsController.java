package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.reports.ReportService;
import pl.rozowi.app.util.Session;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private TextArea reportsArea;

    @FXML
    private Button generateButton;

    @FXML
    private Button saveAsPdfButton;

    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

    private ReportService reportService;
    private String currentReportContent = "";
    private String currentReportType = "";

    @FXML
    private void initialize() {
        reportService = new ReportService();

        // Ustaw styl monospace dla pola tekstowego raportu
        reportsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        // Dodaj typy raportów
        loadReportTypes();

        // Wyłącz przycisk zapisu PDF dopóki nie zostanie wygenerowany raport
        saveAsPdfButton.setDisable(true);

        // Nasłuchuj zmiany typu raportu
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentReportType = newValue;
                        generateButton.setDisable(false);
                    }
                });
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

            switch (currentReportType) {
                case "Struktura Zespołów":
                    generateTeamsStructureReport();
                    break;
                case "Użytkownicy Systemu":
                    generateUsersReport();
                    break;
                case "Przegląd Projektów":
                    generateProjectsOverviewReport(isAdmin, managerId);
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

    private void generateTeamsStructureReport() throws SQLException {
        List<Team> teams = teamDAO.getAllTeams();
        StringBuilder report = new StringBuilder();

        report.append("RAPORT: STRUKTURA ZESPOŁÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        if (teams.isEmpty()) {
            report.append("Brak zespołów w systemie.\n");
        } else {
            for (Team team : teams) {
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

                // Pobierz członków zespołu
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

                // Pobierz zadania przypisane do tego zespołu
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

                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    private void generateUsersReport() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        StringBuilder report = new StringBuilder();

        report.append("RAPORT: UŻYTKOWNICY SYSTEMU\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        report.append("Liczba użytkowników: ").append(users.size()).append("\n\n");

        // Pobierz nazwy ról
        java.util.Map<Integer, String> roleNames = userDAO.getAllRolesMap();

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

            // Pobierz przypisanie do zespołu
            int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
            if (teamId > 0) {
                String teamName = teamDAO.getTeamNameById(teamId);
                report.append("  Zespół: ").append(teamName).append("\n");
            } else {
                report.append("  Zespół: Brak przypisania\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    private void generateProjectsOverviewReport(boolean isAdmin, int managerId) throws SQLException {
        List<Project> projects;

        if (isAdmin) {
            // Administrator widzi wszystkie projekty
            projects = projectDAO.getAllProjects();
        } else {
            // Kierownik widzi tylko swoje projekty
            projects = projectDAO.getProjectsForManager(managerId);
        }

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: PRZEGLĄD PROJEKTÓW\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        report.append("Liczba projektów: ").append(projects.size()).append("\n\n");

        if (projects.isEmpty()) {
            report.append("Brak projektów do wyświetlenia.\n");
        } else {
            for (Project project : projects) {
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
                List<Team> projectTeams = new java.util.ArrayList<>();
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

                // Pobierz zadania dla tego projektu
                List<Task> tasks = taskDAO.getTasksByProjectId(project.getId());
                report.append("Liczba zadań: ").append(tasks.size()).append("\n");

                if (!tasks.isEmpty()) {
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

                // Generuj raport PDF przy użyciu ulepszonej usługi
                switch (currentReportType) {
                    case "Struktura Zespołów":
                        reportService.generateTeamsStructurePdf(filename, currentReportContent);
                        break;
                    case "Użytkownicy Systemu":
                        reportService.generateUsersReportPdf(filename, currentReportContent);
                        break;
                    case "Przegląd Projektów":
                        reportService.generateProjectsOverviewPdf(filename, currentReportContent);
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