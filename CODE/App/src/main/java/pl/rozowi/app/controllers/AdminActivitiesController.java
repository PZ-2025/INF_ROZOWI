package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.dao.TaskActivityDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.models.EnhancedTaskActivity;
import pl.rozowi.app.models.User;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kontroler odpowiedzialny za zarządzanie i wyświetlanie aktywności w systemie
 * z perspektywy administratora. Zawiera funkcjonalności przeglądania, filtrowania
 * i eksportowania historii aktywności użytkowników.
 */
public class AdminActivitiesController {

    @FXML
    private TableView<ActivityEntry> activitiesTable;
    @FXML
    private TableColumn<ActivityEntry, String> colTimestamp;
    @FXML
    private TableColumn<ActivityEntry, String> colUser;
    @FXML
    private TableColumn<ActivityEntry, String> colTaskTitle;
    @FXML
    private TableColumn<ActivityEntry, String> colDescription;
    @FXML
    private TableColumn<ActivityEntry, String> colId;

    @FXML
    private TextField searchField;

    @FXML
    private Label detailTimestamp;
    @FXML
    private Label detailUser;
    @FXML
    private Label detailTask;
    @FXML
    private Label detailType;
    @FXML
    private TextArea detailDescription;

    private final TaskActivityDAO taskActivityDAO = new TaskActivityDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    private ObservableList<ActivityEntry> allActivities = FXCollections.observableArrayList();
    private FilteredList<ActivityEntry> filteredActivities;

    private Map<Integer, String> userNameCache = new HashMap<>();
    private Map<Integer, String> taskTitleCache = new HashMap<>();

    /**
     * Metoda inicjalizująca kontroler. Konfiguruje tabelę aktywności,
     * ładuje dane i ustawia filtry.
     */
    @FXML
    private void initialize() {
        colTimestamp.setCellValueFactory(data -> data.getValue().timestampProperty());
        colUser.setCellValueFactory(data -> data.getValue().userProperty());
        colTaskTitle.setCellValueFactory(data -> data.getValue().taskProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());

        colId.setCellValueFactory(cellData -> {
            int index = activitiesTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });

        loadActivities();

        filteredActivities = new FilteredList<>(allActivities, p -> true);
        activitiesTable.setItems(filteredActivities);

        activitiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayActivityDetails(newSelection);
            } else {
                clearActivityDetails();
            }
        });

        setupFilters();
    }

    /**
     * Ładuje wszystkie aktywności z bazy danych i przygotowuje je do wyświetlenia.
     */
    private void loadActivities() {
        try {
            List<TaskActivity> activities = taskActivityDAO.getAllActivities();

            List<EnhancedTaskActivity> enhancedActivities = new ArrayList<>();
            for (TaskActivity activity : activities) {
                EnhancedTaskActivity enhanced = new EnhancedTaskActivity(activity);

                User user = userDAO.getUserById(activity.getUserId());
                if (user != null) {
                    enhanced.setUserName(user.getName());
                    enhanced.setUserLastName(user.getLastName());
                    enhanced.setUserEmail(user.getEmail());
                }

                if (activity.getTaskId() > 0) {
                    String title = getTaskTitle(activity.getTaskId());
                    enhanced.setTaskTitle(title);
                }

                enhancedActivities.add(enhanced);
            }

            for (EnhancedTaskActivity enhanced : enhancedActivities) {
                ActivityEntry entry = new ActivityEntry(
                        enhanced.getId(),
                        enhanced.getTaskId(),
                        enhanced.getTaskTitleOrDefault(),
                        enhanced.getUserId(),
                        enhanced.getUserDisplayString(),
                        enhanced.getActivityType(),
                        enhanced.getDescription(),
                        enhanced.getActivityDate()
                );
                allActivities.add(entry);
            }

            if (allActivities.isEmpty()) {
                activitiesTable.setPlaceholder(new Label("Brak aktywności w systemie"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            activitiesTable.setPlaceholder(new Label("Błąd podczas ładowania aktywności: " + e.getMessage()));
        }
    }

    /**
     * Pobiera pełną nazwę użytkownika na podstawie ID z cache lub bazy danych.
     *
     * @param userId ID użytkownika
     * @return pełna nazwa użytkownika w formacie "Imię Nazwisko (email)"
     */
    private String getUserName(int userId) {
        if (userNameCache.containsKey(userId)) {
            return userNameCache.get(userId);
        }

        try {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                String fullName = user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")";
                userNameCache.put(userId, fullName);
                return fullName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Użytkownik ID: " + userId;
    }

    /**
     * Pobiera tytuł zadania na podstawie ID z cache lub bazy danych.
     *
     * @param taskId ID zadania
     * @return tytuł zadania lub "Zadanie ID: X" jeśli nie znaleziono
     */
    private String getTaskTitle(int taskId) {
        if (taskTitleCache.containsKey(taskId)) {
            return taskTitleCache.get(taskId);
        }

        try {
            List<pl.rozowi.app.models.Task> tasks = taskDAO.getTasksByProjectId(-1);
            for (pl.rozowi.app.models.Task task : tasks) {
                if (task.getId() == taskId) {
                    String title = task.getTitle();
                    taskTitleCache.put(taskId, title);
                    return title;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Zadanie ID: " + taskId;
    }

    /**
     * Konfiguruje filtry dla listy aktywności.
     */
    private void setupFilters() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Stosuje aktualne filtry do listy aktywności.
     */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        filteredActivities.setPredicate(activity -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    activity.getUser().toLowerCase().contains(searchText) ||
                    activity.getTask().toLowerCase().contains(searchText) ||
                    activity.getDescription().toLowerCase().contains(searchText) ||
                    activity.getType().toLowerCase().contains(searchText);

            return matchesSearch;
        });
    }

    /**
     * Konwertuje typ aktywności z angielskiego na polski.
     *
     * @param englishType typ aktywności w języku angielskim
     * @return przetłumaczony typ aktywności
     */
    private String mapTypeToPolish(String englishType) {
        return switch (englishType.toUpperCase()) {
            case "CREATE" -> "UTWORZENIE";
            case "UPDATE" -> "AKTUALIZACJA";
            case "STATUS" -> "ZMIANA STATUSU";
            case "ASSIGN" -> "PRZYPISANIE";
            case "COMMENT" -> "KOMENTARZ";
            case "PASSWORD" -> "ZMIANA HASŁA";
            case "LOGIN" -> "LOGOWANIE";
            case "LOGOUT" -> "WYLOGOWANIE";
            case "USER_MANAGEMENT" -> "ZARZĄDZANIE UŻYTKOWNIKAMI";
            case "TEAM_MANAGEMENT" -> "ZARZĄDZANIE ZESPOŁAMI";
            default -> englishType;
        };
    }

    /**
     * Konwertuje typ aktywności z polskiego na angielski.
     *
     * @param polishType typ aktywności w języku polskim
     * @return przetłumaczony typ aktywności
     */
    private String mapTypeToEnglish(String polishType) {
        return switch (polishType.toUpperCase()) {
            case "UTWORZENIE" -> "CREATE";
            case "AKTUALIZACJA" -> "UPDATE";
            case "ZMIANA STATUSU" -> "STATUS";
            case "PRZYPISANIE" -> "ASSIGN";
            case "KOMENTARZ" -> "COMMENT";
            case "ZMIANA HASŁA" -> "PASSWORD";
            case "LOGOWANIE" -> "LOGIN";
            case "WYLOGOWANIE" -> "LOGOUT";
            case "ZARZĄDZANIE UŻYTKOWNIKAMI" -> "USER_MANAGEMENT";
            case "ZARZĄDZANIE ZESPOŁAMI" -> "TEAM_MANAGEMENT";
            default -> polishType;
        };
    }

    /**
     * Wyświetla szczegóły wybranej aktywności w panelu detali.
     *
     * @param activity wybrana aktywność do wyświetlenia
     */
    private void displayActivityDetails(ActivityEntry activity) {
        detailTimestamp.setText(activity.getTimestamp());
        detailUser.setText(activity.getUser());
        detailTask.setText(activity.getTask());
        if (detailType != null) {
            detailType.setText(activity.getType());

            switch (activity.getType().toUpperCase()) {
                case "CREATE", "UTWORZENIE" -> detailType.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                case "UPDATE", "AKTUALIZACJA" -> detailType.setStyle("-fx-text-fill: #17a2b8; -fx-font-weight: bold;");
                case "STATUS", "ZMIANA STATUSU" -> detailType.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                case "ASSIGN", "PRZYPISANIE" -> detailType.setStyle("-fx-text-fill: #6f42c1; -fx-font-weight: bold;");
                case "COMMENT", "KOMENTARZ" -> detailType.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
                case "PASSWORD", "ZMIANA HASŁA" -> detailType.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                case "LOGIN" -> detailType.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                case "LOGOUT" -> detailType.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
                case "USER_MANAGEMENT" -> detailType.setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
                case "TEAM_MANAGEMENT" -> detailType.setStyle("-fx-text-fill: #20c997; -fx-font-weight: bold;");
                default -> detailType.setStyle("-fx-font-weight: bold;");
            }
        }
        detailDescription.setText(activity.getDescription());
    }

    /**
     * Czyści panel szczegółów aktywności.
     */
    private void clearActivityDetails() {
        detailTimestamp.setText("-");
        detailUser.setText("-");
        detailTask.setText("-");
        if (detailType != null) {
            detailType.setText("-");
            detailType.setStyle("");
        }
        detailDescription.setText("");
    }

    /**
     * Obsługuje akcję wyszukiwania aktywności.
     */
    @FXML
    private void handleSearch() {
        applyFilters();
    }

    /**
     * Obsługuje akcję czyszczenia filtrów.
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filteredActivities.setPredicate(p -> true);
    }

    /**
     * Obsługuje akcję odświeżania listy aktywności.
     */
    @FXML
    private void handleRefresh() {
        allActivities.clear();
        userNameCache.clear();
        taskTitleCache.clear();
        loadActivities();
        filteredActivities.setPredicate(p -> true);
        clearActivityDetails();
    }

    /**
     * Obsługuje akcję eksportu aktywności do pliku.
     */
    @FXML
    private void handleExportActivities() {
        if (filteredActivities == null || filteredActivities.isEmpty()) {
            showWarning("Brak aktywności do eksportu");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Eksport aktywności");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"),
                new javafx.stage.FileChooser.ExtensionFilter("Pliki CSV", "*.csv")
        );

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        fileChooser.setInitialFileName("aktywnosci_export_" + now.format(formatter) + ".csv");

        javafx.stage.Stage stage = (javafx.stage.Stage) activitiesTable.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write("Data i czas|Użytkownik|Zadanie|Typ aktywności|Opis\n");

                for (ActivityEntry activity : filteredActivities) {
                    writer.write(String.format("%s|%s|%s|%s|%s\n",
                            activity.getTimestamp(),
                            activity.getUser(),
                            activity.getTask(),
                            activity.getType(),
                            activity.getDescription().replace("\n", " ").replace("|", ",")
                    ));
                }

                showInfo("Eksport zakończony", "Dane zostały zapisane do pliku:\n" + file.getAbsolutePath());
            } catch (java.io.IOException e) {
                showError("Błąd eksportu", "Nie udało się wyeksportować danych: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Wyświetla okno dialogowe z informacją.
     *
     * @param title tytuł okna dialogowego
     * @param message treść wiadomości
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
     *
     * @param message treść ostrzeżenia
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
     *
     * @param title tytuł okna dialogowego
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
     * Klasa wewnętrzna reprezentująca pojedynczy wpis aktywności w tabeli.
     */
    public static class ActivityEntry {
        private final int id;
        private final int taskId;
        private final String task;
        private final int userId;
        private final String user;
        private final String type;
        private final String description;
        private final String timestamp;

        /**
         * Tworzy nowy wpis aktywności.
         *
         * @param id ID aktywności
         * @param taskId ID zadania
         * @param taskTitle tytuł zadania
         * @param userId ID użytkownika
         * @param userName nazwa użytkownika
         * @param activityType typ aktywności
         * @param description opis aktywności
         * @param timestamp data i czas aktywności
         */
        public ActivityEntry(int id, int taskId, String taskTitle, int userId, String userName,
                             String activityType, String description, Timestamp timestamp) {
            this.id = id;
            this.taskId = taskId;
            this.task = (taskTitle != null && !taskTitle.isEmpty()) ? taskTitle : "Zadanie ID: " + taskId;
            this.userId = userId;
            this.user = userName;
            this.type = activityType != null ? activityType : "INNE";
            this.description = description != null ? description : "";

            if (timestamp != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.timestamp = dateFormat.format(timestamp);
            } else {
                this.timestamp = "";
            }
        }

        public int getId() { return id; }
        public int getTaskId() { return taskId; }
        public String getTask() { return task; }
        public int getUserId() { return userId; }
        public String getUser() { return user; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getTimestamp() { return timestamp; }

        public SimpleStringProperty taskProperty() { return new SimpleStringProperty(task); }
        public SimpleStringProperty userProperty() { return new SimpleStringProperty(user); }
        public SimpleStringProperty typeProperty() { return new SimpleStringProperty(type); }
        public SimpleStringProperty descriptionProperty() { return new SimpleStringProperty(description); }
        public SimpleStringProperty timestampProperty() { return new SimpleStringProperty(timestamp); }
    }
}