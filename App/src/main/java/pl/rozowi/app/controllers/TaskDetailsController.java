package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
<<<<<<< HEAD
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;

import java.sql.SQLException;
=======
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;

>>>>>>> origin/main
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
<<<<<<< HEAD
    private final UserDAO userDAO = new UserDAO();
=======
>>>>>>> origin/main

    public void setTask(Task task) {
        this.task = task;
        displayTaskDetails();
    }

    private void displayTaskDetails() {
<<<<<<< HEAD
        taskIdLabel.setText(String.valueOf(task.getId()));
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        projectIdLabel.setText(String.valueOf(task.getProjectId()));
        teamIdLabel.setText(String.valueOf(task.getTeamId()));
        startDateLabel.setText(task.getStartDate());
        endDateLabel.setText(task.getEndDate());

        // Set status dropdown
        statusComboBox.getItems().setAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue(task.getStatus());

        // Check current user role
        User current = MainApplication.getCurrentUser();
        boolean isLeader = current.getRoleId() == 3;
        boolean isEmployee = current.getRoleId() == 4;
        boolean isManager = current.getRoleId() == 2;
        boolean isAdmin = current.getRoleId() == 1;

        // Status editable for leader, employee, manager, and admin
        statusComboBox.setDisable(!(isLeader || isEmployee || isManager || isAdmin));

        // Populate and configure assignee dropdown
        if (assigneeComboBox != null) {
            try {
                // Get team members
                List<User> members = teamMemberDAO.getTeamMembers(task.getTeamId());
                assigneeComboBox.getItems().setAll(members);

                // Set the current assignee if one exists
                int assignedUserId = task.getAssignedTo();
                if (assignedUserId > 0) {
                    for (User u : members) {
                        if (u.getId() == assignedUserId) {
                            assigneeComboBox.setValue(u);
                            break;
                        }
                    }
                }

                // Only leaders, managers, and admins can reassign tasks
                assigneeComboBox.setDisable(!(isLeader || isManager || isAdmin));

                // Set up cell factory for better display
                assigneeComboBox.setButtonCell(new ListCell<User>() {
                    @Override
                    protected void updateItem(User item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " " + item.getLastName() + " (" + item.getEmail() + ")");
                        }
                    }
                });

                assigneeComboBox.setCellFactory(param -> new ListCell<User>() {
                    @Override
                    protected void updateItem(User item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " " + item.getLastName() + " (" + item.getEmail() + ")");
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading team members: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Save button visible for authorized users
        saveButton.setVisible(isLeader || isEmployee || isManager || isAdmin);
=======
        taskIdLabel.setText("ID: " + task.getId());
        titleLabel.setText(task.getTitle());
        descriptionLabel.setText(task.getDescription());
        projectIdLabel.setText("Projekt: " + task.getProjectId());
        teamIdLabel.setText("Zespół: " + task.getTeamId());
        startDateLabel.setText("Start: " + task.getStartDate());
        endDateLabel.setText("Koniec: " + task.getEndDate());

        // status
        statusComboBox.getItems().setAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue(task.getStatus());

        // kto może edytować
        User current = MainApplication.getCurrentUser();
        boolean isLeader = current.getRoleId() == 3;
        boolean isEmployee = current.getRoleId() == 4;

        // status edytowalny dla leadera i pracownika
        statusComboBox.setDisable(!(isLeader || isEmployee));

        // assignee tylko dla leadera
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

        // przycisk Zapisz widoczny dla leadera i pracownika
        saveButton.setVisible(isLeader || isEmployee);
>>>>>>> origin/main
    }

    @FXML
    private void handleSave() {
        User current = MainApplication.getCurrentUser();
        int role = current.getRoleId();

<<<<<<< HEAD
        // Only leader (3), employee (4), manager (2), or admin (1) can save changes
        if (role != 3 && role != 4 && role != 2 && role != 1) {
=======
        // tylko leader (3) i pracownik (4) mogą zapisać zmiany
        if (role != 3 && role != 4) {
>>>>>>> origin/main
            closeWindow();
            return;
        }

        boolean okStatus = true, okAssign = true;

<<<<<<< HEAD
        // Save status change
        String newStatus = statusComboBox.getValue();
        if (newStatus != null && !newStatus.equals(task.getStatus())) {
            okStatus = taskDAO.updateTaskStatus(task.getId(), newStatus);
            if (okStatus) {
                task.setStatus(newStatus);
            }
        }

        // Only leaders, managers, and admins can reassign
        if ((role == 3 || role == 2 || role == 1) && assigneeComboBox != null) {
            User newAssignee = assigneeComboBox.getValue();
            if (newAssignee != null &&
                (task.getAssignedTo() != newAssignee.getId() || task.getAssignedTo() == 0)) {
                okAssign = taskDAO.assignTask(task.getId(), newAssignee.getId());
                if (okAssign) {
                    task.setAssignedTo(newAssignee.getId());
                    task.setAssignedEmail(newAssignee.getEmail());
                }
=======
        // zapis statusu
        String newStatus = statusComboBox.getValue();
        if (newStatus != null && !newStatus.equals(task.getStatus())) {
            okStatus = taskDAO.updateTaskStatus(task.getId(), newStatus);
        }

        // jeśli leader, to też zapis przypisania
        if (role == 3 && assigneeComboBox != null) {
            User newAssignee = assigneeComboBox.getValue();
            if (newAssignee != null && newAssignee.getId() != task.getAssignedTo()) {
                okAssign = taskDAO.assignTask(task.getId(), newAssignee.getId());
>>>>>>> origin/main
            }
        }

        if (okStatus && okAssign) {
<<<<<<< HEAD
            showInfo("Changes saved successfully");
            closeWindow();
        } else {
            showError("Failed to save changes to the task.");
=======
            closeWindow();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Nie udało się zapisać zmian w zadaniu.")
                    .showAndWait();
>>>>>>> origin/main
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
<<<<<<< HEAD

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
=======
}
>>>>>>> origin/main
