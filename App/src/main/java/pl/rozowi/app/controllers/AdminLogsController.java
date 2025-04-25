package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class AdminLogsController {

    @FXML
    private TableView<LogEntry> logsTable;
    @FXML
    private TableColumn<LogEntry, String> colTimestamp;
    @FXML
    private TableColumn<LogEntry, String> colLevel;
    @FXML
    private TableColumn<LogEntry, String> colSource;
    @FXML
    private TableColumn<LogEntry, String> colUser;
    @FXML
    private TableColumn<LogEntry, String> colMessage;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> logLevelCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label detailTimestamp;
    @FXML
    private Label detailLevel;
    @FXML
    private Label detailSource;
    @FXML
    private Label detailUser;
    @FXML
    private Label detailIp;
    @FXML
    private Label detailSession;
    @FXML
    private TextArea detailMessage;
    @FXML
    private TextArea detailStackTrace;

    private ObservableList<LogEntry> allLogs = FXCollections.observableArrayList();
    private ObservableList<LogEntry> filteredLogs = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Konfiguracja kolumn tabeli
        colTimestamp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));
        colLevel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
        colSource.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSource()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser()));
        colMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));

        // Zaawansowane formatowanie komórki poziomu
        colLevel.setCellFactory(column -> new TableCell<LogEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    // Kolorowanie w zależności od poziomu
                    switch (item) {
                        case "ERROR" -> setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                        case "WARNING" -> setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                        case "INFO" -> setStyle("-fx-text-fill: #17a2b8;");
                        case "DEBUG" -> setStyle("-fx-text-fill: #6c757d;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Ustaw poziomy logów w filtrze
        logLevelCombo.getItems().addAll("Wszystkie", "ERROR", "WARNING", "INFO", "DEBUG");
        logLevelCombo.setValue("Wszystkie");

        // Obsługa wyboru logu w tabeli
        logsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayLogDetails(newSelection);
            } else {
                clearLogDetails();
            }
        });

        // Wczytaj przykładowe logi
        loadSampleLogs();
    }

    private void loadSampleLogs() {
        // W rzeczywistej implementacji, pobierz logi z bazy danych lub pliku
        List<LogEntry> logs = generateSampleLogs(100);
        allLogs.setAll(logs);
        filteredLogs.setAll(logs);
        logsTable.setItems(filteredLogs);
    }

    private List<LogEntry> generateSampleLogs(int count) {
        List<LogEntry> logs = new ArrayList<>();
        String[] levels = {"ERROR", "WARNING", "INFO", "DEBUG"};
        String[] sources = {"Authentication", "Database", "FileSystem", "Network", "Security", "UI", "TaskManagement"};
        String[] users = {"admin", "manager1", "teamleader1", "user1", "user2", "system"};
        String[] messages = {
            "Nieudane logowanie: nieprawidłowe hasło",
            "Błąd połączenia z bazą danych",
            "Nieudana operacja zapisu pliku",
            "Użytkownik zmienił uprawnienia",
            "Utworzono nowe zadanie",
            "Zaktualizowano status zadania",
            "Usunięto użytkownika",
            "Sesja wygasła",
            "Próba dostępu do zabronionego zasobu",
            "Modyfikacja ustawień systemowych",
            "Błąd podczas generowania raportu",
            "Wykonano kopię zapasową bazy danych",
            "Błąd komunikacji z serwerem"
        };

        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < count; i++) {
            String level = levels[random.nextInt(levels.length)];
            String source = sources[random.nextInt(sources.length)];
            String user = users[random.nextInt(users.length)];
            String message = messages[random.nextInt(messages.length)];

            // Generuj losową datę z ostatniego tygodnia
            LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(7)).minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60));
            String timestamp = dateTime.format(formatter);

            // Tworzenie szczegółów dla szczegółowego widoku
            String ip = "192.168.1." + (random.nextInt(254) + 1);
            String session = "SESSION_" + random.nextInt(10000);
            String stackTrace = level.equals("ERROR") ? generateStackTrace() : "";

            LogEntry logEntry = new LogEntry(timestamp, level, source, user, message, ip, session, stackTrace);
            logs.add(logEntry);
        }

        // Sortuj od najnowszych do najstarszych
        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return logs;
    }

    private String generateStackTrace() {
        Random random = new Random();
        String[] exceptions = {
            "java.sql.SQLException", "java.io.IOException",
            "java.lang.NullPointerException", "java.lang.IllegalArgumentException",
            "javax.naming.NameNotFoundException", "java.net.ConnectException"
        };

        String[] classes = {
            "pl.rozowi.app.dao.UserDAO", "pl.rozowi.app.controllers.LoginController",
            "pl.rozowi.app.database.DatabaseManager", "pl.rozowi.app.services.AuthService",
            "pl.rozowi.app.models.Task", "pl.rozowi.app.util.FileHandler"
        };

        StringBuilder sb = new StringBuilder();
        String exception = exceptions[random.nextInt(exceptions.length)];
        sb.append(exception).append(": ").append("Error message details\n");

        int lines = 3 + random.nextInt(5);
        for (int i = 0; i < lines; i++) {
            String className = classes[random.nextInt(classes.length)];
            String methodName = "method" + (char)('A' + random.nextInt(26)) + (random.nextInt(10));
            int lineNumber = 100 + random.nextInt(900);

            sb.append("\tat ").append(className).append(".").append(methodName)
              .append("(").append(className.substring(className.lastIndexOf('.') + 1))
              .append(".java:").append(lineNumber).append(")\n");
        }

        return sb.toString();
    }

    private void displayLogDetails(LogEntry logEntry) {
        detailTimestamp.setText(logEntry.getTimestamp());
        detailLevel.setText(logEntry.getLevel());
        detailSource.setText(logEntry.getSource());
        detailUser.setText(logEntry.getUser());
        detailIp.setText(logEntry.getIp());
        detailSession.setText(logEntry.getSession());
        detailMessage.setText(logEntry.getMessage());
        detailStackTrace.setText(logEntry.getStackTrace());

        // Kolorowanie etykiety poziomu
        switch (logEntry.getLevel()) {
            case "ERROR" -> detailLevel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            case "WARNING" -> detailLevel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
            case "INFO" -> detailLevel.setStyle("-fx-text-fill: #17a2b8;");
            case "DEBUG" -> detailLevel.setStyle("-fx-text-fill: #6c757d;");
            default -> detailLevel.setStyle("");
        }
    }

    private void clearLogDetails() {
        detailTimestamp.setText("-");
        detailLevel.setText("-");
        detailSource.setText("-");
        detailUser.setText("-");
        detailIp.setText("-");
        detailSession.setText("-");
        detailMessage.setText("");
        detailStackTrace.setText("");
        detailLevel.setStyle("");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty() && logLevelCombo.getValue().equals("Wszystkie") &&
            startDatePicker.getValue() == null && endDatePicker.getValue() == null) {
            // Brak filtrów, pokaż wszystkie logi
            filteredLogs.setAll(allLogs);
            logsTable.setItems(filteredLogs);
            return;
        }

        ObservableList<LogEntry> searchResults = FXCollections.observableArrayList();

        for (LogEntry log : allLogs) {
            boolean matchesSearch = searchText.isEmpty() ||
                                    log.getTimestamp().toLowerCase().contains(searchText) ||
                                    log.getLevel().toLowerCase().contains(searchText) ||
                                    log.getSource().toLowerCase().contains(searchText) ||
                                    log.getUser().toLowerCase().contains(searchText) ||
                                    log.getMessage().toLowerCase().contains(searchText);

            boolean matchesLevel = logLevelCombo.getValue().equals("Wszystkie") ||
                                  log.getLevel().equals(logLevelCombo.getValue());

            boolean matchesDateRange = isInDateRange(log.getTimestamp(), startDatePicker.getValue(), endDatePicker.getValue());

            if (matchesSearch && matchesLevel && matchesDateRange) {
                searchResults.add(log);
            }
        }

        filteredLogs.setAll(searchResults);
        logsTable.setItems(filteredLogs);
    }

    private boolean isInDateRange(String timestamp, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }

        try {
            LocalDate logDate = LocalDate.parse(timestamp.substring(0, 10));

            boolean afterStartDate = startDate == null || !logDate.isBefore(startDate);
            boolean beforeEndDate = endDate == null || !logDate.isAfter(endDate);

            return afterStartDate && beforeEndDate;
        } catch (Exception e) {
            return true; // W razie błędu parsowania, nie filtruj tego logu
        }
    }

    @FXML
    private void handleFilterByLevel() {
        handleSearch(); // Ta sama logika co wyszukiwanie
    }

    @FXML
    private void handleDateFilter() {
        handleSearch(); // Ta sama logika co wyszukiwanie
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        logLevelCombo.setValue("Wszystkie");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);

        filteredLogs.setAll(allLogs);
        logsTable.setItems(filteredLogs);
    }

    @FXML
    private void handleRefresh() {
        loadSampleLogs();
        clearLogDetails();
    }

    @FXML
    private void handleExportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksport logów");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"),
            new FileChooser.ExtensionFilter("Pliki CSV", "*.csv")
        );

        // Ustaw domyślną nazwę pliku
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        fileChooser.setInitialFileName("logs_export_" + now.format(formatter) + ".txt");

        // Pokaż dialog
        Stage stage = (Stage) logsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Nagłówek
                writer.write("Timestamp|Level|Source|User|Message|IP|Session|StackTrace\n");

                // Dane
                for (LogEntry log : filteredLogs) {
                    writer.write(String.format("%s|%s|%s|%s|%s|%s|%s|%s\n",
                            log.getTimestamp(),
                            log.getLevel(),
                            log.getSource(),
                            log.getUser(),
                            log.getMessage(),
                            log.getIp(),
                            log.getSession(),
                            log.getStackTrace().replace("\n", "\\n")
                    ));
                }

                showInfo("Logi zostały wyeksportowane do pliku:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Błąd", "Nie udało się wyeksportować logów: " + e.getMessage());
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
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

    // Klasa wewnętrzna reprezentująca wpis logu
    public static class LogEntry {
        private final String timestamp;
        private final String level;
        private final String source;
        private final String user;
        private final String message;
        private final String ip;
        private final String session;
        private final String stackTrace;

        public LogEntry(String timestamp, String level, String source, String user,
                        String message, String ip, String session, String stackTrace) {
            this.timestamp = timestamp;
            this.level = level;
            this.source = source;
            this.user = user;
            this.message = message;
            this.ip = ip;
            this.session = session;
            this.stackTrace = stackTrace;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getLevel() {
            return level;
        }

        public String getSource() {
            return source;
        }

        public String getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        public String getIp() {
            return ip;
        }

        public String getSession() {
            return session;
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}