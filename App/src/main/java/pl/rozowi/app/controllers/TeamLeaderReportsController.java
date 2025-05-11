package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.TeamLeaderReportService;
import pl.rozowi.app.util.Session;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeamLeaderReportsController {

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
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();

    private TeamLeaderReportService reportService;
    private String currentReportContent = "";
    private String currentReportType = "";

    @FXML
    private void initialize() {
        reportService = new TeamLeaderReportService();

        reportsArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Członkowie Zespołu",
                "Zadania Zespołu"
        ));

        saveAsPdfButton.setDisable(true);

        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentReportType = newValue;
                        generateButton.setDisable(false);
                        reportsArea.clear();
                        currentReportContent = "";
                        saveAsPdfButton.setDisable(true);
                    }
                });

        reportTypeComboBox.getSelectionModel().selectFirst();
    }

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

            switch (currentReportType) {
                case "Członkowie Zespołu":
                    generateTeamMembersReport(currentUser.getId());
                    break;
                case "Zadania Zespołu":
                    generateTeamTasksReport(currentUser.getId());
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

    private void generateTeamMembersReport(int teamLeaderId) throws SQLException {
        List<Integer> teamIds = teamMemberDAO.getTeamIdsForTeamLeader(teamLeaderId);

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: CZŁONKOWIE ZESPOŁU\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        if (teamIds.isEmpty()) {
            report.append("Nie jesteś liderem żadnego zespołu.\n");
        } else {
            for (int teamId : teamIds) {
                Team team = teamDAO.getTeamById(teamId);
                if (team == null) continue;

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

                List<User> members = teamDAO.getTeamMembers(teamId);
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

                report.append("\n\n");
            }
        }

        currentReportContent = report.toString();
        reportsArea.setText(currentReportContent);
    }

    private void generateTeamTasksReport(int teamLeaderId) throws SQLException {
        List<Integer> teamIds = teamMemberDAO.getTeamIdsForTeamLeader(teamLeaderId);

        StringBuilder report = new StringBuilder();

        report.append("RAPORT: ZADANIA ZESPOŁU\n");
        report.append("Data wygenerowania: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        if (teamIds.isEmpty()) {
            report.append("Nie jesteś liderem żadnego zespołu.\n");
        } else {
            for (int teamId : teamIds) {
                Team team = teamDAO.getTeamById(teamId);
                if (team == null) continue;

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

                List<Task> tasks = taskDAO.getTasksByTeamId(teamId);
                report.append("ZADANIA ZESPOŁU (").append(tasks.size()).append("):\n");

                if (tasks.isEmpty()) {
                    report.append("Brak zadań przypisanych do zespołu.\n");
                } else {
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

                        report.append("- ")
                                .append(task.getTitle())
                                .append(" (Status: ")
                                .append(task.getStatus())
                                .append(", Priorytet: ")
                                .append(task.getPriority())
                                .append(")\n");
                    }

                    report.append("\nPODSUMOWANIE STATUSÓW:\n");
                    report.append("- Nowe: ").append(newTasks).append("\n");
                    report.append("- W toku: ").append(inProgressTasks).append("\n");
                    report.append("- Zakończone: ").append(completedTasks).append("\n");

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