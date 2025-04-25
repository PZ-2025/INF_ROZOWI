package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskCreateController {
    @FXML
    private ComboBox<Project> comboProject;
    @FXML
    private ComboBox<Team> comboTeam;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDesc;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private ComboBox<String> comboPriority;
    @FXML
    private ComboBox<User> comboAssignee;

    private final TaskDAO taskDAO = new TaskDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    @FXML
    private void initialize() {
        // Set up priority dropdown
        comboPriority.getItems().setAll("LOW", "MEDIUM", "HIGH");
        comboPriority.setValue("MEDIUM");

        // Load available projects based on user role
        loadAvailableProjects();

        // Set up project selection to populate team dropdown
        comboProject.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTeamsForProject(newVal.getId());
            } else {
                comboTeam.getItems().clear();
                comboAssignee.getItems().clear();
            }
        });

        // Set up team selection to populate assignee dropdown
        comboTeam.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMembersForTeam(newVal.getId());
            } else {
                comboAssignee.getItems().clear();
            }
        });

        // Set up converters for displaying objects in dropdowns
        setupConverters();
    }

    private void loadAvailableProjects() {
        try {
            List<Project> projects;
            User currentUser = MainApplication.getCurrentUser();

            // Filter projects based on user role
            if (currentUser != null) {
                int roleId = currentUser.getRoleId();

                if (roleId == 1) {
                    // Admin can see all projects
                    projects = projectDAO.getAllProjects();
                } else if (roleId == 2) {
                    // Manager can see only their projects
                    projects = projectDAO.getProjectsForManager(currentUser.getId());
                } else if (roleId == 3) {
                    // Team Leader can see projects where they are a team leader
                    int teamId = teamMemberDAO.getTeamIdForUser(currentUser.getId());
                    Team team = null;

                    // Find the team
                    for (Team t : teamDAO.getAllTeams()) {
                        if (t.getId() == teamId) {
                            team = t;
                            break;
                        }
                    }

                    if (team != null) {
                        int projectId = team.getProjectId();
                        Project project = null;

                        // Find the specific project
                        for (Project p : projectDAO.getAllProjects()) {
                            if (p.getId() == projectId) {
                                project = p;
                                break;
                            }
                        }

                        projects = project != null ? Collections.singletonList(project) : new ArrayList<>();

                        // For Team Leader, auto-select the project and team
                        if (!projects.isEmpty()) {
                            comboProject.getItems().setAll(projects);
                            comboProject.setValue(projects.get(0));

                            List<Team> teams = new ArrayList<>();
                            for (Team t : teamDAO.getAllTeams()) {
                                if (t.getId() == teamId) {
                                    teams.add(t);
                                    break;
                                }
                            }

                            if (!teams.isEmpty()) {
                                comboTeam.getItems().setAll(teams);
                                comboTeam.setValue(teams.get(0));
                            }
                        }
                        return;
                    } else {
                        projects = new ArrayList<>();
                    }
                } else {
                    // Regular users can't create tasks
                    projects = new ArrayList<>();
                }
            } else {
                projects = new ArrayList<>();
            }

            comboProject.getItems().setAll(projects);

            // If only one project, auto-select it
            if (projects.size() == 1) {
                comboProject.setValue(projects.get(0));
            }
        } catch (SQLException e) {
            showError("Error loading projects", e.getMessage());
        }
    }

    private void loadTeamsForProject(int projectId) {
        try {
            List<Team> projectTeams = new ArrayList<>();

            // Get all teams and filter by project
            for (Team team : teamDAO.getAllTeams()) {
                if (team.getProjectId() == projectId) {
                    projectTeams.add(team);
                }
            }

            comboTeam.getItems().setAll(projectTeams);

            // If only one team, auto-select it
            if (projectTeams.size() == 1) {
                comboTeam.setValue(projectTeams.get(0));
            }
        } catch (SQLException e) {
            showError("Error loading teams", e.getMessage());
        }
    }

    private void loadMembersForTeam(int teamId) {
        try {
            List<User> members = teamMemberDAO.getTeamMembers(teamId);
            comboAssignee.getItems().setAll(members);
        } catch (Exception e) {
            showError("Error loading team members", e.getMessage());
        }
    }

    private void setupConverters() {
        // Project converter
        comboProject.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project p) {
                return p == null ? "" : p.getId() + " – " + p.getName();
            }

            @Override
            public Project fromString(String s) {
                return null;
            }
        });

        // Team converter
        comboTeam.setConverter(new StringConverter<>() {
            @Override
            public String toString(Team t) {
                return t == null ? "" : t.getId() + " – " + t.getTeamName();
            }

            @Override
            public Team fromString(String s) {
                return null;
            }
        });

        // User converter
        comboAssignee.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                return u == null ? "" : u.getId() + " – " + u.getName() + " " + u.getLastName() + " (" + u.getEmail() + ")";
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });
    }

    @FXML
    private void handleCreate() {
        Project proj = comboProject.getValue();
        Team team = comboTeam.getValue();
        String title = txtTitle.getText().trim();
        String desc = txtDesc.getText().trim();
        String pri = comboPriority.getValue();
        User user = comboAssignee.getValue();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Validate form fields
        if (proj == null || team == null || title.isEmpty() || desc.isEmpty()
                || pri == null || user == null
                || dpStartDate == null || dpStartDate.getValue() == null
                || dpEndDate == null || dpEndDate.getValue() == null) {
            showWarning("Please fill in all fields!");
            return;
        }

        Task t = new Task();
        t.setProjectId(proj.getId());
        t.setTeamId(team.getId());
        t.setTitle(title);
        t.setDescription(desc);
        t.setStatus("Nowe");
        t.setPriority(pri);
        t.setStartDate(dpStartDate.getValue().format(fmt));
        t.setEndDate(dpEndDate.getValue().format(fmt));
        t.setTeamName(team.getTeamName());

        // Don't set assigned fields on the task object as they might not exist in DB schema

        // Save task
        if (!taskDAO.insertTask(t)) {
            showError("Error", "Failed to create task!");
            return;
        }

        // Create task_assignments record manually with direct SQL
        try (java.sql.Connection conn = pl.rozowi.app.database.DatabaseManager.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)")) {
            stmt.setInt(1, t.getId());
            stmt.setInt(2, user.getId());
            int affected = stmt.executeUpdate();

            if (affected <= 0) {
                showWarning("Task created but user assignment failed!");
            }
        } catch (SQLException e) {
            showError("Assignment Error", "Task created but assignment failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Create task activity to track creation
        try {
            TaskActivity activity = new TaskActivity();
            activity.setTaskId(t.getId());
            activity.setUserId(Session.currentUserId);
            activity.setActivityType("CREATE");
            activity.setDescription("Task created and assigned to " + user.getName() + " " + user.getLastName());

            TaskActivityDAO activityDAO = new TaskActivityDAO();
            activityDAO.insertTaskActivity(activity);
        } catch (Exception e) {
            // Non-critical failure - just log
            System.err.println("Failed to create task activity record: " + e.getMessage());
        }

        showInfo("Task Created", "Task has been created successfully!");
        closeWindow();
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) txtTitle.getScene().getWindow()).close();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}