//package pl.rozowi.app.controllers;
//
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import pl.rozowi.app.dao.TaskDAO;
//import pl.rozowi.app.models.Task;
//import pl.rozowi.app.util.Session;
//
//import java.util.List;
//import java.util.Optional;
//
//public class TasksController {
//
//    @FXML
//    private TableView<Task> tasksTable;
//    @FXML
//    private TableColumn<Task, Number> taskIdColumn;
//    @FXML
//    private TableColumn<Task, String> taskNameColumn;
//    @FXML
//    private TableColumn<Task, String> taskDescriptionColumn;
//    @FXML
//    private TableColumn<Task, String> taskStatusColumn;
//    @FXML
//    private TableColumn<Task, String> taskStartDateColumn;
//    @FXML
//    private TableColumn<Task, String> taskDeadlineColumn;
//    @FXML
//    private TableColumn<Task, String> taskTeamColumn;
//    @FXML
//    private TableColumn<Task, Integer> taskAssignedToColumn;
//    @FXML
//    private TextField filterField;
//
//    private TaskDAO taskDAO = new TaskDAO();
//    private ObservableList<Task> allTasks;
//
//    @FXML
//    private void initialize() {
//        taskIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
//        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
//        taskDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
//        taskStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
//        taskStartDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
//        taskDeadlineColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());
//        tasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//
//        loadTasks();
//    }
//
//    private void loadTasks() {
//        int teamId = 0;
//        try {
//            teamId = Integer.parseInt(Session.currentUserTeam);
//        } catch (NumberFormatException e) {
//            System.err.println("Błąd konwersji Session.currentUserTeam na int: " + Session.currentUserTeam);
//            e.printStackTrace();
//        }
//        List<Task> tasks = taskDAO.getColleagueTasks(Session.currentUserId, teamId);
//        allTasks = FXCollections.observableArrayList(tasks);
//        tasksTable.setItems(allTasks);
//    }
//
//    @FXML
//    private void handleFilter() {
//        String filterText = filterField.getText();
//        if (filterText == null || filterText.isEmpty()) {
//            tasksTable.setItems(allTasks);
//            return;
//        }
//        ObservableList<Task> filtered = FXCollections.observableArrayList();
//        String lower = filterText.toLowerCase();
//        for (Task task : allTasks) {
//            if (task.getTitle().toLowerCase().contains(lower) ||
//                    task.getDescription().toLowerCase().contains(lower) ||
//                    task.getStatus().toLowerCase().contains(lower) ||
//                    task.getStartDate().toLowerCase().contains(lower) ||
//                    task.getEndDate().toLowerCase().contains(lower)) {
//                filtered.add(task);
//            }
//        }
//        tasksTable.setItems(filtered);
//    }
//
//@FXML
//    private void handleAddTask() {
//        System.out.println("Dodawanie nowego zadania...");
//    }
//
//    /**
//     * Obsługuje usuwanie zadania.
//     * Zwykły użytkownik powinien mieć ograniczone uprawnienia do usuwania zadań,
//     * więc ta funkcjonalność może być dostępna tylko dla użytkowników o odpowiednich rolach.
//     */
//    @FXML
//    private void handleDeleteTask() {
//        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
//        if (selectedTask == null) {
//            showAlert(Alert.AlertType.WARNING, "Wybierz zadanie", "Wybierz zadanie do usunięcia");
//            return;
//        }
//
//        // Sprawdź czy użytkownik ma uprawnienia do usuwania zadań
//        // Tylko zadania przypisane do danego użytkownika można usunąć
//        if (selectedTask.getAssignedTo() != Session.currentUserId) {
//            showAlert(Alert.AlertType.WARNING, "Brak uprawnień",
//                    "Możesz usuwać tylko zadania, które są przypisane bezpośrednio do Ciebie");
//            return;
//        }
//
//        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
//        confirmDialog.setTitle("Potwierdzenie usunięcia");
//        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć zadanie?");
//        confirmDialog.setContentText("Zadanie: " + selectedTask.getTitle() +
//                "\n\nTa operacja jest nieodwracalna i usunie również wszystkie przypisania i aktywności zadania.");
//
//        Optional<ButtonType> result = confirmDialog.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            try {
//                boolean deleted = taskDAO.deleteTask(selectedTask.getId());
//
//                if (deleted) {
//                    loadTasks(); // Odśwież listę zadań
//                    showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zadanie zostało pomyślnie usunięte z systemu");
//                } else {
//                    showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć zadania z bazy danych");
//                }
//            } catch (Exception ex) {
//                showAlert(Alert.AlertType.ERROR, "Błąd usuwania",
//                        "Wystąpił błąd podczas usuwania zadania: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    private void showAlert(Alert.AlertType type, String title, String message) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}

package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.Session;

import java.util.List;
import java.util.Optional;

public class TasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> taskIdColumn;
    @FXML
    private TableColumn<Task, String> taskNameColumn;
    @FXML
    private TableColumn<Task, String> taskDescriptionColumn;
    @FXML
    private TableColumn<Task, String> taskStatusColumn;
    @FXML
    private TableColumn<Task, String> taskStartDateColumn;
    @FXML
    private TableColumn<Task, String> taskDeadlineColumn;
    @FXML
    private TableColumn<Task, String> taskTeamColumn;
    @FXML
    private TableColumn<Task, String> taskAssignedToColumn;
    @FXML
    private TextField filterField;

    private TaskDAO taskDAO = new TaskDAO();
    private UserDAO userDAO = new UserDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private ObservableList<Task> allTasks;

    @FXML
    private void initialize() {
        // Liczba porządkowa
        taskIdColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createIntegerBinding(
                () -> tasksTable.getItems().indexOf(cellData.getValue()) + 1
        ));

        taskNameColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        taskDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        taskStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        taskStartDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty());
        taskDeadlineColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty());

        // Nowość: zamiast ID przypisanego użytkownika, pokazuj email
        taskAssignedToColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            User user = userDAO.getUserById(cellData.getValue().getAssignedTo());
            return user != null ? user.getEmail() : "Nieprzypisany";
        }));

        // Nowość: nazwa zespołu
        taskTeamColumn.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> {
            return teamDAO.getTeamNameById(cellData.getValue().getTeamId());
        }));

        tasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadTasks();
    }



    private void loadTasks() {
        int teamId = 0;
        try {
            teamId = Integer.parseInt(Session.currentUserTeam);
        } catch (NumberFormatException e) {
            System.err.println("Błąd konwersji Session.currentUserTeam na int: " + Session.currentUserTeam);
            e.printStackTrace();
        }
        List<Task> tasks = taskDAO.getColleagueTasks(Session.currentUserId, teamId);
        allTasks = FXCollections.observableArrayList(tasks);
        tasksTable.setItems(allTasks);
    }

    @FXML
    private void handleFilter() {
        String filterText = filterField.getText();
        if (filterText == null || filterText.isEmpty()) {
            tasksTable.setItems(allTasks);
            return;
        }
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        String lower = filterText.toLowerCase();
        for (Task task : allTasks) {
            if (task.getTitle().toLowerCase().contains(lower) ||
                    task.getDescription().toLowerCase().contains(lower) ||
                    task.getStatus().toLowerCase().contains(lower) ||
                    task.getStartDate().toLowerCase().contains(lower) ||
                    task.getEndDate().toLowerCase().contains(lower)) {
                filtered.add(task);
            }
        }
        tasksTable.setItems(filtered);
    }

    @FXML
    private void handleAddTask() {
        System.out.println("Dodawanie nowego zadania...");
    }

    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "Wybierz zadanie", "Wybierz zadanie do usunięcia");
            return;
        }

        if (selectedTask.getAssignedTo() != Session.currentUserId) {
            showAlert(Alert.AlertType.WARNING, "Brak uprawnień",
                    "Możesz usuwać tylko zadania, które są przypisane bezpośrednio do Ciebie");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć zadanie?");
        confirmDialog.setContentText("Zadanie: " + selectedTask.getTitle() +
                "\n\nTa operacja jest nieodwracalna i usunie również wszystkie przypisania i aktywności zadania.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = taskDAO.deleteTask(selectedTask.getId());

                if (deleted) {
                    loadTasks();
                    showAlert(Alert.AlertType.INFORMATION, "Sukces", "Zadanie zostało pomyślnie usunięte z systemu");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Błąd", "Nie udało się usunąć zadania z bazy danych");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Błąd usuwania",
                        "Wystąpił błąd podczas usuwania zadania: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


