package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.NotificationDAO;
import pl.rozowi.app.models.Notification;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView logoImageView;
    @FXML
    private AnchorPane mainPane;

    // Powiadomienia
    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, String> colName;
    @FXML
    private TableColumn<Notification, String> colDescription;
    @FXML
    private TableColumn<Notification, String> colDate;

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ObservableList<Notification> allNotifications = FXCollections.observableArrayList();

    /**
     * Ustawia usera i ładuje odpowiedni widok po zalogowaniu
     */
    public void setUser(User user) throws IOException {
        welcomeLabel.setText("Witaj, " + user.getName());

        if (user.getDefaultView() != null && !user.getDefaultView().isEmpty()) {
            notificationsTable.setVisible(false);
            searchField.setVisible(false);
            searchButton.setVisible(false);
            switch (user.getDefaultView()) {
                case "Moje zadania":
                    goToMyTasks();
                    break;
                case "Zadania":
                    goToAllTasks();
                    break;
                case "Ustawienia":
                    goToSettings();
                    break;
                default:
                    goToMyTasks();
                    break;
            }
        } else {
            notificationsTable.setVisible(true);
            searchField.setVisible(true);
            searchButton.setVisible(true);
            loadNotifications(user.getId());
        }
    }

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNotificationType()));
        colDescription.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription()));
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(c.getValue().getCreatedAt())
                ));

        notificationsTable.setItems(allNotifications);

        notificationsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle(item.isRead() ? "" : "-fx-font-weight:bold;");
                }
            }
        });

        notificationsTable.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                Notification sel = notificationsTable.getSelectionModel().getSelectedItem();
                if (sel != null) openNotificationDetails(sel);
            }
        });

        searchButton.setOnAction(e -> filterNotifications(searchField.getText()));
    }

    private void loadNotifications(int userId) {
        Platform.runLater(() -> {
            List<Notification> list = notificationDAO.getAll(userId);
            allNotifications.setAll(list);
        });
    }

    private void filterNotifications(String filter) {
        if (filter == null || filter.isEmpty()) {
            notificationsTable.setItems(allNotifications);
        } else {
            ObservableList<Notification> filtered = FXCollections.observableArrayList();
            String lower = filter.toLowerCase();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Notification n : allNotifications) {
                if (n.getNotificationType().toLowerCase().contains(lower)
                        || n.getDescription().toLowerCase().contains(lower)
                        || sdf.format(n.getCreatedAt()).toLowerCase().contains(lower)) {
                    filtered.add(n);
                }
            }
            notificationsTable.setItems(filtered);
        }
    }

    private void openNotificationDetails(Notification n) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/notificationDetails.fxml"));
            Parent root = loader.load();
            NotificationDetailsController ctrl = loader.getController();
            ctrl.setNotification(n);

            if (!n.isRead()) {
                notificationDAO.updateReadStatus(n.getId(), true);
                n.setRead(true);
                notificationsTable.refresh();
            }

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setTitle("Powiadomienie");
            st.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ─── Przyciski nawigacji ─────────────────────────────────────────────────

    @FXML
    private void goToNotifications() throws IOException {
        // Poprawna ścieżka do pliku FXML z listą powiadomień
        loadView("/fxml/notifications.fxml");
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

        Object controller = loader.getController();
        if (controller instanceof SettingsController) {
            ((SettingsController) controller).setUser(MainApplication.getCurrentUser());
        }

        mainPane.getChildren().setAll(view);
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);

        Platform.runLater(() -> {
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.sizeToScene();
        });
    }
}
