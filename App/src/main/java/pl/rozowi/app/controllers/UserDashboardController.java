package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.models.NotificationItem;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.time.LocalDate;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    @FXML
    private TableView<NotificationItem> notificationsTable;
    @FXML
    private TableColumn<NotificationItem, String> colName;
    @FXML
    private TableColumn<NotificationItem, String> colDescription;
    @FXML
    private TableColumn<NotificationItem, String> colDate;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    // Przechowujemy pełną listę powiadomień, aby później filtrować
    private ObservableList<NotificationItem> allNotifications;

    public void setUser(User user) {
        welcomeLabel.setText("Witaj, " + user.getName());
    }



    @FXML
    private void initialize() {
        // Ustawienie cell value factory dla kolumn
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        // Automatyczne dopasowanie kolumn
        notificationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Przykładowe dane powiadomień
        allNotifications = FXCollections.observableArrayList(
                new NotificationItem("Zadanie #1", "Aktualizacja dokumentacji", LocalDate.of(2025, 3, 16).toString()),
                new NotificationItem("Zadanie #2", "Testowanie modułu logowania", LocalDate.of(2025, 3, 17).toString()),
                new NotificationItem("Zadanie #3", "Przygotowanie raportu tygodniowego", LocalDate.of(2025, 3, 18).toString())
        );
        notificationsTable.setItems(allNotifications);

        // Inline stylesheet – nagłówki kolumn na granatowo z białym tekstem
        notificationsTable.getStylesheets().add("data:text/css, .table-view .column-header-background { -fx-background-color: #000080; } .table-view .column-header .label { -fx-text-fill: white; }");

        // Dodaj listener do pola wyszukiwania
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterNotifications(newValue);
        });
    }

    private void filterNotifications(String filter) {
        if (filter == null || filter.isEmpty()) {
            notificationsTable.setItems(allNotifications);
            return;
        }
        ObservableList<NotificationItem> filtered = FXCollections.observableArrayList();
        String lowerFilter = filter.toLowerCase();
        for (NotificationItem item : allNotifications) {
            if (item.getName().toLowerCase().contains(lowerFilter)
                    || item.getDescription().toLowerCase().contains(lowerFilter)
                    || item.getDate().toLowerCase().contains(lowerFilter)) {
                filtered.add(item);
            }
        }
        notificationsTable.setItems(filtered);

    }

    @FXML
    private void goToMyTasks() throws IOException {
        loadView("/fxml/user/myTasks.fxml");
    }

    @FXML
    private void goToAllTasks() throws IOException {
        loadView("/fxml/user/tasks.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        loadView("/fxml/user/settings.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();
        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }
}
