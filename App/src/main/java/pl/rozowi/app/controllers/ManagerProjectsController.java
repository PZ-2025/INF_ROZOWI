package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pl.rozowi.app.dao.ProjectDAO;
<<<<<<< HEAD
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
=======
import pl.rozowi.app.models.Project;
>>>>>>> origin/main
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
<<<<<<< HEAD
    // Status column removed as requested

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();
=======

    private final ProjectDAO projectDAO = new ProjectDAO();
>>>>>>> origin/main
    private final ObservableList<Project> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colStart.setCellValueFactory(c -> c.getValue().startDateProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());
<<<<<<< HEAD
        // Status column removed as requested

=======
>>>>>>> origin/main
        loadAll();
    }

    private void loadAll() {
<<<<<<< HEAD
        data.clear();
=======
>>>>>>> origin/main
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

<<<<<<< HEAD
    @FXML
    private void onDeleteProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showWarning("Wybierz projekt do usunięcia");
            return;
        }

        // Load tasks to show count in the confirmation dialog
        List<Task> tasks = taskDAO.getTasksByProjectId(selectedProject.getId());
        int taskCount = tasks.size();

        // Create a confirmation dialog with warning about tasks
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć projekt?");

        String contentText = "Projekt: " + selectedProject.getName();
        if (taskCount > 0) {
            contentText += "\n\nUWAGA: To spowoduje również usunięcie " + taskCount + " zadań, powiązanych zespołów i aktywności!";
        }
        confirmDialog.setContentText(contentText);

        // Add custom buttons
        ButtonType deleteButtonType = new ButtonType("Usuń", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == deleteButtonType) {
            // Use the new delete method from ProjectDAO
            boolean deleted = projectDAO.deleteProject(selectedProject.getId());

            if (deleted) {
                // Remove from the local list
                data.remove(selectedProject);
                showInfo("Projekt został usunięty wraz ze wszystkimi powiązanymi elementami");
            } else {
                showError("Błąd", "Nie udało się usunąć projektu");
            }
        }
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

=======
>>>>>>> origin/main
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
<<<<<<< HEAD
            } else {
                // Default start and end dates for a new project
                dpStart.setValue(LocalDate.now());
                dpEnd.setValue(LocalDate.now().plusMonths(1));
            }

            // Validation
            Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            // Listeners to enable/disable OK button based on validation
            nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));
            dpStart.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));
            dpEnd.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));

            // Initial validation
            validateForm(okButton);

=======
            }

>>>>>>> origin/main
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
<<<<<<< HEAD

        private void validateForm(Button okButton) {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean datesValid = dpStart.getValue() != null &&
                                dpEnd.getValue() != null &&
                                !dpEnd.getValue().isBefore(dpStart.getValue());

            okButton.setDisable(!(nameValid && datesValid));
        }
    }
}
=======
    }
}
>>>>>>> origin/main
