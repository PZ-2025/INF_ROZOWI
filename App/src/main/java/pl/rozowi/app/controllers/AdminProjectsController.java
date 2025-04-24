package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
import java.util.List;
import java.util.Optional;

public class AdminProjectsController {

    @FXML
    private TableView<Project> projectsTable;
    @FXML
    private TableColumn<Project, Integer> colId;
    @FXML
    private TableColumn<Project, String> colName;
    @FXML
    private TableColumn<Project, String> colDescription;
    @FXML
    private TableColumn<Project, LocalDate> colStartDate;
    @FXML
    private TableColumn<Project, LocalDate> colEndDate;
    @FXML
    private TableColumn<Project, String> colManager;
    @FXML
    private TableColumn<Project, String> colStatus;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<Team> teamsTable;
    @FXML
    private TableColumn<Team, Integer> colTeamId;
    @FXML
    private TableColumn<Team, String> colTeamName;
    @FXML
    private TableColumn<Team, String> colTeamLeader;
    @FXML
    private TableColumn<Team, Integer> colMembersCount;

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Integer> colTaskId;
    @FXML
    private TableColumn<Task, String> colTaskTitle;
    @FXML
    private TableColumn<Task, String> colTaskStatus;
    @FXML
    private TableColumn<Task, String> colTaskPriority;
    @FXML
    private TableColumn<Task, String> colTaskDeadline;
    @FXML
    private TableColumn<Task, String> colTaskAssignee;

    private ProjectDAO projectDAO = new ProjectDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private TaskDAO taskDAO = new TaskDAO();
    private UserDAO userDAO = new UserDAO();

    private ObservableList<Project> allProjects = FXCollections.observableArrayList();
    private ObservableList<Team> projectTeams = FXCollections.observableArrayList();
    private ObservableList<Task> projectTasks = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Konfiguracja kolumn tabeli projektów
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colManager.setCellValueFactory(data -> {
            int managerId = data.getValue().getManagerId();
            String managerName = "Brak";
            try {
                // Tutaj powinno być pobieranie nazwy kierownika z bazy
                // User manager = userDAO.getUserById(managerId);
                // managerName = manager.getName() + " " + manager.getLastName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(managerName);
        });
        colStatus.setCellValueFactory(data -> {
            LocalDate startDate = data.getValue().getStartDate();
            LocalDate endDate = data.getValue().getEndDate();
            LocalDate now = LocalDate.now();

            if (startDate == null || endDate == null) {
                return new SimpleStringProperty("Nieznany");
            }

            if (now.isBefore(startDate)) {
                return new SimpleStringProperty("Planowany");
            } else if (now.isAfter(endDate)) {
                return new SimpleStringProperty("Zakończony");
            } else {
                return new SimpleStringProperty("W trakcie");
            }
        });

        // Konfiguracja kolumn tabeli zespołów
        colTeamId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTeamName.setCellValueFactory(data -> data.getValue().teamNameProperty());
        colTeamLeader.setCellValueFactory(data -> new SimpleStringProperty("Nieznany")); // To wymaga implementacji
        colMembersCount.setCellValueFactory(data -> new SimpleObjectProperty<>(0)); // To wymaga implementacji

        // Konfiguracja kolumn tabeli zadań
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTaskTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colTaskStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colTaskPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colTaskDeadline.setCellValueFactory(data -> data.getValue().endDateProperty());
        colTaskAssignee.setCellValueFactory(data -> data.getValue().assignedEmailProperty());

        // Obsługa wyboru projektu - aktualizacja zespołów i zadań
        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProjectTeams(newSelection.getId());
                loadProjectTasks(newSelection.getId());
            } else {
                projectTeams.clear();
                projectTasks.clear();
            }
        });

        // Wczytaj projekty na start
        loadProjects();
    }

    private void loadProjects() {
        try {
            List<Project> projects = projectDAO.getAllProjects();
            allProjects.setAll(projects);
            projectsTable.setItems(allProjects);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania projektów", e.getMessage());
        }
    }

    private void loadProjectTeams(int projectId) {
        try {
            // Tutaj powinno być pobieranie zespołów przypisanych do projektu
            // List<Team> teams = teamDAO.getTeamsByProjectId(projectId);
            // projectTeams.setAll(teams);

            // Tymczasowe rozwiązanie
            projectTeams.clear();
            teamsTable.setItems(projectTeams);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania zespołów", e.getMessage());
        }
    }

    private void loadProjectTasks(int projectId) {
        try {
            List<Task> tasks = taskDAO.getTasksByProjectId(projectId);
            projectTasks.setAll(tasks);
            tasksTable.setItems(projectTasks);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania zadań", e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            projectsTable.setItems(allProjects);
            return;
        }

        ObservableList<Project> filtered = FXCollections.observableArrayList();
        for (Project project : allProjects) {
            if (String.valueOf(project.getId()).contains(searchText) ||
                project.getName().toLowerCase().contains(searchText) ||
                project.getDescription().toLowerCase().contains(searchText)) {
                filtered.add(project);
            }
        }
        projectsTable.setItems(filtered);
    }

    @FXML
    private void handleAddProject() {
        Dialog<Project> dialog = createProjectDialog(null);
        Optional<Project> result = dialog.showAndWait();

        result.ifPresent(project -> {
            try {
                boolean success = projectDAO.insertProject(project);
                if (success) {
                    loadProjects();
                    showInfo("Dodano nowy projekt");
                } else {
                    showError("Błąd", "Nie udało się dodać projektu");
                }
            } catch (SQLException e) {
                showError("Błąd SQL", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt do edycji");
            return;
        }

        Dialog<Project> dialog = createProjectDialog(selectedProject);
        Optional<Project> result = dialog.showAndWait();

        result.ifPresent(project -> {
            boolean success = projectDAO.updateProject(project);
            if (success) {
                loadProjects();
                showInfo("Zaktualizowano projekt");
            } else {
                showError("Błąd", "Nie udało się zaktualizować projektu");
            }
        });
    }

    @FXML
    private void handleDeleteProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt do usunięcia");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć projekt?");
        confirmDialog.setContentText("Projekt: " + selectedProject.getName());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Tutaj powinna być implementacja usuwania projektu
            // boolean success = projectDAO.deleteProject(selectedProject.getId());

            // Tymczasowe symulowane usunięcie
            allProjects.remove(selectedProject);
            projectTeams.clear();
            projectTasks.clear();

            showInfo("Projekt został usunięty");
        }
    }

    @FXML
    private void handleAssignManager() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt, aby przypisać kierownika");
            return;
        }

        try {
            List<User> managers = userDAO.getAllManagers();
            if (managers.isEmpty()) {
                showWarning("Brak dostępnych kierowników w systemie");
                return;
            }

            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Przypisz kierownika");
            dialog.setHeaderText("Wybierz kierownika dla projektu: " + selectedProject.getName());

            ButtonType assignButtonType = new ButtonType("Przypisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            ComboBox<User> managerComboBox = new ComboBox<>();
            managerComboBox.setItems(FXCollections.observableArrayList(managers));

            // Konwerter dla wyświetlania nazw kierowników
            managerComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Wybierz obecnego kierownika, jeśli istnieje
            int currentManagerId = selectedProject.getManagerId();
            if (currentManagerId > 0) {
                for (User manager : managers) {
                    if (manager.getId() == currentManagerId) {
                        managerComboBox.setValue(manager);
                        break;
                    }
                }
            }

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Kierownik:"), 0, 0);
            grid.add(managerComboBox, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return managerComboBox.getValue();
                }
                return null;
            });

            Optional<User> managerResult = dialog.showAndWait();
            managerResult.ifPresent(manager -> {
                selectedProject.setManagerId(manager.getId());
                boolean success = projectDAO.updateProject(selectedProject);
                if (success) {
                    loadProjects();
                    showInfo("Kierownik został przypisany do projektu");
                } else {
                    showError("Błąd", "Nie udało się przypisać kierownika");
                }
            });

        } catch (Exception e) {
            showError("Błąd podczas pobierania kierowników", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadProjects();

        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            loadProjectTeams(selectedProject.getId());
            loadProjectTasks(selectedProject.getId());
        }
    }

    @FXML
    private void handleAddTeam() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt, do którego chcesz dodać zespół");
            return;
        }

        try {
            List<Team> availableTeams;
            try {
                availableTeams = teamDAO.getAllTeams();
                // Filtruj zespoły, które nie są już przypisane do projektu
                for (Team team : projectTeams) {
                    availableTeams.removeIf(t -> t.getId() == team.getId());
                }
            } catch (SQLException e) {
                showError("Błąd SQL", e.getMessage());
                return;
            }

            if (availableTeams.isEmpty()) {
                showWarning("Brak dostępnych zespołów do przypisania");
                return;
            }

            Dialog<Team> dialog = new Dialog<>();
            dialog.setTitle("Dodaj zespół do projektu");
            dialog.setHeaderText("Wybierz zespół dla projektu: " + selectedProject.getName());

            ButtonType addButtonType = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            ComboBox<Team> teamComboBox = new ComboBox<>();
            teamComboBox.setItems(FXCollections.observableArrayList(availableTeams));

            // Konwerter dla wyświetlania nazw zespołów
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

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Zespół:"), 0, 0);
            grid.add(teamComboBox, 1, 0);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    return teamComboBox.getValue();
                }
                return null;
            });

            Optional<Team> teamResult = dialog.showAndWait();
            teamResult.ifPresent(team -> {
                // Przypisz projekt do zespołu
                team.setProjectId(selectedProject.getId());
                try {
                    boolean success = teamDAO.updateTeam(team);
                    if (success) {
                        loadProjectTeams(selectedProject.getId());
                        showInfo("Zespół został dodany do projektu");
                    } else {
                        showError("Błąd", "Nie udało się dodać zespołu do projektu");
                    }
                } catch (SQLException e) {
                    showError("Błąd SQL", e.getMessage());
                }
            });

        } catch (Exception e) {
            showError("Błąd", e.getMessage());
        }
    }

    @FXML
    private void handleRemoveTeam() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            showWarning("Wybierz projekt");
            return;
        }

        if (selectedTeam == null) {
            showWarning("Wybierz zespół do usunięcia z projektu");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć zespół z projektu?");
        confirmDialog.setContentText("Zespół: " + selectedTeam.getTeamName());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Usuń przypisanie zespołu do projektu
            selectedTeam.setProjectId(0);  // 0 oznacza brak przypisanego projektu
            try {
                boolean success = teamDAO.updateTeam(selectedTeam);
                if (success) {
                    loadProjectTeams(selectedProject.getId());
                    showInfo("Zespół został usunięty z projektu");
                } else {
                    showError("Błąd", "Nie udało się usunąć zespołu z projektu");
                }
            } catch (SQLException e) {
                showError("Błąd SQL", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddTask() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt, do którego chcesz dodać zadanie");
            return;
        }

        Dialog<Task> dialog = createTaskDialog(null, selectedProject.getId());
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            boolean success = taskDAO.insertTask(task);
            if (success) {
                loadProjectTasks(selectedProject.getId());
                showInfo("Dodano nowe zadanie do projektu");
            } else {
                showError("Błąd", "Nie udało się dodać zadania");
            }
        });
    }

    @FXML
    private void handleEditTask() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            showWarning("Wybierz projekt");
            return;
        }

        if (selectedTask == null) {
            showWarning("Wybierz zadanie do edycji");
            return;
        }

        Dialog<Task> dialog = createTaskDialog(selectedTask, selectedProject.getId());
        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(task -> {
            // W rzeczywistej implementacji trzeba by zaktualizować zadanie w bazie
            // Tutaj uproszczona symulacja
            boolean success = taskDAO.updateTask(task);
            if (success) {
                loadProjectTasks(selectedProject.getId());
                showInfo("Zaktualizowano zadanie");
            } else {
                showError("Błąd", "Nie udało się zaktualizować zadania");
            }
        });
    }

    @FXML
    private void handleRemoveTask() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            showWarning("Wybierz projekt");
            return;
        }

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
            // Tutaj powinna być implementacja usuwania zadania
            // boolean success = taskDAO.deleteTask(selectedTask.getId());

            // Tymczasowe symulowane usunięcie
            projectTasks.remove(selectedTask);

            showInfo("Zadanie zostało usunięte");
        }
    }

    private Dialog<Project> createProjectDialog(Project project) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle(project == null ? "Dodaj nowy projekt" : "Edytuj projekt");
        dialog.setHeaderText(project == null ? "Wprowadź dane nowego projektu" : "Edytuj dane projektu");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa projektu");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Opis projektu");
        descriptionArea.setPrefRowCount(3);

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Jeśli edytujemy istniejący projekt, ustawiamy wartości
        if (project != null) {
            nameField.setText(project.getName());
            descriptionArea.setText(project.getDescription());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());
        }

        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Opis:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Data rozpoczęcia:"), 0, 2);
        grid.add(startDatePicker, 1, 2);
        grid.add(new Label("Data zakończenia:"), 0, 3);
        grid.add(endDatePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Walidacja danych przed zapisem
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Dodanie listenerów do pól formularza do walidacji
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateProjectForm(saveButton, nameField, startDatePicker, endDatePicker);
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateProjectForm(saveButton, nameField, startDatePicker, endDatePicker);
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateProjectForm(saveButton, nameField, startDatePicker, endDatePicker);
        });

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Project result = project != null ? project : new Project();
                result.setName(nameField.getText());
                result.setDescription(descriptionArea.getText());
                result.setStartDate(startDatePicker.getValue());
                result.setEndDate(endDatePicker.getValue());

                // Przypisz kierownika projektu, jeśli to nowy projekt
                if (project == null) {
                    // Domyślnie brak przypisanego kierownika
                    result.setManagerId(0);
                }

                return result;
            }
            return null;
        });

        return dialog;
    }

    private Dialog<Task> createTaskDialog(Task task, int projectId) {
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

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Nowe", "W toku", "Zakończone");
        statusComboBox.setValue("Nowe");

        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        priorityComboBox.setValue("MEDIUM");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Pobranie zespołów dla projektu
        ObservableList<Team> teams = FXCollections.observableArrayList();
        try {
            // W rzeczywistej implementacji pobieraj zespoły z bazy
            // teams.addAll(teamDAO.getTeamsByProjectId(projectId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ComboBox<Team> teamComboBox = new ComboBox<>(teams);
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

        // Pobranie użytkowników do przypisania - w rzeczywistej implementacji to powinni być członkowie zespołu
        ObservableList<User> users = FXCollections.observableArrayList();
        try {
            // TODO: Pobierz użytkowników z bazy
        } catch (Exception e) {
            e.printStackTrace();
        }

        ComboBox<User> assigneeComboBox = new ComboBox<>(users);
        assigneeComboBox.setConverter(new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getName() + " " + user.getLastName() : "";
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });

        // Jeśli edytujemy istniejące zadanie, ustawiamy wartości
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusComboBox.setValue(task.getStatus());
            priorityComboBox.setValue(task.getPriority());

            // Daty i inne pola wymagają konwersji
            if (task.getStartDate() != null) {
                startDatePicker.setValue(LocalDate.parse(task.getStartDate()));
            }

            if (task.getEndDate() != null) {
                endDatePicker.setValue(LocalDate.parse(task.getEndDate()));
            }

            // TODO: Ustaw zespół i przypisanego użytkownika, jeśli są znane
        }

        grid.add(new Label("Tytuł:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Opis:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusComboBox, 1, 2);
        grid.add(new Label("Priorytet:"), 0, 3);
        grid.add(priorityComboBox, 1, 3);
        grid.add(new Label("Data rozpoczęcia:"), 0, 4);
        grid.add(startDatePicker, 1, 4);
        grid.add(new Label("Data zakończenia:"), 0, 5);
        grid.add(endDatePicker, 1, 5);
        grid.add(new Label("Zespół:"), 0, 6);
        grid.add(teamComboBox, 1, 6);
        grid.add(new Label("Przypisany do:"), 0, 7);
        grid.add(assigneeComboBox, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Walidacja danych przed zapisem
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Dodanie listenerów do pól formularza do walidacji
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, startDatePicker, endDatePicker);
        });

        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, startDatePicker, endDatePicker);
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateTaskForm(saveButton, titleField, startDatePicker, endDatePicker);
        });

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Task result = task != null ? task : new Task();
                result.setTitle(titleField.getText());
                result.setDescription(descriptionArea.getText());
                result.setStatus(statusComboBox.getValue());
                result.setPriority(priorityComboBox.getValue());

                // Konwersja i ustawienie dat
                if (startDatePicker.getValue() != null) {
                    result.setStartDate(startDatePicker.getValue().toString());
                }

                if (endDatePicker.getValue() != null) {
                    result.setEndDate(endDatePicker.getValue().toString());
                }

                // Ustawienie projektu i zespołu
                result.setProjectId(projectId);

                Team selectedTeam = teamComboBox.getValue();
                if (selectedTeam != null) {
                    result.setTeamId(selectedTeam.getId());
                }

                // Ustawienie przypisanego użytkownika (w rzeczywistej implementacji)
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

    private void validateProjectForm(Button saveButton, TextField nameField, DatePicker startDatePicker, DatePicker endDatePicker) {
        boolean nameValid = !nameField.getText().trim().isEmpty();
        boolean datesValid = startDatePicker.getValue() != null &&
                             endDatePicker.getValue() != null &&
                             !endDatePicker.getValue().isBefore(startDatePicker.getValue());

        saveButton.setDisable(!(nameValid && datesValid));
    }

    private void validateTaskForm(Button saveButton, TextField titleField, DatePicker startDatePicker, DatePicker endDatePicker) {
        boolean titleValid = !titleField.getText().trim().isEmpty();
        boolean datesValid = startDatePicker.getValue() != null &&
                             endDatePicker.getValue() != null &&
                             !endDatePicker.getValue().isBefore(startDatePicker.getValue());

        saveButton.setDisable(!(titleValid && datesValid));
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