package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ManagerProjectsController {

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

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Project> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colStart.setCellValueFactory(c -> c.getValue().startDateProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());
        loadAll();
    }

    private void loadAll() {
        List<Project> projects = projectDAO.getProjectsForManager(Session.currentUserId);
        data.setAll(projects);
        projectsTable.setItems(data);
    }

    @FXML
    private void onAddProject() {
        ProjectFormDialog dlg = new ProjectFormDialog(null);
        Optional<Project> res = dlg.showAndWait();
        res.ifPresent(p -> {
            p.setManagerId(Session.currentUserId);
            try {
                projectDAO.insertProject(p);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            loadAll();
        });
    }

    @FXML
    private void onEditProject() {
        Project sel = projectsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        ProjectFormDialog dlg = new ProjectFormDialog(sel);
        Optional<Project> res = dlg.showAndWait();
        res.ifPresent(p -> {
            p.setManagerId(Session.currentUserId);
            projectDAO.updateProject(p);
            loadAll();
        });
    }

    private static class ProjectFormDialog extends Dialog<Project> {
        private final TextField nameField = new TextField();
        private final TextArea descArea = new TextArea();
        private final DatePicker dpStart = new DatePicker();
        private final DatePicker dpEnd = new DatePicker();

        public ProjectFormDialog(Project p) {
            setTitle(p == null ? "Nowy projekt" : "Edytuj projekt");
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Nazwa:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Opis:"), 0, 1);
            grid.add(descArea, 1, 1);
            grid.add(new Label("Data startu:"), 0, 2);
            grid.add(dpStart, 1, 2);
            grid.add(new Label("Data końca:"), 0, 3);
            grid.add(dpEnd, 1, 3);
            getDialogPane().setContent(grid);

            if (p != null) {
                nameField.setText(p.getName());
                descArea.setText(p.getDescription());
                dpStart.setValue(p.getStartDate());
                dpEnd.setValue(p.getEndDate());
            }

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    Project pr = (p == null ? new Project() : p);
                    pr.setName(nameField.getText());
                    pr.setDescription(descArea.getText());
                    pr.setStartDate(dpStart.getValue());
                    pr.setEndDate(dpEnd.getValue());
                    return pr;
                }
                return null;
            });
        }
    }
}
