package pl.rozowi.app.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler zarządzania projektami dla użytkowników z rolą Kierownika.
 * Umożliwia przeglądanie, dodawanie, edycję i usuwanie projektów,
 * wraz z powiązanymi zadaniami i zespołami.
 */
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
    private final TaskDAO taskDAO = new TaskDAO();
    private final ObservableList<Project> data = FXCollections.observableArrayList();

    /**
     * Inicjalizuje kontroler, konfigurując tabelę projektów i ładując dane.
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                projectsTable.getItems().indexOf(data.getValue()) + 1));

        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colStart.setCellValueFactory(c -> c.getValue().startDateProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());

        loadAll();
    }

    /**
     * Ładuje wszystkie projekty przypisane do aktualnego kierownika.
     */
    private void loadAll() {
        data.clear();
        List<Project> projects = projectDAO.getProjectsForManager(Session.currentUserId);
        data.setAll(projects);
        projectsTable.setItems(data);
    }

    /**
     * Obsługuje dodawanie nowego projektu.
     * Wyświetla okno dialogowe do wprowadzenia danych projektu.
     */
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

    /**
     * Obsługuje edycję istniejącego projektu.
     * Wyświetla okno dialogowe z danymi wybranego projektu.
     */
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

    /**
     * Obsługuje usuwanie projektu.
     * Wyświetla ostrzeżenie jeśli projekt zawiera powiązane zadania.
     */
    @FXML
    private void onDeleteProject() {
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

        int projectIndex = projectsTable.getItems().indexOf(selectedProject) + 1;
        String contentText = "Projekt #" + projectIndex + ": " + selectedProject.getName();

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
                data.remove(selectedProject);
                showInfo("Projekt został usunięty wraz ze wszystkimi powiązanymi elementami");
            } else {
                showError("Błąd", "Nie udało się usunąć projektu");
            }
        }
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

    /**
     * Wewnętrzna klasa okna dialogowego do tworzenia/edycji projektu.
     */

    private static class ProjectFormDialog extends Dialog<Project> {
        private final TextField nameField = new TextField();
        private final TextArea descArea = new TextArea();
        private final DatePicker dpStart = new DatePicker();
        private final DatePicker dpEnd = new DatePicker();

        /**
         * Tworzy nowe okno dialogowe do edycji projektu.
         * @param p Projekt do edycji lub null dla nowego projektu
         */
        public ProjectFormDialog(Project p) {
            setTitle(p == null ? "Nowy projekt" : "Edytuj projekt");
            ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, cancelButtonType);

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
            } else {
                dpStart.setValue(LocalDate.now());
                dpEnd.setValue(LocalDate.now().plusMonths(1));
            }

            Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));
            dpStart.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));
            dpEnd.valueProperty().addListener((obs, oldVal, newVal) -> validateForm(okButton));

            validateForm(okButton);

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

        /**
         * Sprawdza poprawność danych w formularzu.
         * @param okButton Przycisk OK do aktywacji/dezaktywacji
         */
        private void validateForm(Button okButton) {
            boolean nameValid = !nameField.getText().trim().isEmpty();
            boolean datesValid = dpStart.getValue() != null &&
                    dpEnd.getValue() != null &&
                    !dpEnd.getValue().isBefore(dpStart.getValue());

            okButton.setDisable(!(nameValid && datesValid));
        }
    }
}