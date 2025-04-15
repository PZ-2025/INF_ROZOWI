package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;

public class TaskDetailsController {

    @FXML
    private Label taskIdLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label projectIdLabel;
    @FXML
    private Label teamIdLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label endDateLabel;

    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Task task;

    public void setTask(Task task) {
        this.task = task;
        displayTaskDetails();
    }

    private void displayTaskDetails() {
        taskIdLabel.setText("ID: " + task.getId());
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        projectIdLabel.setText("Projekt: " + task.getProjectId());
        teamIdLabel.setText("Zespół: " + task.getTeamId());
        startDateLabel.setText("Start: " + task.getStartDate());
        endDateLabel.setText("Koniec: " + task.getEndDate());

        statusComboBox.getItems().setAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue(task.getStatus());
    }

    @FXML
    private void handleSave() {
        task.setStatus(statusComboBox.getValue());

        TaskDAO taskDAO = new TaskDAO();
        boolean success = taskDAO.updateTask(task);

        if (success) {
            closeWindow();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText(null);
            alert.setContentText("Nie udało się zapisać zmian w zadaniu.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
