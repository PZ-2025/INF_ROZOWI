package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminProjectsController {

    @FXML
    private TableView<Project> projectsTable;
    @FXML
    private TableColumn<Project, Number> colId;
    @FXML
    private TableColumn<Project, String> colName;
    @FXML
    private TableColumn<Project, String> colDesc;
    @FXML
    private TableColumn<Project, LocalDate> colStart;
    @FXML
    private TableColumn<Project, LocalDate> colEnd;
    @FXML
    private TableColumn<Project, Number> colManager;
    @FXML
    private TableColumn<Project, Number> colTeam;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ObservableList<Project> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colStart.setCellValueFactory(c -> c.getValue().startDateProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());
        colManager.setCellValueFactory(c -> c.getValue().managerIdProperty());
        colTeam.setCellValueFactory(c -> c.getValue().teamIdProperty());

        loadAll();
    }

    private void loadAll() {
        try {
            List<Project> projects = projectDAO.getAllProjects();
            data.setAll(projects);
            projectsTable.setItems(data);
        } catch (Exception e) {
            showError("Błąd wczytywania projektów", e.getMessage());
        }
    }

    @FXML
    private void onAddProject() {
        showProjectDialog(null);
    }

    @FXML
    private void onEditProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Wybierz projekt", "Musisz wybrać projekt do edycji.");
            return;
        }
        showProjectDialog(selected);
    }

    @FXML
    private void onDeleteProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Wybierz projekt", "Musisz wybrać projekt do usunięcia.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdź usunięcie");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć projekt: " + selected.getName() + "?");
        confirmDialog.setContentText("Ta operacja jest nieodwracalna.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Implementacja usuwania projektu
                // projectDAO.deleteProject(selected.getId());
                loadAll();
            } catch (Exception e) {
                showError("Błąd usuwania projektu", e.getMessage());
            }
        }
    }

    private void showProjectDialog(Project project) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle(project == null ? "Dodaj nowy projekt" : "Edytuj projekt");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Tworzenie siatki formularza
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa projektu");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Opis projektu");
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        // Pobieranie listy managerów
        ComboBox<User> managerCombo = new ComboBox<>();
        try {
            List<User> managers = userDAO.getAllManagers();
            managerCombo.setItems(FXCollections.observableArrayList(managers));
            managerCombo.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() : "";
                }

                @Override
                public User fromString(String string) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ustawienie wartości dla edycji istniejącego projektu
        if (project != null) {
            nameField.setText(project.getName());
            descArea.setText(project.getDescription());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());

            int managerId = project.getManagerId();
            try {
                for (User manager : userDAO.getAllManagers()) {
                    if (manager.getId() == managerId) {
                        managerCombo.setValue(manager);
                        break;
                    }
                }

                // Ustawienie zespołu
                int teamId = project.getTeamId();
                for (Team team : teamDAO.getAllTeams()) {
                    if (team.getId() == teamId) {
                        teamCombo.setValue(team);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Dodanie kontrolek do siatki
        grid.add(new Label("Nazwa:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Opis:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Data rozpoczęcia:"), 0, 2);
        grid.add(startDatePicker, 1, 2);
        grid.add(new Label("Data zakończenia:"), 0, 3);
        grid.add(endDatePicker, 1, 3);
        grid.add(new Label("Manager:"), 0, 4);
        grid.add(managerCombo, 1, 4);
        grid.add(new Label("Zespół:"), 0, 5);
        grid.add(teamCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (nameField.getText().isEmpty() || startDatePicker.getValue() == null ||
                    endDatePicker.getValue() == null || managerCombo.getValue() == null ||
                    teamCombo.getValue() == null) {
                    showWarning("Brakujące dane", "Wszystkie pola są wymagane.");
                    return null;
                }

                Project result = new Project();
                if (project != null) {
                    result.setId(project.getId());
                }
                result.setName(nameField.getText());
                result.setDescription(descArea.getText());
                result.setStartDate(startDatePicker.getValue());
                result.setEndDate(endDatePicker.getValue());
                result.setManagerId(managerCombo.getValue().getId());
                result.setTeamId(teamCombo.getValue().getId());
                return result;
            }
            return null;
        });

        Optional<Project> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                if (project == null) {
                    // Nowy projekt
                    projectDAO.insertProject(p);
                } else {
                    // Aktualizacja projektu
                    projectDAO.updateProject(p);
                }
                loadAll();
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