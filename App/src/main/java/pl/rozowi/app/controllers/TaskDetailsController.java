package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.TaskEditDialog;

import java.sql.SQLException;
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
    @FXML
    private Button editButton; // New button for editing all task details

    private Task task;
    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final UserDAO userDAO = new UserDAO();

    public void setTask(Task task) {
        this.task = task;
        displayTaskDetails();
    }

    private void displayTaskDetails() {
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

        // Only show edit button for admin and manager
        if (editButton != null) {
            editButton.setVisible(isAdmin || isManager);
        }

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
    }

    @FXML
    private void handleSave() {
        User current = MainApplication.getCurrentUser();
        int role = current.getRoleId();

        // Only leader (3), employee (4), manager (2), or admin (1) can save changes
        if (role != 3 && role != 4 && role != 2 && role != 1) {
            closeWindow();
            return;
        }

        boolean okStatus = true, okAssign = true;

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
            }
        }

        if (okStatus && okAssign) {
            showInfo("Changes saved successfully");
            closeWindow();
        } else {
            showError("Failed to save changes to the task.");
        }
    }

    /**
     * Handles the new Edit button click to edit all task properties.
     * Opens the comprehensive task editor dialog.
     */
    @FXML
    private void handleEdit() {
        // Only admin and manager can use full edit functionality
        User current = MainApplication.getCurrentUser();
        if (current.getRoleId() != 1 && current.getRoleId() != 2) {
            showInfo("You don't have permission to edit all task properties");
            return;
        }

        // Use our new TaskEditDialog utility
        boolean success = TaskEditDialog.showEditDialog(task);

        if (success) {
            // Refresh the task from the database to show updated values
            Task updatedTask = null;
            try {
                // In a real implementation, we would have a taskDAO.getTaskById method
                // Here's a workaround assuming we have a project or team ID
                List<Task> tasks = null;
                if (task.getProjectId() > 0) {
                    tasks = taskDAO.getTasksByProjectId(task.getProjectId());
                } else if (task.getTeamId() > 0) {
                    tasks = taskDAO.getTasksByTeamId(task.getTeamId());
                }

                if (tasks != null) {
                    for (Task t : tasks) {
                        if (t.getId() == task.getId()) {
                            updatedTask = t;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error refreshing task: " + e.getMessage());
            }

            if (updatedTask != null) {
                this.task = updatedTask;
                displayTaskDetails();
            }

            // Close this dialog as it now shows stale data
            closeWindow();
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