package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pl.rozowi.app.dao.*;
import pl.rozowi.app.models.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // Filtry zaawansowane
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

    // Szczegóły zadania
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

    // Historia aktywności
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

    private TaskDAO taskDAO = new TaskDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private UserDAO userDAO = new UserDAO();
    private TaskActivityDAO activityDAO = new TaskActivityDAO();

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private ObservableList<TaskActivity> taskActivities = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Konfiguracja kolumn tabeli zadań
        setupTasksTableColumns();

        // Konfiguracja kolumn tabeli aktywności
        setupActivityTableColumns();

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

    private void setupTasksTableColumns() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colProject.setCellValueFactory(data -> new SimpleStringProperty(getProjectName(data.getValue().getProjectId())));
        colTeam.setCellValueFactory(data -> data.getValue().teamNameProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colAssignee.setCellValueFactory(data -> data.getValue().assignedEmailProperty());
    }

    private void setupActivityTableColumns() {
        colActivityDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getActivityDate() != null ?
                data.getValue().getActivityDate().toString() : ""
        ));
        colActivityUser.setCellValueFactory(data -> new SimpleStringProperty(getUserName(data.getValue().getUserId())));
        colActivityType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActivityType()));
        colActivityDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
    }

    private void setupFilters() {
        // Status i priorytet
        statusFilterCombo.getItems().addAll("Wszystkie", "Nowe", "W toku", "Zakończone");
        statusFilterCombo.getSelectionModel().selectFirst();

        priorityFilterCombo.getItems().addAll("Wszystkie", "LOW", "MEDIUM", "HIGH");
        priorityFilterCombo.getSelectionModel().selectFirst();

        // Projekty
        try {
            List<Project> projects = projectDAO.getAllProjects();

            Project allProjects = new Project();
            allProjects.setId(0);
            allProjects.setName("Wszystkie projekty");

            ObservableList<Project> projectItems = FXCollections.observableArrayList();
            projectItems.add(allProjects);
            projectItems.addAll(projects);

            projectFilterCombo.setItems(projectItems);
            projectFilterCombo.setValue(allProjects);

            projectFilterCombo.setConverter(new javafx.util.StringConverter<Project>() {
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
            showError("Błąd", "Nie udało się załadować listy projektów: " + e.getMessage());
        }

        // Zespoły
        try {
            List<Team> teams = teamDAO.getAllTeams();

            Team allTeams = new Team();
            allTeams.setId(0);
            allTeams.setTeamName("Wszystkie zespoły");

            ObservableList<Team> teamItems = FXCollections.observableArrayList();
            teamItems.add(allTeams);
            teamItems.addAll(teams);

            teamFilterCombo.setItems(teamItems);
            teamFilterCombo.setValue(allTeams);

            teamFilterCombo.setConverter(new javafx.util.StringConverter<Team>() {
                @Override
                public String toString(Team team) {
                    return team != null ? team.getTeamName() : "";
                }

                @Override
                public Team fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showError("Błąd", "Nie udało się załadować listy zespołów: " + e.getMessage());
        }
    }

    private void loadTasks() {
        try {
            // Pobieranie wszystkich zadań - w rzeczywistej implementacji może być potrzebne paginowanie
            List<Task> tasks = new ArrayList<>();

            // Symulacja dla testów
            for (int i = 1; i <= 10; i++) {
                Task task = new Task();
                task.setId(i);
                task.setTitle("Zadanie testowe " + i);
                task.setProjectId((i % 3) + 1);
                task.setTeamId((i % 5) + 1);
                task.setTeamName("Zespół " + ((i % 5) + 1));
                task.setStatus(i % 3 == 0 ? "Zakończone" : (i % 3 == 1 ? "Nowe" : "W toku"));
                task.setPriority(i % 3 == 0 ? "LOW" : (i % 3 == 1 ? "MEDIUM" : "HIGH"));
                task.setStartDate(LocalDate.now().minusDays(i).toString());
                task.setEndDate(LocalDate.now().plusDays(10 - i).toString());
                task.setAssignedEmail("user" + i + "@example.com");
                task.setDescription("Opis zadania testowego numer " + i);
                tasks.add(task);
            }

            allTasks.setAll(tasks);
            tasksTable.setItems(allTasks);
        } catch (Exception e) {
            showError("Błąd", "Nie udało się załadować zadań: " + e.getMessage());
        }
    }

    private void loadTaskActivities(int taskId) {
        try {
            List<TaskActivity> activities = activityDAO.getActivitiesByTaskId(taskId);
            taskActivities.setAll(activities);
            activityTable.setItems(taskActivities);
        } catch (Exception e) {
            showError("Błąd", "Nie udało się załadować historii aktywności: " + e.getMessage());
        }
    }

    private void displayTaskDetails(Task task) {
        if (task == null) {
            clearTaskDetails();
            return;
        }

        detailId.setText(String.valueOf(task.getId()));
        detailTitle.setText(task.getTitle());
        detailProject.setText(getProjectName(task.getProjectId()));
        detailTeam.setText(task.getTeamName());
        detailStatus.setText(task.getStatus());
        detailPriority.setText(task.getPriority());
        detailStartDate.setText(task.getStartDate());
        detailEndDate.setText(task.getEndDate());
        detailAssignee.setText(task.getAssignedEmail());
        detailDescription.setText(task.getDescription());

        // W rzeczywistej implementacji pobierz datę ostatniej aktualizacji
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

    private String getProjectName(int projectId) {
        try {
            // W rzeczywistej implementacji pobierz nazwę projektu z bazy
            return "Projekt " + projectId;
        } catch (Exception e) {
            return "Nieznany";
        }
    }

    private String getUserName(int userId) {
        try {
            // W rzeczywistej implementacji pobierz nazwę użytkownika z bazy
            return "Użytkownik " + userId;
        } catch (Exception e) {
            return "Nieznany";
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            tasksTable.setItems(allTasks);
            return;
        }

        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task task : allTasks) {
            if (String.valueOf(task.getId()).contains(searchText) ||
                task.getTitle().toLowerCase().contains(searchText) ||
                task.getDescription().toLowerCase().contains(searchText) ||
                task.getStatus().toLowerCase().contains(searchText) ||
                task.getPriority().toLowerCase().contains(searchText) ||
                getProjectName(task.getProjectId()).toLowerCase().contains(searchText) ||
                task.getTeamName().toLowerCase().contains(searchText) ||
                task.getAssignedEmail().toLowerCase().contains(searchText)) {
                filtered.add(task);
            }
        }
        tasksTable.setItems(filtered);
    }

    @FXML
    private void handleApplyFilters() {
        ObservableList<Task> filtered = FXCollections.observableArrayList(allTasks);

        // Filtr projektu
        Project selectedProject = projectFilterCombo.getValue();
        if (selectedProject != null && selectedProject.getId() != 0) {
            filtered = filtered.filtered(task -> task.getProjectId() == selectedProject.getId());
        }

        // Filtr zespołu
        Team selectedTeam = teamFilterCombo.getValue();
        if (selectedTeam != null && selectedTeam.getId() != 0) {
            filtered = filtered.filtered(task -> task.getTeamId() == selectedTeam.getId());
        }

        // Filtr statusu
        String selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Wszystkie")) {
            filtered = filtered.filtered(task -> task.getStatus().equals(selectedStatus));
        }

        // Filtr priorytetu
        String selectedPriority = priorityFilterCombo.getValue();
        if (selectedPriority != null && !selectedPriority.equals("Wszystkie")) {
            filtered = filtered.filtered(task -> task.getPriority().equals(selectedPriority));
        }

        // Filtr daty początkowej
        LocalDate startDate = startDateFilter.getValue();
        if (startDate != null) {
            filtered = filtered.filtered(task -> {
                try {
                    LocalDate taskDate = LocalDate.parse(task.getStartDate());
                    return !taskDate.isBefore(startDate);
                } catch (Exception e) {
                    return true;
                }
            });
        }

        // Filtr daty końcowej
        LocalDate endDate = endDateFilter.getValue();
        if (endDate != null) {
            filtered = filtered.filtered(task -> {
                try {
                    LocalDate taskDate = LocalDate.parse(task.getEndDate());
                    return !taskDate.isAfter(endDate);
                } catch (Exception e) {
                    return true;
                }
            });
        }

        tasksTable.setItems(filtered);
    }

    @FXML
    private void handleClearFilters() {
        projectFilterCombo.getSelectionModel().selectFirst();
        teamFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.getSelectionModel().selectFirst();
        priorityFilterCombo.getSelectionModel().selectFirst();
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);

        tasksTable.setItems(allTasks);
    }

    @FXML
    private void handleAddTask() {
        Dialog<Task> dialog = createTaskDialog(null);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            boolean success = taskDAO.insertTask(task);
            if (success) {
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
            if (success) {
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
            // boolean success = taskDAO.deleteTask(selectedTask.getId());

            // Symulacja
            allTasks.remove(selectedTask);
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
            // Pobierz użytkowników z bazy (w rzeczywistej implementacji filtruj według zespołu)
            // List<User> users = userDAO.getAllUsers();

            // Symulacja
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                User user = new User();
                user.setId(i);
                user.setName("Imię" + i);
                user.setLastName("Nazwisko" + i);
                user.setEmail("user" + i + "@example.com");
                users.add(user);
            }

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Przypisz użytkownika");
            dialog.setHeaderText("Wybierz użytkownika dla zadania: " + selectedTask.getTitle());

            ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setItems(FXCollections.observableArrayList(users));

            // Konwerter dla wyświetlania użytkowników
            userComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

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

                    loadTasks();  // W rzeczywistej implementacji, tylko zaktualizuj konkretne zadanie
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
                activity.setUserId(1);  // Aktualnie zalogowany użytkownik
                activity.setActivityType("Status");
                activity.setDescription("Zmieniono status na: " + status);

                activityDAO.insertTaskActivity(activity);
                loadTaskActivities(selectedTask.getId());

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

            projectComboBox.setConverter(new javafx.util.StringConverter<Project>() {
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
            showError("Błąd", "Nie udało się załadować listy projektów: " + e.getMessage());
        }

        // ComboBox dla zespołu
        ComboBox<Team> teamComboBox = new ComboBox<>();
        try {
            List<Team> teams = teamDAO.getAllTeams();
            teamComboBox.setItems(FXCollections.observableArrayList(teams));

            teamComboBox.setConverter(new javafx.util.StringConverter<Team>() {
                @Override
                public String toString(Team team) {
                    return team != null ? team.getTeamName() : "";
                }

                @Override
                public Team fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showError("Błąd", "Nie udało się załadować listy zespołów: " + e.getMessage());
        }

        // Pozostałe pola
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue("Nowe");

        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityComboBox.setValue("MEDIUM");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Dla edycji, ustaw istniejące wartości
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusComboBox.setValue(task.getStatus());
            priorityComboBox.setValue(task.getPriority());

            // Ustawienie projektu
            try {
                int projectId = task.getProjectId();
                projectComboBox.getItems().stream()
                    .filter(p -> p.getId() == projectId)
                    .findFirst()
                    .ifPresent(projectComboBox::setValue);
            } catch (Exception e) {
                System.err.println("Błąd podczas ustawiania projektu: " + e.getMessage());
            }

            // Ustawienie zespołu
            try {
                int teamId = task.getTeamId();
                teamComboBox.getItems().stream()
                    .filter(t -> t.getId() == teamId)
                    .findFirst()
                    .ifPresent(teamComboBox::setValue);
            } catch (Exception e) {
                System.err.println("Błąd podczas ustawiania zespołu: " + e.getMessage());
            }

            // Ustawienie dat
            try {
                if (task.getStartDate() != null) {
                    startDatePicker.setValue(LocalDate.parse(task.getStartDate()));
                }

                if (task.getEndDate() != null) {
                    endDatePicker.setValue(LocalDate.parse(task.getEndDate()));
                }
            } catch (Exception e) {
                System.err.println("Błąd podczas ustawiania dat: " + e.getMessage());
            }
        } else {
            // Dla nowego zadania, ustaw dzisiejszą datę i termin za 2 tygodnie
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusWeeks(2));
        }

        // Dodanie pól do formularza
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
        grid.add(new Label("Opis:"), 0, 7);
        grid.add(descriptionArea, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Walidacja
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Walidacja pól
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox, startDatePicker, endDatePicker);
        });

        projectComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox, startDatePicker, endDatePicker);
        });

        teamComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox, startDatePicker, endDatePicker);
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox, startDatePicker, endDatePicker);
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, projectComboBox, teamComboBox, startDatePicker, endDatePicker);
        });

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Task result = task != null ? task : new Task();
                result.setTitle(titleField.getText());
                result.setDescription(descriptionArea.getText());
                result.setStatus(statusComboBox.getValue());
                result.setPriority(priorityComboBox.getValue());

                Project selectedProject = projectComboBox.getValue();
                if (selectedProject != null) {
                    result.setProjectId(selectedProject.getId());
                }

                Team selectedTeam = teamComboBox.getValue();
                if (selectedTeam != null) {
                    result.setTeamId(selectedTeam.getId());
                    result.setTeamName(selectedTeam.getTeamName());
                }

                if (startDatePicker.getValue() != null) {
                    result.setStartDate(startDatePicker.getValue().toString());
                }

                if (endDatePicker.getValue() != null) {
                    result.setEndDate(endDatePicker.getValue().toString());
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