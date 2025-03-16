package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;
import java.util.List;

public class MyTasksController {

    @FXML
    private ListView<String> tasksList;


    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<String> myTasks;

    @FXML
    private void initialize() {
        loadTasks();
    }


    private void loadTasks() {
        List<Task> tasks = taskDAO.getTasksForUser(Session.currentUserId);
        myTasks = FXCollections.observableArrayList();
        for(Task task : tasks) {
            myTasks.add("[" + task.getStatus() + "] " + task.getName() + " - " + task.getDescription());
        }
        tasksList.setItems(myTasks);
    }

    @FXML
    private void handleRefresh() {
        loadTasks();
    }
}
