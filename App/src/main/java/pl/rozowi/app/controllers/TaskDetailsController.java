package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;

import java.util.List;

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
    private ComboBox<User> assigneeComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private Task task;
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

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

        User current = MainApplication.getCurrentUser();
        boolean isLeader = current.getRoleId() == 3;
        statusComboBox.setDisable(!isLeader);

        if (assigneeComboBox != null) {
            if (isLeader) {
                List<User> members = teamMemberDAO.getTeamMembers(task.getTeamId());
                assigneeComboBox.getItems().setAll(members);
                members.stream()
                        .filter(u -> u.getId() == task.getAssignedTo())
                        .findFirst()
                        .ifPresent(assigneeComboBox::setValue);
            }
            assigneeComboBox.setDisable(!isLeader);
        }

        saveButton.setVisible(isLeader);
    }

    @FXML
    private void handleSave() {
        if (MainApplication.getCurrentUser().getRoleId() != 3) {
            closeWindow();
            return;
        }

        String newStatus = statusComboBox.getValue();
        boolean okStatus = taskDAO.updateTaskStatus(task.getId(), newStatus);

        boolean okAssign = true;
        if (assigneeComboBox != null) {
            User newAssignee = assigneeComboBox.getValue();
            if (newAssignee != null) {
                okAssign = taskDAO.assignTask(task.getId(), newAssignee.getId());
            }
        }

        if (okStatus && okAssign) {
            closeWindow();
        } else {
            new Alert(Alert.AlertType.ERROR, "Nie udało się zapisać zmian w zadaniu.").showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
