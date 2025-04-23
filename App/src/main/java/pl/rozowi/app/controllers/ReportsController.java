package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.ReportDAO;
import pl.rozowi.app.models.Report;
import pl.rozowi.app.models.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReportsController {

    @FXML
    private Button generateButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button viewReportButton;

    @FXML
    private TableView<Report> reportsTable;
    @FXML
    private TableColumn<Report, Number> colId;
    @FXML
    private TableColumn<Report, String> colName;
    @FXML
    private TableColumn<Report, String> colType;
    @FXML
    private TableColumn<Report, String> colDate;
    @FXML
    private TableColumn<Report, String> colCreatedBy;

    private final ReportDAO reportDAO = new ReportDAO();
    private ObservableList<Report> reportsData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        loadReports();

        generateButton.setOnAction(e -> openReportGenerator());
        refreshButton.setOnAction(e -> loadReports());
        viewReportButton.setOnAction(e -> openSelectedReport());

        // Początkowo przycisk podglądu wyłączony
        viewReportButton.setDisable(true);

        // Włącz przycisk podglądu gdy raport jest wybrany
        reportsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewReportButton.setDisable(newSelection == null);
        });
    }

    private void setupTable() {
        // Konfiguracja kolumn tabeli
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("reportType"));

        // Formatowanie daty w kolumnie
        colDate.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getCreatedAt() != null
                    ? sdf.format(cellData.getValue().getCreatedAt())
                    : ""
            );
        });

        // Kolumna z ID utworzonego przez
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdBy"));

        reportsTable.setItems(reportsData);
    }

    private void loadReports() {
        List<Report> reports = reportDAO.getAllReports();
        reportsData.clear();
        reportsData.addAll(reports);
    }

    private void openReportGenerator() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reportGenerator.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Generator raportów");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Po zamknięciu generatora, odśwież listę raportów
            loadReports();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd podczas otwierania generatora raportów: " + e.getMessage());
        }
    }

    private void openSelectedReport() {
        Report selectedReport = reportsTable.getSelectionModel().getSelectedItem();
        if (selectedReport == null) {
            showError("Wybierz raport do wyświetlenia");
            return;
        }

        String filePath = selectedReport.getExportedFile();
        if (filePath == null || filePath.isEmpty()) {
            showError("Brak ścieżki do pliku raportu");
            return;
        }

        try {
            File file = new File(filePath);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showError("Plik raportu nie istnieje: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Błąd podczas otwierania pliku: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sprawdza, czy zalogowany użytkownik ma uprawnienia do generowania raportów
     */
    public boolean hasReportingPermission() {
        User currentUser = MainApplication.getCurrentUser();
        return currentUser != null && (currentUser.getRoleId() == 1 || currentUser.getRoleId() == 2); // Admin lub Manager
    }

    /**
     * Ta metoda jest wywoływana po załadowaniu kontrolera przez setUser z dashboard
     */
    public void setUser(User user) {
        // Dostosuj widoczność przycisków w zależności od roli
        boolean canGenerateReports = hasReportingPermission();
        generateButton.setVisible(canGenerateReports);
        generateButton.setManaged(canGenerateReports);
    }
}