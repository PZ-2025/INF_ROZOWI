package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.util.List;

public class TasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> taskIdColumn;
    @FXML
    private TableColumn<Task, String> taskNameColumn;
    @FXML
    private TableColumn<Task, String> taskDescriptionColumn;
    @FXML
    private TableColumn<Task, String> taskStatusColumn;
    @FXML
    private TableColumn<Task, String> taskStartDateColumn;
    @FXML
    private TableColumn<Task, String> taskDeadlineColumn;
    @FXML
    private TableColumn<Task, String> taskTeamColumn;
    @FXML
    private TableColumn<Task, String> taskAssignedToColumn;
    @FXML
    private TextField filterField;

    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> allTasks;

    @FXML
    private void initialize() {
        // Ustawienie cell value factory dla kolumn
        taskIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        taskDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        taskStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        taskStartDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        taskDeadlineColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
        taskTeamColumn.setCellValueFactory(cellData -> cellData.getValue().teamProperty());
        taskAssignedToColumn.setCellValueFactory(cellData -> cellData.getValue().assignedToProperty().asString());

        // Ustawienie polityki automatycznego dopasowywania kolumn
        tasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadTasks();
    }

    private void loadTasks() {
        // Pobierz zadania kolegów – te, które należą do tego samego zespołu, ale nie są przypisane do aktualnego użytkownika
        List<Task> tasks = taskDAO.getColleagueTasks(Session.currentUserId, Session.currentUserTeam);
        allTasks = FXCollections.observableArrayList(tasks);
        tasksTable.setItems(allTasks);
    }

    @FXML
    private void handleFilter() {
        String filterText = filterField.getText();
        if (filterText == null || filterText.isEmpty()) {
            tasksTable.setItems(allTasks);
            return;
        }
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        String lower = filterText.toLowerCase();
        for (Task task : allTasks) {
            if (task.getName().toLowerCase().contains(lower) ||
                    task.getDescription().toLowerCase().contains(lower) ||
                    task.getStatus().toLowerCase().contains(lower) ||
                    task.getStartDate().toLowerCase().contains(lower) ||
                    task.getEndDate().toLowerCase().contains(lower) ||
                    task.getTeam().toLowerCase().contains(lower)) {
                filtered.add(task);
            }
        }
        tasksTable.setItems(filtered);
    }

    @FXML
    private void handleAddTask() {
        System.out.println("Dodawanie nowego zadania...");
        // Tutaj możesz otworzyć okno dialogowe lub nowy widok dodawania zadania.
    }
}
