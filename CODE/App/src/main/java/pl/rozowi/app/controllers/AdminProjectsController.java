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
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler odpowiedzialny za zarządzanie projektami w panelu administratora.
 * Udostępnia funkcjonalności przeglądania, dodawania, edycji i usuwania projektów,
 * a także zarządzania powiązanymi zespołami i zadaniami.
 */
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

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();

    private final ObservableList<Project> allProjects = FXCollections.observableArrayList();
    private final ObservableList<Team> projectTeams = FXCollections.observableArrayList();
    private final ObservableList<Task> projectTasks = FXCollections.observableArrayList();

    /**
     * Metoda inicjalizująca kontroler. Konfiguruje tabele projektów, zespołów i zadań,
     * ustawia wartości domyślne i ładuje dane.
     */
    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colStartDate.setCellValueFactory(data -> data.getValue().startDateProperty());
        colEndDate.setCellValueFactory(data -> data.getValue().endDateProperty());
        colManager.setCellValueFactory(data -> {
            int managerId = data.getValue().getManagerId();
            if (managerId <= 0) {
                return new SimpleStringProperty("Brak przypisania");
            }

            try {
                List<User> managers = userDAO.getAllManagers();
                for (User manager : managers) {
                    if (manager.getId() == managerId) {
                        return new SimpleStringProperty(manager.getName() + " " + manager.getLastName());
                    }
                }
                return new SimpleStringProperty("ID: " + managerId);
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("Błąd");
            }
        });

        colTeamId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTeamName.setCellValueFactory(data -> data.getValue().teamNameProperty());
        colTeamLeader.setCellValueFactory(data -> {
            int teamId = data.getValue().getId();
            try {
                List<User> teamMembers = teamDAO.getTeamMembers(teamId);
                for (User member : teamMembers) {
                    if (teamMemberDAO.isTeamLeader(teamId, member.getId())) {
                        return new SimpleStringProperty(member.getName() + " " + member.getLastName());
                    }
                }
                return new SimpleStringProperty("Brak lidera");
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("Błąd pobierania");
            }
        });
        colMembersCount.setCellValueFactory(data -> {
            int teamId = data.getValue().getId();
            try {
                List<User> members = teamDAO.getTeamMembers(teamId);
                return new SimpleObjectProperty<>(members.size());
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleObjectProperty<>(0);
            }
        });

        colTaskId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTaskTitle.setCellValueFactory(data -> data.getValue().titleProperty());
        colTaskStatus.setCellValueFactory(data -> data.getValue().statusProperty());
        colTaskPriority.setCellValueFactory(data -> data.getValue().priorityProperty());
        colTaskDeadline.setCellValueFactory(data -> data.getValue().endDateProperty());
        colTaskAssignee.setCellValueFactory(data -> data.getValue().assignedEmailProperty());

        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProjectTeams(newSelection.getId());
                loadProjectTasks(newSelection.getId());
            } else {
                projectTeams.clear();
                projectTasks.clear();
            }
        });

        loadProjects();
    }

    /**
     * Ładuje listę wszystkich projektów z bazy danych.
     */
    private void loadProjects() {
        try {
            List<Project> projects = projectDAO.getAllProjects();
            allProjects.setAll(projects);
            projectsTable.setItems(allProjects);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania projektów", e.getMessage());
        }
    }

    /**
     * Ładuje listę zespołów powiązanych z wybranym projektem.
     *
     * @param projectId ID projektu dla którego mają zostać załadowane zespoły
     */
    private void loadProjectTeams(int projectId) {
        try {
            List<Team> teams = teamDAO.getAllTeams();
            List<Team> projectTeamsFiltered = teams.stream()
                .filter(team -> team.getProjectId() == projectId)
                .toList();

            projectTeams.setAll(projectTeamsFiltered);
            teamsTable.setItems(projectTeams);
        } catch (SQLException e) {
            showError("Błąd podczas wczytywania zespołów", e.getMessage());
        }
    }

    /**
     * Ładuje listę zadań powiązanych z wybranym projektem.
     *
     * @param projectId ID projektu dla którego mają zostać załadowane zadania
     */
    private void loadProjectTasks(int projectId) {
        try {
            List<Task> tasks = taskDAO.getTasksByProjectId(projectId);
            projectTasks.setAll(tasks);
            tasksTable.setItems(projectTasks);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania zadań", e.getMessage());
        }
    }

    /**
     * Wyszukuje projekty na podstawie wprowadzonego tekstu.
     */
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

    /**
     * Obsługuje akcję dodawania nowego projektu.
     */
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

    /**
     * Obsługuje akcję edycji istniejącego projektu.
     */
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

    /**
     * Obsługuje akcję usuwania projektu.
     */
    @FXML
    private void handleDeleteProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt do usunięcia");
            return;
        }

        List<Task> tasks = taskDAO.getTasksByProjectId(selectedProject.getId());
        int taskCount = tasks.size();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć projekt?");

        String contentText = "Projekt: " + selectedProject.getName();
        if (taskCount > 0) {
            contentText += "\n\nUWAGA: To spowoduje również usunięcie " + taskCount + " zadań, powiązanych zespołów i aktywności!";
        }
        confirmDialog.setContentText(contentText);

        ButtonType deleteButtonType = new ButtonType("Usuń", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == deleteButtonType) {
            boolean deleted = projectDAO.deleteProject(selectedProject.getId());

            if (deleted) {
                allProjects.remove(selectedProject);
                projectTeams.clear();
                projectTasks.clear();

                showInfo("Projekt został usunięty wraz ze wszystkimi powiązanymi elementami");
            } else {
                showError("Błąd", "Nie udało się usunąć projektu");
            }
        }
    }

    /**
     * Obsługuje akcję przypisania kierownika do projektu.
     */
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
            ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, cancelButtonType);

            ComboBox<User> managerComboBox = new ComboBox<>();
            managerComboBox.setItems(FXCollections.observableArrayList(managers));

            managerComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

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

    /**
     * Odświeża listę projektów i powiązanych danych.
     */
    @FXML
    private void handleRefresh() {
        loadProjects();

        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            loadProjectTeams(selectedProject.getId());
            loadProjectTasks(selectedProject.getId());
        }
    }

    /**
     * Tworzy okno dialogowe do dodawania/edycji projektu.
     *
     * @param project istniejący projekt (null dla nowego projektu)
     * @return skonfigurowane okno dialogowe
     */
    private Dialog<Project> createProjectDialog(Project project) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle(project == null ? "Dodaj nowy projekt" : "Edytuj projekt");
        dialog.setHeaderText(project == null ? "Wprowadź dane nowego projektu" : "Edytuj dane projektu");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

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

        ComboBox<User> managerComboBox = new ComboBox<>();
        try {
            List<User> managers = userDAO.getAllManagers();
            managerComboBox.setItems(FXCollections.observableArrayList(managers));

            managerComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (project != null) {
            nameField.setText(project.getName());
            descriptionArea.setText(project.getDescription());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());

            int currentManagerId = project.getManagerId();
            if (currentManagerId > 0) {
                for (User manager : managerComboBox.getItems()) {
                    if (manager.getId() == currentManagerId) {
                        managerComboBox.setValue(manager);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Opis:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Data rozpoczęcia:"), 0, 2);
        grid.add(startDatePicker, 1, 2);
        grid.add(new Label("Data zakończenia:"), 0, 3);
        grid.add(endDatePicker, 1, 3);
        grid.add(new Label("Kierownik:"), 0, 4);
        grid.add(managerComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

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

                User selectedManager = managerComboBox.getValue();
                if (selectedManager != null) {
                    result.setManagerId(selectedManager.getId());
                } else {
                    result.setManagerId(0);
                }

                return result;
            }
            return null;
        });

        return dialog;
    }

    /**
     * Waliduje formularz projektu.
     *
     * @param saveButton przycisk zapisu
     * @param nameField pole nazwy projektu
     * @param startDatePicker data rozpoczęcia
     * @param endDatePicker data zakończenia
     */
    private void validateProjectForm(Button saveButton, TextField nameField, DatePicker startDatePicker, DatePicker endDatePicker) {
        boolean nameValid = !nameField.getText().trim().isEmpty();
        boolean datesValid = startDatePicker.getValue() != null &&
                             endDatePicker.getValue() != null &&
                             !endDatePicker.getValue().isBefore(startDatePicker.getValue());

        saveButton.setDisable(!(nameValid && datesValid));
    }

    /**
     * Wyświetla okno dialogowe z informacją.
     *
     * @param message treść wiadomości
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
     *
     * @param message treść ostrzeżenia
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
     *
     * @param title tytuł okna
     * @param message treść błędu
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}