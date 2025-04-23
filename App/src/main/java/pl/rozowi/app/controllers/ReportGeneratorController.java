package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.ReportService;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

public class ReportGeneratorController {

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> priorityFilterComboBox;

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private Button generateButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button previewButton;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private GridPane formGrid;

    @FXML
    private VBox filterControls;

    private final ReportService reportService;
    private final ProjectDAO projectDAO;
    private final UserDAO userDAO;

    private String generatedFilePath;

    public ReportGeneratorController() {
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();
        TaskDAO taskDAO = new TaskDAO();
        TeamDAO teamDAO = new TeamDAO();
        TaskAssignmentDAO taskAssignmentDAO = new TaskAssignmentDAO();

        reportService = new ReportService(userDAO, taskDAO, projectDAO, teamDAO, taskAssignmentDAO);
    }

    @FXML
    private void initialize() {
        // Domyślnie ukryj przyciski pobierania i podglądu
        downloadButton.setVisible(false);
        previewButton.setVisible(false);
        // Typy raportów
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Raport obciążenia pracowników",
                "Raport postępu projektu",
                "Raport wydajności użytkowników"
        ));
        reportTypeComboBox.getSelectionModel().selectFirst();

        // Filtr statusu
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "Wszystkie",
                "Nowe",
                "W toku",
                "Zakończone"
        ));
        statusFilterComboBox.getSelectionModel().selectFirst();

        // Filtr priorytetu
        priorityFilterComboBox.setItems(FXCollections.observableArrayList(
                "Wszystkie",
                "LOW",
                "MEDIUM",
                "HIGH"
        ));
        priorityFilterComboBox.getSelectionModel().selectFirst();

        // Ustaw daty domyślne (ostatni miesiąc)
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Załaduj projekty do comboboxa
        try {
            List<Project> projects = projectDAO.getAllProjects();
            projectComboBox.setItems(FXCollections.observableArrayList(projects));
            if (!projects.isEmpty()) {
                projectComboBox.getSelectionModel().selectFirst();
            }

            // Dodaj converter do wyświetlania nazw projektów
            projectComboBox.setConverter(new javafx.util.StringConverter<Project>() {
                @Override
                public String toString(Project project) {
                    return project == null ? "" : project.getId() + " - " + project.getName();
                }

                @Override
                public Project fromString(String string) {
                    return null;
                }
            });

            // Załaduj użytkowników do comboboxa
            List<User> users = userDAO.getAllUsers();
            userComboBox.setItems(FXCollections.observableArrayList(users));
            if (!users.isEmpty()) {
                userComboBox.getSelectionModel().selectFirst();
            }

            // Dodaj converter do wyświetlania nazw użytkowników
            userComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user == null ? "" : user.getId() + " - " + user.getName() + " " + user.getLastName();
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Obsługa zmiany typu raportu - dostosuj widoczność kontrolek
            reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                updateControlsVisibility(newVal);
            });

            // Ustaw początkową widoczność kontrolek
            updateControlsVisibility(reportTypeComboBox.getValue());

            // Przyciski akcji
            generateButton.setOnAction(e -> generateReport());
            downloadButton.setOnAction(e -> downloadReport());
            previewButton.setOnAction(e -> previewReport());

            // Przyciski pobierania i podglądu na początku są wyłączone
            downloadButton.setDisable(true);
            previewButton.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            resultTextArea.setText("Wystąpił błąd podczas ładowania danych: " + e.getMessage());
        }
    }

    /**
     * Dostosowuje widoczność kontrolek w zależności od wybranego typu raportu
     */
    private void updateControlsVisibility(String reportType) {
        if (reportType == null) return;

        // Domyślnie wszystko widoczne
        projectComboBox.setVisible(true);
        userComboBox.setVisible(true);
        statusFilterComboBox.setVisible(true);
        priorityFilterComboBox.setVisible(true);

        switch (reportType) {
            case "Raport obciążenia pracowników":
                projectComboBox.setVisible(false);
                break;

            case "Raport postępu projektu":
                userComboBox.setVisible(false);
                priorityFilterComboBox.setVisible(false);
                break;

            case "Raport wydajności użytkowników":
                projectComboBox.setVisible(false);
                statusFilterComboBox.setVisible(false);
                priorityFilterComboBox.setVisible(false);
                break;
        }
    }

    /**
     * Generuje raport na podstawie wybranych parametrów
     */
    private void generateReport() {
        try {
            // Pobierz wybrane parametry
            String reportType = reportTypeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            // Sprawdź czy daty są poprawne
            if (startDate == null || endDate == null) {
                showError("Wybierz daty początkową i końcową.");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showError("Data początkowa nie może być późniejsza niż data końcowa.");
                return;
            }

            // Pobierz filtry
            String statusFilter = statusFilterComboBox.getValue();
            if (statusFilter != null && statusFilter.equals("Wszystkie")) {
                statusFilter = null;
            }

            String priorityFilter = priorityFilterComboBox.getValue();
            if (priorityFilter != null && priorityFilter.equals("Wszystkie")) {
                priorityFilter = null;
            }

            // Pobierz aktualnego użytkownika
            User currentUser = MainApplication.getCurrentUser();
            if (currentUser == null) {
                showError("Nie znaleziono zalogowanego użytkownika.");
                return;
            }

            resultTextArea.setText("Generowanie raportu...");

            // Generuj właściwy raport w zależności od typu
            switch (reportType) {
                case "Raport obciążenia pracowników":
                    User selectedUser = userComboBox.getValue();
                    Integer userId = selectedUser != null ? selectedUser.getId() : null;

                    // Jeśli to nie admin, pokaż tylko swoje dane
                    if (currentUser.getRoleId() != 1) {
                        userId = currentUser.getId();
                    }

                    generatedFilePath = reportService.generateEmployeeWorkloadReport(userId, startDate, endDate, statusFilter, priorityFilter);
                    break;

                case "Raport postępu projektu":
                    Project selectedProject = projectComboBox.getValue();
                    Integer projectId = selectedProject != null ? selectedProject.getId() : null;

                    generatedFilePath = reportService.generateProjectProgressReport(projectId, startDate, endDate, statusFilter);
                    break;

                case "Raport wydajności użytkowników":
                    User selectedPerformanceUser = userComboBox.getValue();
                    Integer performanceUserId = selectedPerformanceUser != null ? selectedPerformanceUser.getId() : null;

                    // Jeśli to nie admin, pokaż tylko swoje dane
                    if (currentUser.getRoleId() != 1) {
                        performanceUserId = currentUser.getId();
                    }

                    generatedFilePath = reportService.generateUserPerformanceReport(performanceUserId, startDate, endDate);
                    break;

                default:
                    showError("Nieznany typ raportu");
                    return;
            }

            if (generatedFilePath != null) {
                // Zapisz informację o raporcie w bazie danych
                reportService.saveReportRecord(
                    reportType,
                    reportType.toLowerCase().replace(" ", "_"),
                    "Zakres dat: " + startDate + " do " + endDate,
                    currentUser.getId(),
                    generatedFilePath
                );

                resultTextArea.setText("Raport wygenerowany pomyślnie: " + generatedFilePath);

                // Pokaż przyciski do pobrania i podglądu
                downloadButton.setVisible(true);
                previewButton.setVisible(true);
                downloadButton.setDisable(false);
                previewButton.setDisable(false);
            } else {
                resultTextArea.setText("Wystąpił błąd podczas generowania raportu.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultTextArea.setText("Błąd: " + e.getMessage());
        }
    }

    /**
     * Umożliwia pobranie wygenerowanego raportu
     */
    private void downloadReport() {
        if (generatedFilePath == null) {
            showError("Najpierw wygeneruj raport.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz raport");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Pliki PDF", "*.pdf")
        );

        File sourceFile = new File(generatedFilePath);
        fileChooser.setInitialFileName(sourceFile.getName());

        File targetFile = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());
        if (targetFile != null) {
            try {
                Files.copy(Paths.get(generatedFilePath), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                resultTextArea.setText("Raport zapisany pomyślnie w: " + targetFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Błąd podczas zapisywania pliku: " + e.getMessage());
            }
        }
    }

    /**
     * Otwiera wygenerowany raport w domyślnej przeglądarce PDF
     */
    private void previewReport() {
        if (generatedFilePath == null) {
            showError("Najpierw wygeneruj raport.");
            return;
        }

        try {
            File file = new File(generatedFilePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                showError("Plik raportu nie istnieje.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Błąd podczas otwierania pliku: " + e.getMessage());
        }
    }

    /**
     * Wyświetla komunikat błędu
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}