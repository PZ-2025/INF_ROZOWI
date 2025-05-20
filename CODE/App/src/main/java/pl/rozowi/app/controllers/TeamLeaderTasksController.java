package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import pl.rozowi.app.dao.TaskDAO;
import pl.rozowi.app.models.Task;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler odpowiedzialny za zarządzanie zadaniami zespołu przez lidera.
 * Umożliwia wyświetlanie, filtrowanie, dodawanie, usuwanie oraz przeglądanie szczegółów zadań.
 */
public class TeamLeaderTasksController {

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Number> colId;
    @FXML
    private TableColumn<Task, String> colTitle;
    @FXML
    private TableColumn<Task, String> colDescription;
    @FXML
    private TableColumn<Task, String> colStatus;
    @FXML
    private TableColumn<Task, String> colPriority;
    @FXML
    private TableColumn<Task, String> colStart;
    @FXML
    private TableColumn<Task, String> colEnd;
    @FXML
    private TableColumn<Task, String> colTeam;
    @FXML
    private TableColumn<Task, String> colAssignedTo;
    @FXML
    private TextField filterField;

    private final TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    /**
     * Inicjalizacja kontrolera.
     * Konfiguruje tabelę zadań, ustawia fabrykę wierszy oraz listenera dla pola filtrującego.
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colEnd.setCellValueFactory(c -> c.getValue().endDateProperty());
        colTeam.setCellValueFactory(c -> c.getValue().teamNameProperty());
        colAssignedTo.setCellValueFactory(c -> c.getValue().assignedEmailProperty());

        tasksTable.setItems(allTasks);
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

        filterField.textProperty().addListener((obs, o, n) -> applyFilter(n));
        loadTasks();
    }

    /**
     * Ładuje zadania przypisane do zespołów, którymi zarządza aktualny lider.
     */
    @FXML
    private void loadTasks() {
        List<Task> list = taskDAO.getTasksForLeader(Session.currentUserId);
        allTasks.setAll(list);
    }

    /**
     * Filtruje zadania w tabeli na podstawie wprowadzonego tekstu.
     * @param text Tekst do filtrowania (wyszukiwany w tytule, statusie i przypisanym użytkowniku)
     */
    private void applyFilter(String text) {
        if (text == null || text.isBlank()) {
            tasksTable.setItems(allTasks);
            return;
        }
        String lower = text.toLowerCase();
        ObservableList<Task> filtered = allTasks.filtered(t ->
                t.getTitle().toLowerCase().contains(lower) ||
                        t.getStatus().toLowerCase().contains(lower) ||
                        t.getAssignedEmail().toLowerCase().contains(lower)
        );
        tasksTable.setItems(filtered);
    }

    /**
     * Otwiera okno dialogowe ze szczegółami wybranego zadania.
     * @param task Zadanie, którego szczegóły mają być wyświetlone
     */
    private void openDetails(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teamleader/taskDetails.fxml"));
            Parent root = loader.load();
            TaskDetailsController ctrl = loader.getController();
            ctrl.setTask(task);
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setTitle("Szczegóły zadania");
            st.showAndWait();
            loadTasks();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Obsługuje usuwanie wybranego zadania.
     * Wyświetla okno dialogowe z potwierdzeniem przed usunięciem.
     */
    @FXML
    private void handleDeleteTask() {
        Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "Wybierz zadanie", "Wybierz zadanie do usunięcia");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć zadanie?");
        confirmDialog.setContentText("Zadanie: " + selectedTask.getTitle() + "\n\nTa operacja jest nieodwracalna i usunie również wszystkie przypisania i aktywności zadania.");

        ButtonType anulujButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, anulujButtonType);

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
                showAlert(Alert.AlertType.ERROR, "Błąd usuwania", "Wystąpił błąd podczas usuwania zadania: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Wyświetla okno dialogowe z komunikatem.
     * @param type Typ komunikatu (ERROR, WARNING, INFORMATION, CONFIRMATION)
     * @param title Tytuł okna dialogowego
     * @param message Treść komunikatu
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (type == Alert.AlertType.CONFIRMATION) {
            ButtonType anulujButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getDialogPane().getButtonTypes().setAll(ButtonType.OK, anulujButtonType);
        }

        alert.showAndWait();
    }

    /**
     * Otwiera okno dialogowe do tworzenia nowego zadania.
     * Po zamknięciu okna odświeża listę zadań.
     */
    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/teamleader/taskCreate.fxml"));
            Parent root = loader.load();
            TaskCreateController ctrl = loader.getController();

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setTitle("Nowe zadanie");
            st.showAndWait();

            loadTasks();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}