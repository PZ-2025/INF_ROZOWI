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

/**
 * Kontroler odpowiedzialny za zarządzanie logami systemowymi w panelu administratora.
 * Udostępnia funkcjonalności przeglądania, filtrowania i eksportowania logów.
 */
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

    /**
     * Metoda inicjalizująca kontroler. Konfiguruje tabelę logów,
     * ustawia wartości domyślne i ładuje przykładowe dane.
     */
    @FXML
    private void initialize() {
        colTimestamp.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));
        colLevel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLevel()));
        colSource.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSource()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser()));
        colMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));

        colLevel.setCellFactory(column -> new TableCell<LogEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

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

        logLevelCombo.getItems().addAll("Wszystkie", "ERROR", "WARNING", "INFO", "DEBUG");
        logLevelCombo.setValue("Wszystkie");

        logsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayLogDetails(newSelection);
            } else {
                clearLogDetails();
            }
        });

        loadSampleLogs();
    }

    /**
     * Ładuje przykładowe logi do tabeli (w wersji produkcyjnej należy zastąpić
     * prawdziwym źródłem danych).
     */
    private void loadSampleLogs() {
        List<LogEntry> logs = generateSampleLogs(100);
        allLogs.setAll(logs);
        filteredLogs.setAll(logs);
        logsTable.setItems(filteredLogs);
    }

    /**
     * Generuje przykładowe wpisy logów dla celów demonstracyjnych.
     *
     * @param count liczba logów do wygenerowania
     * @return lista wygenerowanych wpisów logów
     */
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

            LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(7)).minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60));
            String timestamp = dateTime.format(formatter);

            String ip = "192.168.1." + (random.nextInt(254) + 1);
            String session = "SESSION_" + random.nextInt(10000);
            String stackTrace = level.equals("ERROR") ? generateStackTrace() : "";

            LogEntry logEntry = new LogEntry(timestamp, level, source, user, message, ip, session, stackTrace);
            logs.add(logEntry);
        }

        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return logs;
    }

    /**
     * Generuje przykładowy stack trace dla błędów.
     *
     * @return wygenerowany stack trace
     */
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

    /**
     * Wyświetla szczegóły wybranego logu w panelu detali.
     *
     * @param logEntry wybrany wpis logu
     */
    private void displayLogDetails(LogEntry logEntry) {
        detailTimestamp.setText(logEntry.getTimestamp());
        detailLevel.setText(logEntry.getLevel());
        detailSource.setText(logEntry.getSource());
        detailUser.setText(logEntry.getUser());
        detailIp.setText(logEntry.getIp());
        detailSession.setText(logEntry.getSession());
        detailMessage.setText(logEntry.getMessage());
        detailStackTrace.setText(logEntry.getStackTrace());

        switch (logEntry.getLevel()) {
            case "ERROR" -> detailLevel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
            case "WARNING" -> detailLevel.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
            case "INFO" -> detailLevel.setStyle("-fx-text-fill: #17a2b8;");
            case "DEBUG" -> detailLevel.setStyle("-fx-text-fill: #6c757d;");
            default -> detailLevel.setStyle("");
        }
    }

    /**
     * Czyści panel szczegółów logu.
     */
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

    /**
     * Wyszukuje logi na podstawie wprowadzonych kryteriów.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty() && logLevelCombo.getValue().equals("Wszystkie") &&
            startDatePicker.getValue() == null && endDatePicker.getValue() == null) {
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

    /**
     * Sprawdza czy data logu mieści się w wybranym zakresie.
     *
     * @param timestamp znacznik czasu logu
     * @param startDate data początkowa zakresu
     * @param endDate data końcowa zakresu
     * @return true jeśli log mieści się w zakresie dat
     */
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
            return true;
        }
    }

    /**
     * Filtruje logi na podstawie wybranego poziomu (ERROR, WARNING itp.).
     */
    @FXML
    private void handleFilterByLevel() {
        handleSearch();
    }

    /**
     * Filtruje logi na podstawie wybranego zakresu dat.
     */
    @FXML
    private void handleDateFilter() {
        handleSearch();
    }

    /**
     * Czyści wszystkie ustawione filtry.
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        logLevelCombo.setValue("Wszystkie");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);

        filteredLogs.setAll(allLogs);
        logsTable.setItems(filteredLogs);
    }

    /**
     * Odświeża listę logów.
     */
    @FXML
    private void handleRefresh() {
        loadSampleLogs();
        clearLogDetails();
    }

    /**
     * Eksportuje widoczne logi do pliku tekstowego lub CSV.
     */
    @FXML
    private void handleExportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Eksport logów");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"),
            new FileChooser.ExtensionFilter("Pliki CSV", "*.csv")
        );

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        fileChooser.setInitialFileName("logs_export_" + now.format(formatter) + ".txt");

        Stage stage = (Stage) logsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Timestamp|Level|Source|User|Message|IP|Session|StackTrace\n");

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

    /**
     * Wyświetla okno dialogowe z informacją.
     *
     * @param message treść wiadomości
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla okno dialogowe z błędem.
     *
     * @param title tytuł okna
     * @param message treść błędu
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Klasa wewnętrzna reprezentująca pojedynczy wpis logu.
     */
    public static class LogEntry {
        private final String timestamp;
        private final String level;
        private final String source;
        private final String user;
        private final String message;
        private final String ip;
        private final String session;
        private final String stackTrace;

        /**
         * Tworzy nowy wpis logu.
         *
         * @param timestamp znacznik czasu
         * @param level poziom logu (ERROR, WARNING itp.)
         * @param source źródło logu
         * @param user użytkownik związany z logiem
         * @param message treść wiadomości
         * @param ip adres IP związany z logiem
         * @param session identyfikator sesji
         * @param stackTrace stack trace dla błędów
         */
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