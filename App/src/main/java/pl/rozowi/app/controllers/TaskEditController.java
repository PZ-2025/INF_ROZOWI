package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for editing task details.
 * This controller is used by admin and other privileged users to edit task properties.
 */
public class TaskEditController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<String> priorityComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<Team> teamComboBox;
    @FXML
    private ComboBox<User> assigneeComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final UserDAO userDAO = new UserDAO();

    private Task currentTask;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Initialize the controller.
     */
    @FXML
    private void initialize() {
        // Set up status options
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");

        // Set up priority options
        priorityComboBox.getItems().addAll("LOW", "MEDIUM", "HIGH");

        // Set up converters for team and assignee dropdowns
        setupConverters();
    }

    /**
     * Sets the task to be edited and populates form fields.
     *
     * @param task The task to edit
     */
    public void setTask(Task task) {
        this.currentTask = task;

        // Populate fields with current values
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        statusComboBox.setValue(task.getStatus());
        priorityComboBox.setValue(task.getPriority());

        // Parse dates
        if (task.getStartDate() != null && !task.getStartDate().isEmpty()) {
            try {
                startDatePicker.setValue(LocalDate.parse(task.getStartDate()));
            } catch (Exception e) {
                System.err.println("Error parsing start date: " + e.getMessage());
            }
        }

        if (task.getEndDate() != null && !task.getEndDate().isEmpty()) {
            try {
                endDatePicker.setValue(LocalDate.parse(task.getEndDate()));
            } catch (Exception e) {
                System.err.println("Error parsing end date: " + e.getMessage());
            }
        }

        // Load teams
        loadTeams(task.getProjectId(), task.getTeamId());

        // Load team members for assignee dropdown
        if (task.getTeamId() > 0) {
            loadTeamMembers(task.getTeamId(), task.getAssignedTo());
        }
    }

    /**
     * Loads available teams for the project and selects the current team.
     */
    private void loadTeams(int projectId, int currentTeamId) {
        try {
            List<Team> teams = teamDAO.getAllTeams();
            // Filter teams by project
            teamComboBox.getItems().clear();
            for (Team team : teams) {
                if (team.getProjectId() == projectId) {
                    teamComboBox.getItems().add(team);
                    if (team.getId() == currentTeamId) {
                        teamComboBox.setValue(team);
                    }
                }
            }

            // Add listener to update members when team changes
            teamComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadTeamMembers(newVal.getId(), 0);
                }
            });
        } catch (SQLException e) {
            showError("Error loading teams", e.getMessage());
        }
    }

    /**
     * Loads team members for the selected team and sets the current assignee.
     */
    private void loadTeamMembers(int teamId, int currentAssigneeId) {
        try {
            List<User> members = teamMemberDAO.getTeamMembers(teamId);
            assigneeComboBox.getItems().clear();
            assigneeComboBox.getItems().addAll(members);

            // Select current assignee if present
            if (currentAssigneeId > 0) {
                for (User user : members) {
                    if (user.getId() == currentAssigneeId) {
                        assigneeComboBox.setValue(user);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            showError("Error loading team members", e.getMessage());
        }
    }

    /**
     * Sets up string converters for dropdown controls.
     */
    private void setupConverters() {
        // Team converter
        teamComboBox.setConverter(new StringConverter<Team>() {
            @Override
            public String toString(Team team) {
                return team == null ? "" : team.getTeamName();
            }

            @Override
            public Team fromString(String string) {
                return null; // Not needed for our use case
            }
        });

        // User/Assignee converter
        assigneeComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")";
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for our use case
            }
        });
    }

    /**
     * Handles the save button action.
     * Updates the task with the new values and closes the dialog.
     */
    @FXML
    private void handleSave() {
        // Validate form fields
        if (!validateForm()) {
            return;
        }

        // Update task with values from form
        currentTask.setTitle(titleField.getText().trim());
        currentTask.setDescription(descriptionArea.getText().trim());
        currentTask.setStatus(statusComboBox.getValue());
        currentTask.setPriority(priorityComboBox.getValue());

        // Format dates for DB storage
        if (startDatePicker.getValue() != null) {
            currentTask.setStartDate(startDatePicker.getValue().format(dateFormatter));
        }

        if (endDatePicker.getValue() != null) {
            currentTask.setEndDate(endDatePicker.getValue().format(dateFormatter));
        }

        // Update team if changed
        Team selectedTeam = teamComboBox.getValue();
        if (selectedTeam != null) {
            currentTask.setTeamId(selectedTeam.getId());
            currentTask.setTeamName(selectedTeam.getTeamName());
        }

        // Save task to database
        boolean success = taskDAO.updateTask(currentTask);

        // Handle assignee changes if needed
        User selectedUser = assigneeComboBox.getValue();
        if (selectedUser != null && (currentTask.getAssignedTo() != selectedUser.getId())) {
            boolean assignSuccess = taskDAO.assignTask(currentTask.getId(), selectedUser.getId());
            if (assignSuccess) {
                currentTask.setAssignedTo(selectedUser.getId());
                currentTask.setAssignedEmail(selectedUser.getEmail());
            } else {
                showWarning("Task updated but user assignment failed");
            }
        }

        if (success) {
            showInfo("Task Updated", "Task details have been updated successfully");
            closeDialog();
        } else {
            showError("Error", "Failed to update task details");
        }
    }

    /**
     * Handles the cancel button action.
     * Closes the dialog without saving changes.
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Validates form fields before saving.
     *
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateForm() {
        // Title is required
        if (titleField.getText().trim().isEmpty()) {
            showWarning("Please enter a title for the task");
            return false;
        }

        // Status is required
        if (statusComboBox.getValue() == null) {
            showWarning("Please select a status for the task");
            return false;
        }

        // Priority is required
        if (priorityComboBox.getValue() == null) {
            showWarning("Please select a priority for the task");
            return false;
        }

        // Dates validation
        if (startDatePicker.getValue() == null) {
            showWarning("Please select a start date for the task");
            return false;
        }

        if (endDatePicker.getValue() == null) {
            showWarning("Please select an end date for the task");
            return false;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showWarning("End date cannot be before start date");
            return false;
        }

        // Team is required
        if (teamComboBox.getValue() == null) {
            showWarning("Please select a team for the task");
            return false;
        }

        return true;
    }

    /**
     * Closes the dialog window.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an information alert.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}