package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminTasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Integer> colId;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colProject;
    @FXML
    private TableColumn<Task, String> colTeam;
    @FXML
    private TableColumn<Task, String> colStatus;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colStartDate;
    @FXML
    private TableColumn<Task, String> colEndDate;
    @FXML
    private TableColumn<Task, String> colAssignee;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<Project> projectFilterCombo;
    @FXML
    private ComboBox<Team> teamFilterCombo;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private ComboBox<String> priorityFilterCombo;
    @FXML
    private DatePicker startDateFilter;
    @FXML
    private DatePicker endDateFilter;

    @FXML
    private Label detailId;
    @FXML
    private Label detailTitle;
    @FXML
    private Label detailProject;
    @FXML
    private Label detailTeam;
    @FXML
    private Label detailStatus;
    @FXML
    private Label detailPriority;
    @FXML
    private Label detailStartDate;
    @FXML
    private Label detailEndDate;
    @FXML
    private Label detailAssignee;
    @FXML
    private Label detailLastUpdate;
    @FXML
    private TextArea detailDescription;

    @FXML
    private TableView<TaskActivity> activityTable;
    @FXML
    private TableColumn<TaskActivity, String> colActivityDate;
    @FXML
    private TableColumn<TaskActivity, String> colActivityUser;
    @FXML
    private TableColumn<TaskActivity, String> colActivityType;
    @FXML
    private TableColumn<TaskActivity, String> colActivityDesc;

    private final TaskDAO taskDAO = new TaskDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TaskActivityDAO activityDAO = new TaskActivityDAO();
    private final TaskAssignmentDAO assignmentDAO = new TaskAssignmentDAO();

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private ObservableList<Task> filteredTasks = FXCollections.observableArrayList();
    private ObservableList<TaskActivity> taskActivities = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Konfiguracja kolumn tabeli zadań
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colProject.setCellValueFactory(data -> {
            int projectId = data.getValue().getProjectId();
            String projectName = "Nieznany";
            try {
                List<Project> projects = projectDAO.getAllProjects();
                for (Project project : projects) {
                    if (project.getId() == projectId) {
                        projectName = project.getName();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(projectName);
        });
        colTeam.setCellValueFactory(data -> data.getValue().teamNameProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colAssignee.setCellValueFactory(data -> data.getValue().assignedEmailProperty());

        // Konfiguracja kolumn tabeli aktywności
        colActivityDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getActivityDate() != null ?
                        data.getValue().getActivityDate().toString() : ""));
        colActivityUser.setCellValueFactory(data -> {
            int userId = data.getValue().getUserId();
            String userName = "Nieznany";
            try {
                List<User> users = userDAO.getAllUsers();
                for (User user : users) {
                    if (user.getId() == userId) {
                        userName = user.getName() + " " + user.getLastName();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(userName);
        });
        colActivityType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActivityType()));
        colActivityDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        // Konfiguracja filtrów
        setupFilters();

        // Obsługa wyboru zadania - aktualizacja szczegółów i aktywności
        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayTaskDetails(newSelection);
                loadTaskActivities(newSelection.getId());
            } else {
                clearTaskDetails();
                taskActivities.clear();
            }
        });

        // Wczytaj dane
        loadTasks();
    }

    private void setupFilters() {
        try {
            // Projekty
            List<Project> projects = projectDAO.getAllProjects();
            Project allProjects = new Project();
            allProjects.setId(0);
            allProjects.setName("Wszystkie projekty");

            ObservableList<Project> projectList = FXCollections.observableArrayList();
            projectList.add(allProjects);
            projectList.addAll(projects);
            projectFilterCombo.setItems(projectList);
            projectFilterCombo.setValue(allProjects);

            projectFilterCombo.setConverter(new StringConverter<Project>() {
                @Override
                public String toString(Project project) {
                    return project != null ? project.getName() : "";
                }

                @Override
                public Project fromString(String string) {
                    return null;
                }
            });

            // Zespoły
            List<Team> teams = teamDAO.getAllTeams();
            Team allTeams = new Team();
            allTeams.setId(0);
            allTeams.setTeamName("Wszystkie zespoły");

            ObservableList<Team> teamList = FXCollections.observableArrayList();
            teamList.add(allTeams);
            teamList.addAll(teams);
            teamFilterCombo.setItems(teamList);
            teamFilterCombo.setValue(allTeams);

            teamFilterCombo.setConverter(new StringConverter<Team>() {
                @Override
                public String toString(Team team) {
                    return team != null ? team.getTeamName() : "";
                }

                @Override
                public Team fromString(String string) {
                    return null;
                }
            });

            // Statusy
            statusFilterCombo.getItems().addAll("Wszystkie", "Nowe", "W toku", "Zakończone");
            statusFilterCombo.setValue("Wszystkie");

            // Priorytety
            priorityFilterCombo.getItems().addAll("Wszystkie", "LOW", "MEDIUM", "HIGH");
            priorityFilterCombo.setValue("Wszystkie");
        } catch (Exception e) {
            showError("Błąd konfiguracji filtrów", e.getMessage());
        }
    }

    private void loadTasks() {
        try {
            // W rzeczywistej implementacji pobralibyśmy wszystkie zadania
            // Tu uproszczony proces ładowania zadań bez filtrów
            List<Task> tasks = new ArrayList<>();

            // Na potrzeby testów tworzymy przykładowe zadania ze wszystkich projektów
            List<Project> projects = projectDAO.getAllProjects();
            for (Project project : projects) {
                List<Task> projectTasks = taskDAO.getTasksByProjectId(project.getId());
                if (projectTasks != null) {
                    tasks.addAll(projectTasks);
                }
            }

            allTasks.setAll(tasks);
            filteredTasks.setAll(tasks);
            tasksTable.setItems(filteredTasks);
        } catch (Exception e) {
            showError("Błąd podczas ładowania zadań", e.getMessage());
        }
    }

    private void loadTaskActivities(int taskId) {
        try {
            List<TaskActivity> activities = activityDAO.getActivitiesByTaskId(taskId);
            taskActivities.setAll(activities);
            activityTable.setItems(taskActivities);
        } catch (Exception e) {
            showError("Błąd podczas ładowania aktywności", e.getMessage());
        }
    }

    private void displayTaskDetails(Task task) {
        if (task == null) {
            clearTaskDetails();
            return;
        }

        detailId.setText(String.valueOf(task.getId()));
        detailTitle.setText(task.getTitle());

        try {
            // Pobierz nazwę projektu
            List<Project> projects = projectDAO.getAllProjects();
            for (Project project : projects) {
                if (project.getId() == task.getProjectId()) {
                    detailProject.setText(project.getName());
                    break;
                }
            }
        } catch (Exception e) {
            detailProject.setText("Błąd pobierania projektu");
        }

        detailTeam.setText(task.getTeamName());
        detailStatus.setText(task.getStatus());
        detailPriority.setText(task.getPriority());
        detailStartDate.setText(task.getStartDate());
        detailEndDate.setText(task.getEndDate());
        detailAssignee.setText(task.getAssignedEmail());
        detailDescription.setText(task.getDescription());

        // Przykładowa data ostatniej aktualizacji
        detailLastUpdate.setText("2025-04-24 12:34:56");
    }

    private void clearTaskDetails() {
        detailId.setText("-");
        detailTitle.setText("-");
        detailProject.setText("-");
        detailTeam.setText("-");
        detailStatus.setText("-");
        detailPriority.setText("-");
        detailStartDate.setText("-");
        detailEndDate.setText("-");
        detailAssignee.setText("-");
        detailLastUpdate.setText("-");
        detailDescription.setText("");
        taskActivities.clear();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty() && projectFilterCombo.getValue().getId() == 0 &&
                teamFilterCombo.getValue().getId() == 0 && statusFilterCombo.getValue().equals("Wszystkie") &&
                priorityFilterCombo.getValue().equals("Wszystkie") && startDateFilter.getValue() == null &&
                endDateFilter.getValue() == null) {
            // Brak filtrów - pokaż wszystkie zadania
            filteredTasks.setAll(allTasks);
            tasksTable.setItems(filteredTasks);
            return;
        }

        // Filtruj zadania według kryteriów
        ObservableList<Task> result = FXCollections.observableArrayList();

        for (Task task : allTasks) {
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(task.getId()).contains(searchText) ||
                    task.getTitle().toLowerCase().contains(searchText) ||
                    task.getDescription().toLowerCase().contains(searchText) ||
                    task.getStatus().toLowerCase().contains(searchText) ||
                    task.getTeamName().toLowerCase().contains(searchText) ||
                    task.getAssignedEmail().toLowerCase().contains(searchText);

            boolean matchesProject = projectFilterCombo.getValue().getId() == 0 ||
                    task.getProjectId() == projectFilterCombo.getValue().getId();

            boolean matchesTeam = teamFilterCombo.getValue().getId() == 0 ||
                    task.getTeamId() == teamFilterCombo.getValue().getId();

            boolean matchesStatus = statusFilterCombo.getValue().equals("Wszystkie") ||
                    task.getStatus().equals(statusFilterCombo.getValue());

            boolean matchesPriority = priorityFilterCombo.getValue().equals("Wszystkie") ||
                    task.getPriority().equals(priorityFilterCombo.getValue());

            // Filtracja dat
            boolean matchesStartDate = true;
            boolean matchesEndDate = true;

            if (startDateFilter.getValue() != null) {
                try {
                    LocalDate taskStartDate = LocalDate.parse(task.getStartDate());
                    matchesStartDate = !taskStartDate.isBefore(startDateFilter.getValue());
                } catch (Exception e) {
                    // Ignoruj błędy parsowania daty
                }
            }

            if (endDateFilter.getValue() != null) {
                try {
                    LocalDate taskEndDate = LocalDate.parse(task.getEndDate());
                    matchesEndDate = !taskEndDate.isAfter(endDateFilter.getValue());
                } catch (Exception e) {
                    // Ignoruj błędy parsowania daty
                }
            }

            // Dodaj zadanie tylko jeśli spełnia wszystkie kryteria filtrowania
            if (matchesSearch && matchesProject && matchesTeam && matchesStatus &&
                    matchesPriority && matchesStartDate && matchesEndDate) {
                result.add(task);
            }
        }

        filteredTasks.setAll(result);
        tasksTable.setItems(filteredTasks);
    }

    @FXML
    private void handleApplyFilters() {
        handleSearch(); // Ta sama logika co wyszukiwanie
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        projectFilterCombo.getSelectionModel().selectFirst(); // "Wszystkie projekty"
        teamFilterCombo.getSelectionModel().selectFirst(); // "Wszystkie zespoły"
        statusFilterCombo.setValue("Wszystkie");
        priorityFilterCombo.setValue("Wszystkie");
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);

        filteredTasks.setAll(allTasks);
        tasksTable.setItems(filteredTasks);
    }

    @FXML
    private void handleAddTask() {
        Dialog<Task> dialog = createTaskDialog(null);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            boolean success = taskDAO.insertTask(task);
            if (success) {
                // Jeśli zadanie ma przypisanego użytkownika, dodaj wpis w tabeli przypisań
                if (task.getAssignedTo() > 0) {
                    TaskAssignment assignment = new TaskAssignment();
                    assignment.setTaskId(task.getId());
                    assignment.setUserId(task.getAssignedTo());
                    assignmentDAO.insertTaskAssignment(assignment);
                }

                loadTasks();
                showInfo("Dodano nowe zadanie");
            } else {
                showError("Błąd", "Nie udało się dodać zadania");
            }
        });
    }

    @FXML
    private void handleEditTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie do edycji");
            return;
        }

        Dialog<Task> dialog = createTaskDialog(selectedTask);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            boolean success = taskDAO.updateTask(task);
            if (success && task.getAssignedTo() > 0) {
                // Uaktualnij przypisanie
                taskDAO.assignTask(task.getId(), task.getAssignedTo());

                loadTasks();
                // Jeśli aktualnie wybrane zadanie zostało zaktualizowane, odśwież szczegóły
                Task currentSelection = tasksTable.getSelectionModel().getSelectedItem();
                if (currentSelection != null && currentSelection.getId() == task.getId()) {
                    displayTaskDetails(task);
                }
                showInfo("Zaktualizowano zadanie");
            } else {
                showError("Błąd", "Nie udało się zaktualizować zadania");
            }
        });
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie do usunięcia");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć zadanie?");
        confirmDialog.setContentText("Zadanie: " + selectedTask.getTitle());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // W rzeczywistej implementacji usuń zadanie z bazy
            // Symulacja usuwania poprzez usunięcie z listy
            allTasks.remove(selectedTask);
            filteredTasks.remove(selectedTask);
            clearTaskDetails();

            showInfo("Zadanie zostało usunięte");
        }
    }

    @FXML
    private void handleAssignUser() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie, do którego chcesz przypisać użytkownika");
            return;
        }

        try {
            // Pobierz użytkowników z zespołu zadania
            List<User> teamMembers = new ArrayList<>();
            if (selectedTask.getTeamId() > 0) {
                teamMembers = teamDAO.getTeamMembers(selectedTask.getTeamId());
            }

            if (teamMembers.isEmpty()) {
                // Jeśli nie ma członków zespołu, pokaż wszystkich użytkowników
                teamMembers = userDAO.getAllUsers();
            }

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Przypisz użytkownika");
            dialog.setHeaderText("Wybierz użytkownika dla zadania: " + selectedTask.getTitle());

            ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setItems(FXCollections.observableArrayList(teamMembers));

            // Konwerter dla wyświetlania użytkowników
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Wybierz obecnego użytkownika, jeśli istnieje
            int currentAssignedUserId = selectedTask.getAssignedTo();
            if (currentAssignedUserId > 0) {
                for (User user : teamMembers) {
                    if (user.getId() == currentAssignedUserId) {
                        userComboBox.setValue(user);
                        break;
                    }
                }
            }

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Użytkownik:"), 0, 0);
            grid.add(userComboBox, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return userComboBox.getValue();
                }
                return null;
            });

            Optional<User> userResult = dialog.showAndWait();
            userResult.ifPresent(user -> {
                boolean success = taskDAO.assignTask(selectedTask.getId(), user.getId());
                if (success) {
                    // Aktualizuj dane w tabeli
                    selectedTask.setAssignedTo(user.getId());
                    selectedTask.setAssignedEmail(user.getEmail());

                    loadTasks();  // W rzeczywistej implementacji odświeżmy wszystkie zadania

                    // Odśwież widok szczegółów
                    displayTaskDetails(selectedTask);

                    showInfo("Użytkownik został przypisany do zadania");
                } else {
                    showError("Błąd", "Nie udało się przypisać użytkownika do zadania");
                }
            });

        } catch (Exception e) {
            showError("Błąd", "Nie udało się pobrać listy użytkowników: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeStatus() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie, którego status chcesz zmienić");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Zmień status");
        dialog.setHeaderText("Wybierz nowy status dla zadania: " + selectedTask.getTitle());

        ButtonType changeButtonType = new ButtonType("Zmień", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue(selectedTask.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Status:"), 0, 0);
        grid.add(statusComboBox, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return statusComboBox.getValue();
            }
            return null;
        });

        Optional<String> statusResult = dialog.showAndWait();
        statusResult.ifPresent(status -> {
            boolean success = taskDAO.updateTaskStatus(selectedTask.getId(), status);
            if (success) {
                // Aktualizuj dane w tabeli
                selectedTask.setStatus(status);

                tasksTable.refresh();
                displayTaskDetails(selectedTask);

                // Dodaj aktywność
                TaskActivity activity = new TaskActivity();
                activity.setTaskId(selectedTask.getId());
                activity.setUserId(1);  // Przykładowo, administrator o ID 1
                activity.setActivityType("Status");
                activity.setDescription("Zmieniono status na: " + status);

                boolean activityAdded = activityDAO.insertTaskActivity(activity);
                if (activityAdded) {
                    loadTaskActivities(selectedTask.getId());
                }

                showInfo("Status zadania został zmieniony");
            } else {
                showError("Błąd", "Nie udało się zmienić statusu zadania");
            }
        });
    }

@FXML
    private void handleRefresh() {
        loadTasks();

        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            loadTaskActivities(selectedTask.getId());
        }
    }

    private Dialog<Task> createTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Dodaj nowe zadanie" : "Edytuj zadanie");
        dialog.setHeaderText(task == null ? "Wprowadź dane nowego zadania" : "Edytuj dane zadania");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField();
        titleField.setPromptText("Tytuł zadania");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Opis zadania");
        descriptionArea.setPrefRowCount(3);

        // ComboBox dla projektu
        ComboBox<Project> projectComboBox = new ComboBox<>();
        try {
            List<Project> projects = projectDAO.getAllProjects();
            projectComboBox.setItems(FXCollections.observableArrayList(projects));

            // Konwerter dla wyświetlania projektów
            projectComboBox.setConverter(new StringConverter<Project>() {
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

        // ComboBox dla zespołu - będzie aktualizowany po wyborze projektu
        ComboBox<Team> teamComboBox = new ComboBox<>();
        teamComboBox.setConverter(new StringConverter<Team>() {
            @Override
            public String toString(Team team) {
                return team != null ? team.getTeamName() : "";
            }

            @Override
            public Team fromString(String string) {
                return null;
            }
        });

        // Aktualizacja zespołów po wyborze projektu
        projectComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    List<Team> teams = teamDAO.getAllTeams();
                    // Filtrowanie zespołów tylko do tych, które są przypisane do wybranego projektu
                    List<Team> projectTeams = teams.stream()
                            .filter(team -> team.getProjectId() == newVal.getId())
                            .toList();

                    teamComboBox.setItems(FXCollections.observableArrayList(projectTeams));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        // ComboBox dla statusu
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue("Nowe");

        // ComboBox dla priorytetu
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityComboBox.setValue("MEDIUM");

        // Pola dat
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // ComboBox dla przypisanego użytkownika - będzie aktualizowany po wyborze zespołu
        ComboBox<User> assigneeComboBox = new ComboBox<>();
        assigneeComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        // Aktualizacja użytkowników po wyborze zespołu
        teamComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    List<User> teamMembers = teamDAO.getTeamMembers(newVal.getId());
                    assigneeComboBox.setItems(FXCollections.observableArrayList(teamMembers));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Jeśli edytujemy istniejące zadanie, ustawiamy wartości
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusComboBox.setValue(task.getStatus());
            priorityComboBox.setValue(task.getPriority());

            // Daty wymagają konwersji
            if (task.getStartDate() != null) {
                try {
                    startDatePicker.setValue(LocalDate.parse(task.getStartDate()));
                } catch (Exception e) {
                    // Ignoruj błędy parsowania daty
                }
            }

            if (task.getEndDate() != null) {
                try {
                    endDatePicker.setValue(LocalDate.parse(task.getEndDate()));
                } catch (Exception e) {
                    // Ignoruj błędy parsowania daty
                }
            }

            // Ustawienie projektu
            int projectId = task.getProjectId();
            if (projectId > 0) {
                for (Project project : projectComboBox.getItems()) {
                    if (project.getId() == projectId) {
                        projectComboBox.setValue(project);
                        break;
                    }
                }
            }

            // Ustawienie zespołu
            int teamId = task.getTeamId();
            if (teamId > 0) {
                try {
                    List<Team> teams = teamDAO.getAllTeams();
                    for (Team team : teams) {
                        if (team.getId() == teamId) {
                            teamComboBox.setValue(team);
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Ustawienie przypisanego użytkownika
            int assignedTo = task.getAssignedTo();
            if (assignedTo > 0) {
                try {
                    List<User> users = userDAO.getAllUsers();
                    for (User user : users) {
                        if (user.getId() == assignedTo) {
                            assigneeComboBox.setValue(user);
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Dla nowego zadania, ustawiamy domyślne daty
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusWeeks(2));
        }

        // Dodanie elementów do siatki
        grid.add(new Label("Tytuł:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Projekt:"), 0, 1);
        grid.add(projectComboBox, 1, 1);
        grid.add(new Label("Zespół:"), 0, 2);
        grid.add(teamComboBox, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusComboBox, 1, 3);
        grid.add(new Label("Priorytet:"), 0, 4);
        grid.add(priorityComboBox, 1, 4);
        grid.add(new Label("Data rozpoczęcia:"), 0, 5);
        grid.add(startDatePicker, 1, 5);
        grid.add(new Label("Data zakończenia:"), 0, 6);
        grid.add(endDatePicker, 1, 6);
        grid.add(new Label("Przypisany do:"), 0, 7);
        grid.add(assigneeComboBox, 1, 7);
        grid.add(new Label("Opis:"), 0, 8);
        grid.add(descriptionArea, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Walidacja formularza
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Dodanie listenerów do pól formularza
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox,
                            startDatePicker, endDatePicker);
        });

        projectComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox,
                            startDatePicker, endDatePicker);
        });

        teamComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox,
                            startDatePicker, endDatePicker);
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox,
                            startDatePicker, endDatePicker);
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox,
                            startDatePicker, endDatePicker);
        });

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Task result = task != null ? task : new Task();
                result.setTitle(titleField.getText());
                result.setDescription(descriptionArea.getText());
                result.setStatus(statusComboBox.getValue());
                result.setPriority(priorityComboBox.getValue());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (startDatePicker.getValue() != null) {
                    result.setStartDate(startDatePicker.getValue().format(formatter));
                }

                if (endDatePicker.getValue() != null) {
                    result.setEndDate(endDatePicker.getValue().format(formatter));
                }

                Project selectedProject = projectComboBox.getValue();
                if (selectedProject != null) {
                    result.setProjectId(selectedProject.getId());
                }

                Team selectedTeam = teamComboBox.getValue();
                if (selectedTeam != null) {
                    result.setTeamId(selectedTeam.getId());
                    result.setTeamName(selectedTeam.getTeamName());
                }

                User selectedUser = assigneeComboBox.getValue();
                if (selectedUser != null) {
                    result.setAssignedTo(selectedUser.getId());
                    result.setAssignedEmail(selectedUser.getEmail());
                }

                return result;
            }
            return null;
        });

        return dialog;
    }

    private void validateTaskForm(Button saveButton, TextField titleField, ComboBox<Project> projectComboBox,
                                 ComboBox<Team> teamComboBox, DatePicker startDatePicker, DatePicker endDatePicker) {
        boolean titleValid = !titleField.getText().trim().isEmpty();
        boolean projectValid = projectComboBox.getValue() != null;
        boolean teamValid = teamComboBox.getValue() != null;
        boolean datesValid = startDatePicker.getValue() != null &&
                            endDatePicker.getValue() != null &&
                            !endDatePicker.getValue().isBefore(startDatePicker.getValue());

        saveButton.setDisable(!(titleValid && projectValid && teamValid && datesValid));
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
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