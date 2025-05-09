package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerTasksController {

    @FXML
    private TextField filterField;
    @FXML
    private TableView<ProjectRow> projectsTable;
    @FXML
    private TableColumn<ProjectRow, String> colName;
    @FXML
    private TableColumn<ProjectRow, String> colDesc;
    @FXML
    private TableColumn<ProjectRow, Number> colTotal;
    @FXML
    private TableColumn<ProjectRow, Number> colDone;
    @FXML
    private TableColumn<ProjectRow, Double> colProg;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final ObservableList<ProjectRow> data = FXCollections.observableArrayList();
    private final ObservableList<ProjectRow> filteredData = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colTotal.setCellValueFactory(c -> c.getValue().totalTasksProperty());
        colDone.setCellValueFactory(c -> c.getValue().completedTasksProperty());
        colProg.setCellValueFactory(c -> c.getValue().progressProperty().asObject());
        colProg.setCellFactory(ProgressBarTableCell.forTableColumn());

        loadAll();

        // Dodanie nasłuchiwania na zmiany w polu wyszukiwania
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            onFilter();
        });
    }

    private void loadAll() throws SQLException {
        data.clear();

        // Pobierz tylko projekty przypisane do zalogowanego kierownika
        List<Project> managerProjects = projectDAO.getProjectsForManager(Session.currentUserId);

        if (managerProjects.isEmpty()) {
            showInfo("Nie masz przypisanych żadnych projektów.");
            return;
        }

        for (Project p : managerProjects) {
            // Pobierz zadania dla każdego projektu
            List<Task> tasks = taskDAO.getTasksByProjectId(p.getId());

            // Oblicz liczbę zakończonych zadań
            long done = tasks.stream()
                    .map(Task::getStatus)
                    .filter(s -> s != null && s.trim().equalsIgnoreCase("Zakończone"))
                    .count();

            // Dodaj wiersz projektu z danymi o zadaniach
            data.add(new ProjectRow(p, tasks.size(), (int) done));
        }

        // Ustaw dane w tabeli
        filteredData.setAll(data);
        projectsTable.setItems(filteredData);
    }

    @FXML
    private void onFilter() {
        String searchText = filterField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            // Jeśli pole wyszukiwania jest puste, wyświetl wszystkie projekty
            filteredData.setAll(data);
        } else {
            // Filtruj projekty według tekstu wyszukiwania
            filteredData.setAll(data.stream()
                    .filter(r ->
                            r.getName().toLowerCase().contains(searchText) ||
                                    r.getDescription().toLowerCase().contains(searchText))
                    .collect(Collectors.toList()));
        }

        projectsTable.setItems(filteredData);
    }

    @FXML
    private void handleRefresh() {
        try {
            loadAll();
        } catch (SQLException e) {
            showError("Błąd odświeżania danych", e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
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

    public static class ProjectRow extends Project {
        private final javafx.beans.property.IntegerProperty totalTasks =
                new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.IntegerProperty completedTasks =
                new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.DoubleProperty progress =
                new javafx.beans.property.SimpleDoubleProperty();

        public ProjectRow(Project p, int total, int done) {
            setId(p.getId());
            setProjectName(p.getProjectName());
            setDescription(p.getDescription());
            setStartDate(p.getStartDate());
            setEndDate(p.getEndDate());
            setManagerId(p.getManagerId());

            totalTasks.set(total);
            completedTasks.set(done);
            progress.set(total == 0 ? 0.0 : (double) done / total);
        }

        public javafx.beans.property.IntegerProperty totalTasksProperty() {
            return totalTasks;
        }

        public javafx.beans.property.IntegerProperty completedTasksProperty() {
            return completedTasks;
        }

        public javafx.beans.property.DoubleProperty progressProperty() {
            return progress;
        }
    }
}