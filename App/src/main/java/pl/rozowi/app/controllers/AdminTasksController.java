package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminTasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colId;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colDescription;
    @FXML
    private TableColumn<Task, String> colStatus;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colStartDate;
    @FXML
    private TableColumn<Task, String> colEndDate;
    @FXML
    private TableColumn<Task, Number> colProjectId;
    @FXML
    private TableColumn<Task, Number> colTeamId;
    @FXML
    private TableColumn<Task, String> colAssignedTo;
    @FXML
    private TextField searchField;

    private final TaskDAO taskDAO = new TaskDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<Task> taskData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colDescription.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colStartDate.setCellValueFactory(c -> c.getValue().startDateProperty());
        colEndDate.setCellValueFactory(c -> c.getValue().endDateProperty());
        colProjectId.setCellValueFactory(c -> c.getValue().projectIdProperty());
        colTeamId.setCellValueFactory(c -> c.getValue().teamIdProperty());
        colAssignedTo.setCellValueFactory(c -> c.getValue().assignedEmailProperty());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTasks(newValue);
        });

        loadAllTasks();
    }

    private void loadAllTasks() {
        // Potrzebna jest metoda getAllTasks() w TaskDAO
        // Ta metoda nie istnieje, więc trzeba ją zaimplementować
        List<Task> tasks = taskDAO.getAllTasks();
        taskData.setAll(tasks);
        tasksTable.setItems(taskData);
    }

    private void filterTasks(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tasksTable.setItems(taskData);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Task> filteredData = FXCollections.observableArrayList();

        for (Task task : taskData) {
            if (task.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                task.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                task.getStatus().toLowerCase().contains(lowerCaseFilter) ||
                task.getPriority().toLowerCase().contains(lowerCaseFilter)) {
                filteredData.add(task);
            }
        }

        tasksTable.setItems(filteredData);
    }

    @FXML
    private void onAddTask() {
        showTaskDialog(null);
    }

    @FXML
    private void onEditTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie", "Musisz wybrać zadanie do edycji");
            return;
        }

        showTaskDialog(selectedTask);
    }

    @FXML
    private void onDeleteTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie", "Musisz wybrać zadanie do usunięcia");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć zadanie: " + selectedTask.getTitle() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Ta funkcja wymaga implementacji w TaskDAO
            // taskDAO.deleteTask(selectedTask.getId());
            // loadAllTasks();
        }
    }

    private void showTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Dodaj nowe zadanie" : "Edytuj zadanie");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Tworzenie siatki formularza
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Tytuł zadania");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Opis zadania");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Nowe", "W toku", "Zakończone");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Pobieranie listy projektów
        ComboBox<Project> projectCombo = new ComboBox<>();
        try {
            List<Project> projects = projectDAO.getAllProjects();
            projectCombo.setItems(FXCollections.observableArrayList(projects));
            projectCombo.setConverter(new StringConverter<Project>() {
                @Override
                public String toString(Project project) {
                    return project != null ? project.getName() : "";
                }

                @Override
                public Project fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pobieranie listy zespołów
        ComboBox<Team> teamCombo = new ComboBox<>();
        try {
            List<Team> teams = teamDAO.getAllTeams();
            teamCombo.setItems(FXCollections.observableArrayList(teams));
            teamCombo.setConverter(new StringConverter<Team>() {
                @Override
                public String toString(Team team) {
                    return team != null ? team.getTeamName() : "";
                }

                @Override
                public Team fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Wybór użytkownika do przypisania
        ComboBox<User> userCombo = new ComboBox<>();
        // Aktualizacja listy użytkowników przy wyborze zespołu
        teamCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                List<User> teamMembers = teamDAO.getTeamMembers(newValue.getId());
                userCombo.setItems(FXCollections.observableArrayList(teamMembers));
            } else {
                userCombo.getItems().clear();
            }
        });

        userCombo.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        // Ustawienie wartości dla edycji istniejącego zadania
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusCombo.setValue(task.getStatus());
            priorityCombo.setValue(task.getPriority());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (task.getStartDate() != null && !task.getStartDate().isEmpty()) {
                startDatePicker.setValue(LocalDate.parse(task.getStartDate(), formatter));
            }

            if (task.getEndDate() != null && !task.getEndDate().isEmpty()) {
                endDatePicker.setValue(LocalDate.parse(task.getEndDate(), formatter));
            }

            // Ustawienie projektu
            int projectId = task.getProjectId();
            for (Project project : projectCombo.getItems()) {
                if (project.getId() == projectId) {
                    projectCombo.setValue(project);
                    break;
                }
            }

            // Ustawienie zespołu
            int teamId = task.getTeamId();
            for (Team team : teamCombo.getItems()) {
                if (team.getId() == teamId) {
                    teamCombo.setValue(team);
                    // Ładowanie członków zespołu
                    List<User> teamMembers = teamDAO.getTeamMembers(teamId);
                    userCombo.setItems(FXCollections.observableArrayList(teamMembers));

                    // Tutaj trzeba pobierać przypisanego użytkownika z bazy
                    // i ustawić go w userCombo
                    break;
                }
            }
        }

        // Dodanie kontrolek do siatki
        grid.add(new Label("Tytuł:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Opis:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(new Label("Priorytet:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Data rozpoczęcia:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("Data zakończenia:"), 0, 5);
        grid.add(endDatePicker, 1, 5);
        grid.add(new Label("Projekt:"), 0, 6);
        grid.add(projectCombo, 1, 6);
        grid.add(new Label("Zespół:"), 0, 7);
        grid.add(teamCombo, 1, 7);
        grid.add(new Label("Przypisany do:"), 0, 8);
        grid.add(userCombo, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Walidacja pól
                if (titleField.getText().isEmpty() || descriptionArea.getText().isEmpty() ||
                    statusCombo.getValue() == null || priorityCombo.getValue() == null ||
                    startDatePicker.getValue() == null || endDatePicker.getValue() == null ||
                    projectCombo.getValue() == null || teamCombo.getValue() == null ||
                    userCombo.getValue() == null) {
                    showWarning("Brakujące dane", "Wszystkie pola są wymagane.");
                    return null;
                }

                Task result = task != null ? task : new Task();
                result.setTitle(titleField.getText());
                result.setDescription(descriptionArea.getText());
                result.setStatus(statusCombo.getValue());
                result.setPriority(priorityCombo.getValue());
                result.setStartDate(startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.setEndDate(endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.setProjectId(projectCombo.getValue().getId());
                result.setTeamId(teamCombo.getValue().getId());
                result.setAssignedTo(userCombo.getValue().getId());

                return result;
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(t -> {
            try {
                if (task == null) {
                    // Nowe zadanie
                    boolean insertSuccess = taskDAO.insertTask(t);
                    if (insertSuccess) {
                        // Przypisanie zadania do użytkownika
                        taskDAO.assignTask(t.getId(), t.getAssignedTo());
                    }
                } else {
                    // Aktualizacja zadania
                    boolean updateSuccess = taskDAO.updateTask(t);
                    if (updateSuccess) {
                        // Przypisanie zadania do użytkownika
                        taskDAO.assignTask(t.getId(), t.getAssignedTo());
                    }
                }
                loadAllTasks();
            } catch (Exception e) {
                showError("Błąd", e.getMessage());
            }
        });
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
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