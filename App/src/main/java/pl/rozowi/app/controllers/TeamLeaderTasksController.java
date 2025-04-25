package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TeamLeaderTasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colId;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colDescription;
    @FXML
    private TableColumn<Task, String> colStatus;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colStart;
    @FXML
    private TableColumn<Task, String> colEnd;
    @FXML
    private TableColumn<Task, String> colTeam;
    @FXML
    private TableColumn<Task, String> colAssignedTo;
    @FXML
    private TextField filterField;

    private final TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());
        colTeam.setCellValueFactory(c -> c.getValue().teamNameProperty());
        colAssignedTo.setCellValueFactory(c -> c.getValue().assignedEmailProperty());

        tasksTable.setItems(allTasks);
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

        filterField.textProperty().addListener((obs, o, n) -> applyFilter(n));
        loadTasks();
    }

    @FXML
    private void loadTasks() {
        List<Task> list = taskDAO.getTasksForLeader(Session.currentUserId);
        allTasks.setAll(list);
    }

    private void applyFilter(String text) {
        if (text == null || text.isBlank()) {
            tasksTable.setItems(allTasks);
            return;
        }
        String lower = text.toLowerCase();
        ObservableList<Task> filtered = allTasks.filtered(t ->
                t.getTitle().toLowerCase().contains(lower) ||
                        t.getStatus().toLowerCase().contains(lower) ||
                        t.getAssignedEmail().toLowerCase().contains(lower)
        );
        tasksTable.setItems(filtered);
    }

    private void openDetails(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teamleader/taskDetails.fxml"));
            Parent root = loader.load();
            TaskDetailsController ctrl = loader.getController();
            ctrl.setTask(task);
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setTitle("Szczegóły zadania");
            st.showAndWait();
            loadTasks();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teamleader/taskCreate.fxml"));
            Parent root = loader.load();
            TaskCreateController ctrl = loader.getController();

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setTitle("Nowe zadanie");
            st.showAndWait();

            loadTasks();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
