package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.rozowi.app.dao.TaskActivityDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.models.User;
import pl.rozowi.app.models.Task;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivitiesController {

    @FXML
    private TableView<TaskActivity> activitiesTable;
    @FXML
    private TableColumn<TaskActivity, String> colTimestamp;
    @FXML
    private TableColumn<TaskActivity, String> colUser;
    @FXML
    private TableColumn<TaskActivity, String> colTaskTitle;
    @FXML
    private TableColumn<TaskActivity, String> colActivityType;
    @FXML
    private TableColumn<TaskActivity, String> colDescription;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> activityTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

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

    private final TaskActivityDAO activityDAO = new TaskActivityDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    private ObservableList<TaskActivity> allActivities = FXCollections.observableArrayList();
    private ObservableList<TaskActivity> filteredActivities = FXCollections.observableArrayList();

    // Caches for faster lookups
    private Map<Integer, String> userNameCache = new HashMap<>();
    private Map<Integer, String> taskTitleCache = new HashMap<>();

    @FXML
    private void initialize() {
        // Set up table columns
        colTimestamp.setCellValueFactory(data -> {
            Timestamp timestamp = data.getValue().getActivityDate();
            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return new SimpleStringProperty(sdf.format(timestamp));
            }
            return new SimpleStringProperty("");
        });

        colUser.setCellValueFactory(data -> {
            int userId = data.getValue().getUserId();
            String userName = getUserName(userId);
            return new SimpleStringProperty(userName);
        });

        colTaskTitle.setCellValueFactory(data -> {
            int taskId = data.getValue().getTaskId();
            String taskTitle = getTaskTitle(taskId);
            return new SimpleStringProperty(taskTitle);
        });

        colActivityType.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getActivityType()));

        colDescription.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDescription()));

        // Set up activity type filter
        activityTypeCombo.getItems().addAll(
            "Wszystkie", "CREATE", "UPDATE", "STATUS", "ASSIGN", "COMMENT");
        activityTypeCombo.setValue("Wszystkie");

        // Set up search and filter listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        activityTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Handle row selection for showing details
        activitiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showActivityDetails(newVal);
            } else {
                clearActivityDetails();
            }
        });

        // Load all activities
        loadActivities();
    }

    private void loadActivities() {
        List<TaskActivity> activities = activityDAO.getAllActivities();
        allActivities.setAll(activities);
        filteredActivities.setAll(activities);
        activitiesTable.setItems(filteredActivities);
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String activityType = activityTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        filteredActivities.clear();

        for (TaskActivity activity : allActivities) {
            boolean matchesSearch = searchText.isEmpty() ||
                                   getUserName(activity.getUserId()).toLowerCase().contains(searchText) ||
                                   getTaskTitle(activity.getTaskId()).toLowerCase().contains(searchText) ||
                                   activity.getDescription().toLowerCase().contains(searchText);

            boolean matchesType = "Wszystkie".equals(activityType) ||
                                 activity.getActivityType().equals(activityType);

            boolean matchesDateRange = isInDateRange(activity.getActivityDate(), startDate, endDate);

            if (matchesSearch && matchesType && matchesDateRange) {
                filteredActivities.add(activity);
            }
        }

        activitiesTable.setItems(filteredActivities);
    }

    private boolean isInDateRange(Timestamp timestamp, LocalDate startDate, LocalDate endDate) {
        if (timestamp == null || (startDate == null && endDate == null)) {
            return true;
        }

        LocalDate activityDate = timestamp.toLocalDateTime().toLocalDate();
        boolean afterStartDate = startDate == null || !activityDate.isBefore(startDate);
        boolean beforeEndDate = endDate == null || !activityDate.isAfter(endDate);

        return afterStartDate && beforeEndDate;
    }

    private void showActivityDetails(TaskActivity activity) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        detailTimestamp.setText(activity.getActivityDate() != null ?
                              sdf.format(activity.getActivityDate()) : "-");
        detailUser.setText(getUserName(activity.getUserId()));
        detailTask.setText(getTaskTitle(activity.getTaskId()));
        detailType.setText(activity.getActivityType());
        detailDescription.setText(activity.getDescription());
    }

    private void clearActivityDetails() {
        detailTimestamp.setText("-");
        detailUser.setText("-");
        detailTask.setText("-");
        detailType.setText("-");
        detailDescription.setText("");
    }

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

    private String getTaskTitle(int taskId) {
        if (taskTitleCache.containsKey(taskId)) {
            return taskTitleCache.get(taskId);
        }

        try {
            // We'd ideally have a method to get a single task by ID
            // but we can work with what we have
            List<Task> tasks = taskDAO.getTasksByProjectId(-1); // This won't work properly, but it's a placeholder
            for (Task task : tasks) {
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

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        activityTypeCombo.setValue("Wszystkie");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);

        filteredActivities.setAll(allActivities);
        activitiesTable.setItems(filteredActivities);
    }

    @FXML
    private void handleRefresh() {
        loadActivities();
        clearActivityDetails();
    }
}