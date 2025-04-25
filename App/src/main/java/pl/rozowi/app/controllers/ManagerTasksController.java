package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;

import java.sql.SQLException;
import java.util.List;

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

    @FXML
    public void initialize() throws SQLException {
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colTotal.setCellValueFactory(c -> c.getValue().totalTasksProperty());
        colDone.setCellValueFactory(c -> c.getValue().completedTasksProperty());
        colProg.setCellValueFactory(c -> c.getValue().progressProperty().asObject());
        colProg.setCellFactory(ProgressBarTableCell.forTableColumn());

        loadAll();
    }

    private void loadAll() throws SQLException {
        data.clear();
        List<Project> projects = projectDAO.getAllProjects();
        for (Project p : projects) {
            List<Task> tasks = taskDAO.getTasksByProjectId(p.getId());
            long done = tasks.stream()
                    .map(Task::getStatus)
                    .filter(s -> s != null && s.trim().equalsIgnoreCase("Zakończone"))
                    .count();
            data.add(new ProjectRow(p, tasks.size(), (int) done));
        }
        projectsTable.setItems(data);
    }

    @FXML
    private void onFilter() {
        String f = filterField.getText().toLowerCase();
        if (f.isEmpty()) {
            projectsTable.setItems(data);
        } else {
            projectsTable.setItems(data.filtered(r ->
                    r.getName().toLowerCase().contains(f) ||
                            r.getDescription().toLowerCase().contains(f)
            ));
        }
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
