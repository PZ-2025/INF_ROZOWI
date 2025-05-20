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
import pl.rozowi.app.services.ActivityService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler odpowiedzialny za zarządzanie zadaniami w panelu administracyjnym.
 * Pozwala na wyświetlanie, dodawanie, edycję, usuwanie oraz filtrowanie zadań.
 * Zapewnia również funkcjonalność przypisywania użytkowników do zadań oraz zmianę ich statusów.
 */
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

    private final TaskDAO taskDAO = new TaskDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TaskActivityDAO activityDAO = new TaskActivityDAO();
    private final TaskAssignmentDAO assignmentDAO = new TaskAssignmentDAO();

    private ObservableList<Task> allTasks = FXCollections.observableArrayList();
    private ObservableList<Task> filteredTasks = FXCollections.observableArrayList();

    /**
     * Inicjalizuje kontroler. Ładuje zadania, konfiguruje tabelę oraz ustawia filtry.
     * Wywoływana automatycznie po załadowaniu pliku FXML.
     */
    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(
                tasksTable.getItems().indexOf(data.getValue()) + 1));
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

        setupFilters();

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayTaskDetails(newSelection);
            } else {
                clearTaskDetails();
            }
        });

        loadTasks();
    }

    /**
     * Konfiguruje dostępne filtry dla listy zadań.
     * Ustawia wartości domyślne dla filtrów projektów, zespołów, statusów i priorytetów.
     */
    private void setupFilters() {
        try {
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

            statusFilterCombo.getItems().addAll("Wszystkie", "Nowe", "W toku", "Zakończone");
            statusFilterCombo.setValue("Wszystkie");

            priorityFilterCombo.getItems().addAll("Wszystkie", "Niskie", "Średnie", "Wysokie");
            priorityFilterCombo.setValue("Wszystkie");
        } catch (Exception e) {
            showError("Błąd konfiguracji filtrów", e.getMessage());
        }
    }

    /**
     * Ładuje listę wszystkich zadań z bazy danych i wyświetla je w tabeli.
     */
    private void loadTasks() {
        try {
            List<Task> tasks = new ArrayList<>();

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

    /**
     * Wyświetla szczegółowe informacje o wybranym zadaniu.
     * @param task Zadanie, którego szczegóły mają zostać wyświetlone
     */
    private void displayTaskDetails(Task task) {
        if (task == null) {
            clearTaskDetails();
            return;
        }

        int orderNumber = filteredTasks.indexOf(task) + 1;
        detailId.setText(String.valueOf(orderNumber));
        detailTitle.setText(task.getTitle());

        try {
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

        detailLastUpdate.setText("2025-04-24 12:34:56");
    }

    /**
     * Czyści panel ze szczegółowymi informacjami o zadaniu.
     */
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
    }

    /**
     * Obsługuje akcję wyszukiwania zadań na podstawie wprowadzonego tekstu i ustawionych filtrów.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty() && projectFilterCombo.getValue().getId() == 0 &&
                teamFilterCombo.getValue().getId() == 0 && statusFilterCombo.getValue().equals("Wszystkie") &&
                priorityFilterCombo.getValue().equals("Wszystkie") && startDateFilter.getValue() == null &&
                endDateFilter.getValue() == null) {
            filteredTasks.setAll(allTasks);
            tasksTable.setItems(filteredTasks);
            return;
        }

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

            boolean matchesStartDate = true;
            boolean matchesEndDate = true;

            if (startDateFilter.getValue() != null) {
                try {
                    LocalDate taskStartDate = LocalDate.parse(task.getStartDate());
                    matchesStartDate = !taskStartDate.isBefore(startDateFilter.getValue());
                } catch (Exception e) {
                }
            }

            if (endDateFilter.getValue() != null) {
                try {
                    LocalDate taskEndDate = LocalDate.parse(task.getEndDate());
                    matchesEndDate = !taskEndDate.isAfter(endDateFilter.getValue());
                } catch (Exception e) {
                }
            }

            if (matchesSearch && matchesProject && matchesTeam && matchesStatus &&
                    matchesPriority && matchesStartDate && matchesEndDate) {
                result.add(task);
            }
        }

        filteredTasks.setAll(result);
        tasksTable.setItems(filteredTasks);
    }

    /**
     * Stosuje ustawione filtry do listy zadań.
     */
    @FXML
    private void handleApplyFilters() {
        handleSearch();
    }

    /**
     * Czyści wszystkie ustawione filtry i przywraca pełną listę zadań.
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        projectFilterCombo.getSelectionModel().selectFirst();
        teamFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.setValue("Wszystkie");
        priorityFilterCombo.setValue("Wszystkie");
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);

        filteredTasks.setAll(allTasks);
        tasksTable.setItems(filteredTasks);
    }

    /**
     * Obsługuje akcję dodawania nowego zadania.
     * Wyświetla okno dialogowe do wprowadzenia danych nowego zadania.
     */
    @FXML
    private void handleAddTask() {
        Dialog<Task> dialog = createTaskDialog(null);
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            boolean success = taskDAO.insertTask(task);
            if (success) {
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

    /**
     * Obsługuje akcję edycji istniejącego zadania.
     * Wyświetla okno dialogowe z danymi wybranego zadania do modyfikacji.
     */
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
                taskDAO.assignTask(task.getId(), task.getAssignedTo());

                loadTasks();
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

    /**
     * Obsługuje akcję usuwania wybranego zadania.
     * Wyświetla okno potwierdzenia przed usunięciem zadania.
     */
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
        confirmDialog.setContentText("Zadanie: " + selectedTask.getTitle() + "\n\nTa operacja jest nieodwracalna i usunie również wszystkie przypisania i aktywności zadania.");

        ButtonType anulujButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, anulujButtonType);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = taskDAO.deleteTask(selectedTask.getId());

                if (deleted) {
                    allTasks.remove(selectedTask);
                    filteredTasks.remove(selectedTask);
                    clearTaskDetails();

                    showInfo("Zadanie zostało pomyślnie usunięte z systemu");
                } else {
                    showError("Błąd", "Nie udało się usunąć zadania z bazy danych");
                }
            } catch (Exception ex) {
                showError("Błąd usuwania", "Wystąpił błąd podczas usuwania zadania: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Obsługuje akcję przypisywania użytkownika do wybranego zadania.
     * Wyświetla okno dialogowe z listą dostępnych użytkowników.
     */
    @FXML
    private void handleAssignUser() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showWarning("Wybierz zadanie, do którego chcesz przypisać użytkownika");
            return;
        }

        try {
            List<User> teamMembers = new ArrayList<>();
            if (selectedTask.getTeamId() > 0) {
                teamMembers = teamDAO.getTeamMembers(selectedTask.getTeamId());
            }

            if (teamMembers.isEmpty()) {
                teamMembers = userDAO.getAllUsers();
            }

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Przypisz użytkownika");
            dialog.setHeaderText("Wybierz użytkownika dla zadania: " + selectedTask.getTitle());

            ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, cancelButtonType);

            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setItems(FXCollections.observableArrayList(teamMembers));

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
                int oldUserId = selectedTask.getAssignedTo();

                boolean success = taskDAO.assignTask(selectedTask.getId(), user.getId());
                if (success) {
                    selectedTask.setAssignedTo(user.getId());
                    selectedTask.setAssignedEmail(user.getEmail());

                    loadTasks();
                    displayTaskDetails(selectedTask);

                    ActivityService.logAssignment(
                            selectedTask.getId(),
                            selectedTask.getTitle(),
                            oldUserId,
                            user.getId()
                    );

                    showInfo("Użytkownik został przypisany do zadania");
                } else {
                    showError("Błąd", "Nie udało się przypisać użytkownika do zadania");
                }
            });

        } catch (Exception e) {
            showError("Błąd", "Nie udało się pobrać listy użytkowników: " + e.getMessage());
        }
    }

    /**
     * Obsługuje akcję zmiany statusu wybranego zadania.
     * Wyświetla okno dialogowe z dostępnymi statusami do wyboru.
     */
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
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, cancelButtonType);

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
        statusResult.ifPresent(newStatus -> {
            String oldStatus = selectedTask.getStatus();

            boolean success = taskDAO.updateTaskStatus(selectedTask.getId(), newStatus);
            if (success) {
                selectedTask.setStatus(newStatus);

                tasksTable.refresh();
                displayTaskDetails(selectedTask);

                ActivityService.logStatusChange(
                        selectedTask.getId(),
                        selectedTask.getTitle(),
                        oldStatus,
                        newStatus
                );

                showInfo("Status zadania został zmieniony");
            } else {
                showError("Błąd", "Nie udało się zmienić statusu zadania");
            }
        });
    }

    /**
     * Odświeża listę zadań, ponownie ładując dane z bazy danych.
     */
    @FXML
    private void handleRefresh() {
        loadTasks();

        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            displayTaskDetails(selectedTask);
        }
    }

    /**
     * Tworzy i konfiguruje okno dialogowe do dodawania/edycji zadania.
     * @param task Zadanie do edycji lub null w przypadku dodawania nowego zadania
     * @return Skonfigurowane okno dialogowe
     */
    private Dialog<Task> createTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Dodaj nowe zadanie" : "Edytuj zadanie");
        dialog.setHeaderText(task == null ? "Wprowadź dane nowego zadania" : "Edytuj dane zadania");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField();
        titleField.setPromptText("Tytuł zadania");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Opis zadania");
        descriptionArea.setPrefRowCount(3);

        ComboBox<Project> projectComboBox = new ComboBox<>();
        try {
            List<Project> projects = projectDAO.getAllProjects();
            projectComboBox.setItems(FXCollections.observableArrayList(projects));

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

        projectComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    List<Team> teams = teamDAO.getAllTeams();
                    List<Team> projectTeams = teams.stream()
                            .filter(team -> team.getProjectId() == newVal.getId())
                            .toList();

                    teamComboBox.setItems(FXCollections.observableArrayList(projectTeams));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue("Nowe");

        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll("Niskie", "Średnie", "Wysokie");
        priorityComboBox.setValue("Średnie");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

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

        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusComboBox.setValue(task.getStatus());
            priorityComboBox.setValue(task.getPriority());

            if (task.getStartDate() != null) {
                try {
                    startDatePicker.setValue(LocalDate.parse(task.getStartDate()));
                } catch (Exception e) {
                }
            }

            if (task.getEndDate() != null) {
                try {
                    endDatePicker.setValue(LocalDate.parse(task.getEndDate()));
                } catch (Exception e) {
                }
            }

            int projectId = task.getProjectId();
            if (projectId > 0) {
                for (Project project : projectComboBox.getItems()) {
                    if (project.getId() == projectId) {
                        projectComboBox.setValue(project);
                        break;
                    }
                }
            }

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
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusWeeks(2));
        }

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

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

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

    /**
     * Sprawdza poprawność danych w formularzu zadania.
     * @param saveButton Przycisk zapisu, który ma być włączony/wyłączony
     * @param titleField Pole tytułu zadania
     * @param projectComboBox ComboBox z listą projektów
     * @param teamComboBox ComboBox z listą zespołów
     * @param startDatePicker Data rozpoczęcia zadania
     * @param endDatePicker Data zakończenia zadania
     */
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

    /**
     * Wyświetla okno dialogowe z informacją.
     * @param message Treść wiadomości do wyświetlenia
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla okno dialogowe z ostrzeżeniem.
     * @param message Treść ostrzeżenia do wyświetlenia
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wyświetla okno dialogowe z błędem.
     * @param title Tytuł okna dialogowego
     * @param message Treść błędu do wyświetlenia
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}