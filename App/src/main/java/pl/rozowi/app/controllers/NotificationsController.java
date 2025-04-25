package pl.rozowi.app.controllers;

import javafx.beans.property.SimpleStringProperty;
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
import pl.rozowi.app.dao.NotificationDAO;
import pl.rozowi.app.models.Notification;
import pl.rozowi.app.util.Session;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationsController {
    @FXML
    private TableView<Notification> tableView;
    @FXML
    private TableColumn<Notification, String> colType;
    @FXML
    private TableColumn<Notification, String> colDate;
    @FXML
    private TableColumn<Notification, String> colDesc;
    @FXML
    private ComboBox<String> filterCombo;

    private NotificationDAO dao = new NotificationDAO();
    private ObservableList<Notification> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(c.getValue().getCreatedAt())));
        colDesc.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription()));

        // Wyświetlanie pogrubione jeśli nieprzeczytane
        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !item.isRead()) {
                    setStyle("-fx-font-weight:bold;");
                } else {
                    setStyle("");
                }
            }
        });

        tableView.setOnMouseClicked(evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                Notification sel = tableView.getSelectionModel().getSelectedItem();
                if (sel != null) openDetails(sel);
            }
        });

        filterCombo.setItems(FXCollections.observableArrayList("Wszystkie", "Przeczytane", "Nieprzeczytane"));
        filterCombo.setValue("Wszystkie");

        filterCombo.setOnAction(e -> refreshList());
        refreshList();
    }

    @FXML
    public void refreshList() {
        int uid = Session.currentUserId;
        List<Notification> list;
        switch (filterCombo.getValue()) {
            case "Przeczytane":
                list = dao.getRead(uid);
                break;
            case "Nieprzeczytane":
                list = dao.getUnread(uid);
                break;
            default:
                list = dao.getAll(uid);
                break;
        }
        data.setAll(list);
        tableView.setItems(data);
    }

    private void openDetails(Notification n) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/notificationDetails.fxml"));
            Parent root = loader.load();
            NotificationDetailsController ctrl = loader.getController();
            ctrl.setNotification(n);

            // oznacz jako przeczytane
            if (!n.isRead()) {
                dao.updateReadStatus(n.getId(), true);
                n.setRead(true);
                tableView.refresh();
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
}
