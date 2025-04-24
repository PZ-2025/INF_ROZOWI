package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.NotificationDAO;
import pl.rozowi.app.models.Notification;
import pl.rozowi.app.models.NotificationItem;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboardController {

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
    private ObservableList<NotificationItem> allNotifications = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        try {
            goToEmployees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAdminName(String adminName) {
        welcomeLabel.setText("Witaj, " + adminName);
    }

    @FXML
    private void goToEmployees() throws IOException {
        loadView("/fxml/admin/adminEmployees.fxml");
    }

    @FXML
    private void goToTeams() throws IOException {
        loadView("/fxml/admin/adminTeams.fxml");
    }

    @FXML
    private void goToReports() throws IOException {
        loadView("/fxml/admin/adminReports.fxml");
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
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent view = loader.load();

        mainPane.getChildren().clear();
        mainPane.getChildren().add(view);

        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
    }

//    private void loadNotifications(User user) {
//        NotificationDAO notificationDAO = new NotificationDAO();
//        List<Notification> notifications = notificationDAO.getNotificationsForUser(user.getId());
//
//        if (notifications.isEmpty()) {
//            showNoNotificationsMessage();
//            return;
//        }
//
//        ObservableList<NotificationItem> items = FXCollections.observableArrayList();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        for (Notification n : notifications) {
//            String dateStr = (n.getDate() != null) ? sdf.format(n.getDate()) : "Brak daty";
//            String notificationName = n.getNotificationType() != null ? n.getNotificationType() : "Powiadomienie";
//
//            NotificationItem item = new NotificationItem(
//                    notificationName,
//                    n.getDescription() != null ? n.getDescription() : "Brak opisu",
//                    dateStr
//            );
//            items.add(item);
//        }
//
//        Platform.runLater(() -> {
//            allNotifications.clear();
//            allNotifications.addAll(items);
//            notificationsTable.refresh();
//        });
//    }
//
//    private void showNoNotificationsMessage() {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Powiadomienia");
//            alert.setHeaderText(null);
//            alert.setContentText("Brak nowych powiadomień.");
//            alert.showAndWait();
//        });
//    }
//
//    private void filterNotifications(String filter) {
//        if (filter == null || filter.isEmpty()) {
//            notificationsTable.setItems(allNotifications);
//            return;
//        }
//
//        ObservableList<NotificationItem> filtered = allNotifications.filtered(item ->
//                item.getName().toLowerCase().contains(filter.toLowerCase()) ||
//                        item.getDescription().toLowerCase().contains(filter.toLowerCase()) ||
//                        item.getDate().toLowerCase().contains(filter.toLowerCase())
//        );
//
//        notificationsTable.setItems(filtered);
//    }

    public void setUser(User user) throws IOException {
        welcomeLabel.setText("Witaj, " + user.getName());
        System.out.println("Domyślny widok: " + user.getDefaultView());

        if (user.getDefaultView() != null && !user.getDefaultView().isEmpty()) {
            notificationsTable.setVisible(false);
            searchField.setVisible(false);
            searchButton.setVisible(false);

            switch (user.getDefaultView()) {
                case "Pracownicy":
                    goToEmployees();
                    break;
                case "Zespoły":
                    goToTeams();
                    break;
                case "Raporty":
                    goToReports();
                    break;
                case "Ustawienia":
                    goToSettings();
                    break;
                default:
                    goToEmployees();
            }
        } else {
            notificationsTable.setVisible(true);
            searchField.setVisible(true);
            searchButton.setVisible(true);
//            loadNotifications(user);
        }
    }
}
