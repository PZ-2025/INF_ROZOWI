package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.services.ActivityService;
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
        comboPriority.getItems().setAll("Niskie", "Średnie", "Wysokie");
        comboPriority.setValue("Średnie");

        loadAvailableProjects();

        comboProject.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTeamsForProject(newVal.getId());
            } else {
                comboTeam.getItems().clear();
                comboAssignee.getItems().clear();
            }
        });

        comboTeam.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMembersForTeam(newVal.getId());
            } else {
                comboAssignee.getItems().clear();
            }
        });

        setupConverters();
    }

    private void loadAvailableProjects() {
        try {
            List<Project> projects;
            User currentUser = MainApplication.getCurrentUser();

            if (currentUser != null) {
                int roleId = currentUser.getRoleId();

                if (roleId == 1) {
                    projects = projectDAO.getAllProjects();
                } else if (roleId == 2) {
                    projects = projectDAO.getProjectsForManager(currentUser.getId());
                } else if (roleId == 3) {
                    int teamId = teamMemberDAO.getTeamIdForUser(currentUser.getId());
                    Team team = null;

                    for (Team t : teamDAO.getAllTeams()) {
                        if (t.getId() == teamId) {
                            team = t;
                            break;
                        }
                    }

                    if (team != null) {
                        int projectId = team.getProjectId();
                        Project project = null;

                        for (Project p : projectDAO.getAllProjects()) {
                            if (p.getId() == projectId) {
                                project = p;
                                break;
                            }
                        }

                        projects = project != null ? Collections.singletonList(project) : new ArrayList<>();

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
                    projects = new ArrayList<>();
                }
            } else {
                projects = new ArrayList<>();
            }

            comboProject.getItems().setAll(projects);

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

            for (Team team : teamDAO.getAllTeams()) {
                if (team.getProjectId() == projectId) {
                    projectTeams.add(team);
                }
            }

            comboTeam.getItems().setAll(projectTeams);

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

        if (!taskDAO.insertTask(t)) {
            showError("Error", "Failed to create task!");
            return;
        }

        boolean assignmentSuccess = false;
        try (java.sql.Connection conn = pl.rozowi.app.database.DatabaseManager.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO task_assignments (task_id, user_id) VALUES (?, ?)")) {
            stmt.setInt(1, t.getId());
            stmt.setInt(2, user.getId());
            int affected = stmt.executeUpdate();

            assignmentSuccess = affected > 0;
            if (!assignmentSuccess) {
                showWarning("Task created but user assignment failed!");
            }
        } catch (SQLException e) {
            showError("Assignment Error", "Task created but assignment failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        ActivityService.logTaskCreation(t.getId(), t.getTitle(), user.getId());

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