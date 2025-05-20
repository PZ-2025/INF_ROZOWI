package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.util.List;

/**
 * Kontroler odpowiedzialny za zarządzanie widokiem zadań przypisanych do aktualnie zalogowanego użytkownika.
 * Umożliwia przeglądanie, wyszukiwanie i zarządzanie zadaniami użytkownika.
 */
public class MyTasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colId;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colStatus;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colEndDate;
    @FXML
    private TableColumn<Task, String> colTeam;

    @FXML
    private TextField searchField;

    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private ObservableList<Task> filteredTasks = FXCollections.observableArrayList();

    /**
     * Inicjalizuje kontroler, konfigurując tabelę zadań i ładując dane.
     */
    @FXML
    private void initialize() {
        setupTableColumns();

        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<Task>() {
                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);
                    if (task == null || empty) {
                        setStyle("");
                    } else if ("Zakończone".equals(task.getStatus())) {
                        setStyle("");
                    } else if ("W toku".equals(task.getStatus())) {
                        setStyle("");
                    } else {
                        setStyle("");
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openTaskDetails(row.getItem());
                }
            });

            return row;
        });

        loadTasks();
    }

    /**
     * Konfiguruje kolumny tabeli zadań.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(tasksTable.getItems().indexOf(data.getValue()) + 1));
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colTeam.setCellValueFactory(data -> data.getValue().teamNameProperty());
    }

    /**
     * Otwiera okno ze szczegółami wybranego zadania.
     * @param task Zadanie do wyświetlenia
     */
    private void openTaskDetails(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/taskDetails.fxml"));
            Parent root = loader.load();

            TaskDetailsController controller = loader.getController();
            controller.setTask(task);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Szczegóły zadania");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadTasks();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Błąd", "Nie można otworzyć szczegółów zadania: " + e.getMessage());
        }
    }

    /**
     * Ładuje zadania przypisane do aktualnego użytkownika.
     */
    private void loadTasks() {
        List<Task> tasks = taskDAO.getTasksForUser(Session.currentUserId);
        allTasks.setAll(tasks);
        filteredTasks.setAll(tasks);
        tasksTable.setItems(filteredTasks);
    }

    /**
     * Filtruje zadania na podstawie wprowadzonego tekstu w polu wyszukiwania.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredTasks.setAll(allTasks);
        } else {
            filteredTasks.clear();
            for (Task task : allTasks) {
                if (task.getTitle().toLowerCase().contains(searchText) ||
                        task.getStatus().toLowerCase().contains(searchText) ||
                        task.getPriority().toLowerCase().contains(searchText) ||
                        (task.getTeamName() != null && task.getTeamName().toLowerCase().contains(searchText))) {

                    filteredTasks.add(task);
                }
            }
        }

        tasksTable.setItems(filteredTasks);
    }

    /**
     * Odświeża listę zadań, resetując filtr wyszukiwania.
     */
    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadTasks();
    }

    /**
     * Wyświetla okno dialogowe z komunikatem.
     * @param type Typ komunikatu (INFORMATION, WARNING, ERROR)
     * @param title Tytuł okna dialogowego
     * @param message Treść komunikatu
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}