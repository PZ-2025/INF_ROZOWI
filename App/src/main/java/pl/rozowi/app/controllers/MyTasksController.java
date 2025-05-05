package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.util.List;

public class MyTasksController {

    @FXML
    private ListView<Task> tasksList;

    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> myTasks;

    @FXML
    private void initialize() {
        loadTasks();

        tasksList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Task selectedTask = tasksList.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    openTaskDetails(selectedTask);
                }
            }
        });
    }

    private void openTaskDetails(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/taskDetails.fxml"));
            Parent root = loader.load();

            TaskDetailsController controller = loader.getController();
            controller.setTask(task);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Szczegóły zadania");
            stage.initModality(Modality.APPLICATION_MODAL); // Okno modalne
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        List<Task> tasks = taskDAO.getTasksForUser(Session.currentUserId);
        myTasks = FXCollections.observableArrayList(tasks);

        tasksList.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                } else {
                    setText("[" + task.getStatus() + "] " + task.getTitle() + " - " + task.getDescription());
                }
            }
        });

        tasksList.setItems(myTasks);
    }

    @FXML
    private void handleRefresh() {
        loadTasks();
    }
}

