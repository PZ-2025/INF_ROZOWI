package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.ReportService;
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

        // Load report types based on user role
        loadReportTypes();

        // Disable the save PDF button until a report is generated
        saveAsPdfButton.setDisable(true);

        // Set up listeners
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

        // All users can see all report types
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Struktura Zespołów",
                "Użytkownicy Systemu",
                "Przegląd Projektów"
        ));

        // Select the first item by default
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

            // Enable the save PDF button after successful generation
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

                // Get project name
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

                // Get team members
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

                // Get tasks assigned to this team
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

        // Get role names
        java.util.Map<Integer, String> roleNames = userDAO.getAllRolesMap();

        // Sort users by role
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

            // Get team assignment
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
            // Admin can see all projects
            projects = projectDAO.getAllProjects();
        } else {
            // Manager can only see their projects
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

                // Get manager
                int projectManagerId = project.getManagerId();
                String managerName = "Brak przypisania";
                if (projectManagerId > 0) {
                    User manager = userDAO.getUserById(projectManagerId);
                    if (manager != null) {
                        managerName = manager.getName() + " " + manager.getLastName();
                    }
                }
                report.append("Kierownik: ").append(managerName).append("\n");

                // Get teams for this project
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

                // Get tasks for this project
                List<Task> tasks = taskDAO.getTasksByProjectId(project.getId());
                report.append("Liczba zadań: ").append(tasks.size()).append("\n");

                if (!tasks.isEmpty()) {
                    // Count tasks by status
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

                    // Calculate completion percentage
                    double completionPercentage = (double) completedTasks / tasks.size() * 100;
                    report.append("Procent ukończenia: ").append(String.format("%.2f", completionPercentage)).append("%\n");
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

        // Open file chooser for save location
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Wybierz folder do zapisu PDF");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        Stage stage = (Stage) reportsArea.getScene().getWindow();
        File selectedDir = dirChooser.showDialog(stage);

        if (selectedDir != null) {
            try {
                // Generate filename based on report type and timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String sanitizedReportType = currentReportType.replaceAll("\\s+", "_").toLowerCase();
                String filename = selectedDir.getAbsolutePath() + File.separator +
                                 "raport_" + sanitizedReportType + "_" + timestamp + ".pdf";

                // Generate PDF using the service
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

            } catch (IOException e) {
                showError("Błąd zapisu", "Nie udało się zapisać raportu PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
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