package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import pl.rozowi.app.util.Session;

import java.time.format.DateTimeFormatter;
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
    private final TaskAssignmentDAO assignmentDAO = new TaskAssignmentDAO();
    private final TeamMemberDAO memberDAO = new TeamMemberDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    @FXML
    private void initialize() {
        comboPriority.getItems().setAll("LOW", "MEDIUM", "HIGH");

        List<Project> projects = projectDAO.getAllProjects();
        comboProject.getItems().setAll(projects);
        comboProject.setConverter(new StringConverter<>() {
            @Override
            public String toString(Project p) {
                return p == null ? "" : p.getId() + " – " + p.getProjectName();
            }

            @Override
            public Project fromString(String s) {
                return null;
            }
        });

        int userId = Session.currentUserId;
        List<Team> teams = teamDAO.getTeamsForUser(userId);
        comboTeam.getItems().setAll(teams);
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

        comboTeam.getSelectionModel().selectedItemProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                comboAssignee.getItems().setAll(memberDAO.getTeamMembers(newT.getId()));
            } else {
                comboAssignee.getItems().clear();
            }
        });

        comboAssignee.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                return u == null ? "" : u.getId() + " – " + u.getEmail();
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
            new Alert(Alert.AlertType.WARNING, "Uzupełnij wszystkie pola!").showAndWait();
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

        // zapis zadania
        if (!taskDAO.insertTask(t)) {
            new Alert(Alert.AlertType.ERROR, "Nie udało się zapisać zadania!").showAndWait();
            return;
        }

        // przypisanie użytkownika
        TaskAssignment ta = new TaskAssignment();
        ta.setTaskId(t.getId());
        ta.setUserId(user.getId());
        assignmentDAO.insertTaskAssignment(ta);

        new Alert(Alert.AlertType.INFORMATION, "Zadanie utworzone i przypisane!").showAndWait();
        ((Stage) txtTitle.getScene().getWindow()).close();
    }

    @FXML
    private void handleClose() {
        ((Stage) txtTitle.getScene().getWindow()).close();
    }
}
