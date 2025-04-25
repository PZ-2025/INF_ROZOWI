package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.ReportDAO;
import pl.rozowi.app.models.Report;
import pl.rozowi.app.models.User;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class ReportsController {

    @FXML
    private Button generateButton;
    @FXML
    private TextArea reportsArea;

    private ReportDAO reportDAO = new ReportDAO();

    @FXML
    private void initialize() {
        displayAllReports();
    }

    @FXML
    private void handleGenerateReport() {
        User currentUser = MainApplication.getCurrentUser();
        if (currentUser == null) {
            reportsArea.setText("Brak zalogowanego użytkownika.");
            return;
        }

        Report report = new Report();
        String reportName;
        String reportType;
        String reportScope;
        String exportedFile = "";

        int roleId = currentUser.getRoleId();
        switch (roleId) {
            case 1: // Administrator
                reportName = "Raport systemowy";
                reportType = "system_overview";
                reportScope = "Dane: Użytkownicy, Projekty, Zespoły, Zadania, Raporty.";
                break;
            case 2: // Kierownik
                reportName = "Raport postępu zespołu";
                reportType = "team_progress";
                reportScope = "Dane: Postęp projektów, Zadania zespołu, Aktywności.";
                break;
            case 3: // Użytkownik
                reportName = "Raport wydajności użytkownika";
                reportType = "user_performance";
                reportScope = "Dane: Twoje zadania, Postęp, Aktywności.";
                break;
            default:
                reportName = "Raport ogólny";
                reportType = "general";
                reportScope = "Ogólne dane systemowe.";
                break;
        }

        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setReportScope(reportScope);
        report.setCreatedBy(currentUser.getId());
        report.setCreatedAt(Timestamp.from(Instant.now()));
        report.setExportedFile(exportedFile);

        boolean inserted = reportDAO.insertReport(report);
        if (inserted) {
            reportsArea.setText("Wygenerowano raport:\n" +
                    "Nazwa: " + report.getReportName() + "\n" +
                    "Typ: " + report.getReportType() + "\n" +
                    "Zakres: " + report.getReportScope() + "\n" +
                    "Data: " + report.getCreatedAt());
        } else {
            reportsArea.setText("Wystąpił błąd podczas generowania raportu.");
        }

        displayAllReports();
    }

    private void displayAllReports() {
        List<Report> reports = reportDAO.getAllReports();
        StringBuilder sb = new StringBuilder();
        for (Report r : reports) {
            sb.append("ID: ").append(r.getId()).append("\n")
                    .append("Nazwa: ").append(r.getReportName()).append("\n")
                    .append("Typ: ").append(r.getReportType()).append("\n")
                    .append("Zakres: ").append(r.getReportScope()).append("\n")
                    .append("Utworzony przez: ").append(r.getCreatedBy()).append("\n")
                    .append("Data: ").append(r.getCreatedAt()).append("\n")
                    .append("Plik: ").append(r.getExportedFile()).append("\n")
                    .append("------------------------------------\n");
        }
        reportsArea.setText(sb.toString());
    }
}
