package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ProgressBarTableCell;
import pl.rozowi.app.dao.ProjectDAO;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Project;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kontroler zarządzający widokiem zadań dla kierowników projektów.
 * Wyświetla listę projektów wraz ze statystykami dotyczącymi zadań.
 */
public class ManagerTasksController {

    @FXML
    private TextField filterField;
    @FXML
    private TableView<ProjectRow> projectsTable;
    @FXML
    private TableColumn<ProjectRow, Number> colId;
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

    /**
     * Inicjalizuje kontroler, konfigurując tabelę projektów i ładując dane.
     * @throws SQLException w przypadku problemów z dostępem do bazy danych
     */
    @FXML
    public void initialize() throws SQLException {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(filteredData.indexOf(c.getValue()) + 1));
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colDesc.setCellValueFactory(c -> c.getValue().descriptionProperty());
        colTotal.setCellValueFactory(c -> c.getValue().totalTasksProperty());
        colDone.setCellValueFactory(c -> c.getValue().completedTasksProperty());
        colProg.setCellValueFactory(c -> c.getValue().progressProperty().asObject());
        colProg.setCellFactory(ProgressBarTableCell.forTableColumn());

        projectsTable.setRowFactory(tv -> new TableRow<ProjectRow>() {
            @Override
            protected void updateItem(ProjectRow item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    return;
                }

                if (item.getTotalTasks() > 0 && item.getCompletedTasks() == item.getTotalTasks()) {
                    setStyle("-fx-background-color: #e6ffe6;");
                } else if (item.getProgress() >= 0.75) {
                    setStyle("-fx-background-color: #e6f9ff;");
                } else {
                    setStyle("");
                }
            }
        });

        loadAll();

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            onFilter();
        });
    }

    /**
     * Ładuje wszystkie projekty przypisane do aktualnego kierownika wraz ze statystykami zadań.
     * @throws SQLException w przypadku problemów z dostępem do bazy danych
     */
    private void loadAll() throws SQLException {
        data.clear();

        List<Project> managerProjects = projectDAO.getProjectsForManager(Session.currentUserId);

        if (managerProjects.isEmpty()) {
            showInfo("Nie masz przypisanych żadnych projektów.");
            return;
        }

        for (Project p : managerProjects) {
            List<Task> tasks = taskDAO.getTasksByProjectId(p.getId());

            long done = tasks.stream()
                    .map(Task::getStatus)
                    .filter(s -> s != null && s.trim().equalsIgnoreCase("Zakończone"))
                    .count();

            data.add(new ProjectRow(p, tasks.size(), (int) done));
        }

        filteredData.setAll(data);
        projectsTable.setItems(filteredData);
    }

    /**
     * Filtruje wyświetlane projekty na podstawie wprowadzonego tekstu.
     */
    @FXML
    private void onFilter() {
        String searchText = filterField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredData.setAll(data);
        } else {
            filteredData.setAll(data.stream()
                    .filter(r ->
                            r.getName().toLowerCase().contains(searchText) ||
                                    r.getDescription().toLowerCase().contains(searchText))
                    .collect(Collectors.toList()));
        }

        projectsTable.setItems(filteredData);
    }

    /**
     * Odświeża dane w tabeli projektów.
     */
    @FXML
    private void handleRefresh() {
        try {
            loadAll();
        } catch (SQLException e) {
            showError("Błąd odświeżania danych", e.getMessage());
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
     * Klasa reprezentująca wiersz projektu w tabeli z dodatkowymi statystykami zadań.
     */
    public static class ProjectRow extends Project {
        private final javafx.beans.property.IntegerProperty totalTasks =
                new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.IntegerProperty completedTasks =
                new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.DoubleProperty progress =
                new javafx.beans.property.SimpleDoubleProperty();

        /**
         * Tworzy nowy wiersz projektu z danymi statystycznymi.
         * @param p Obiekt projektu
         * @param total Liczba wszystkich zadań w projekcie
         * @param done Liczba zakończonych zadań w projekcie
         */
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

        /**
         * Zwraca właściwość z liczbą wszystkich zadań.
         * @return Właściwość IntegerProperty
         */
        public javafx.beans.property.IntegerProperty totalTasksProperty() {
            return totalTasks;
        }

        /**
         * Zwraca właściwość z liczbą zakończonych zadań.
         * @return Właściwość IntegerProperty
         */
        public javafx.beans.property.IntegerProperty completedTasksProperty() {
            return completedTasks;
        }

        /**
         * Zwraca właściwość z postępem zadań (wartość 0-1).
         * @return Właściwość DoubleProperty
         */
        public javafx.beans.property.DoubleProperty progressProperty() {
            return progress;
        }

        /**
         * Pobiera liczbę wszystkich zadań w projekcie.
         * @return Liczba zadań
         */
        public int getTotalTasks() {
            return totalTasks.get();
        }

        /**
         * Pobiera liczbę zakończonych zadań w projekcie.
         * @return Liczba zakończonych zadań
         */
        public int getCompletedTasks() {
            return completedTasks.get();
        }

        /**
         * Pobiera postęp realizacji zadań (wartość 0-1).
         * @return Wartość postępu (0.0 - 1.0)
         */
        public double getProgress() {
            return progress.get();
        }
    }
}