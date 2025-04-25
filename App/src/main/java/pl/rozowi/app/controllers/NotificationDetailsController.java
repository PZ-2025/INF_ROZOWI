package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pl.rozowi.app.models.Notification;

import java.text.SimpleDateFormat;

public class NotificationDetailsController {
    @FXML
    private Label lblId, lblType, lblDate, lblDesc;

    public void setNotification(Notification n) {
        if (n == null) {
            lblId.setText("Brak danych");
            lblDate.setText("Brak danych");
            lblDesc.setText("Brak danych");
            return;
        }

        lblId.setText(String.valueOf(n.getId()));
        lblDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(n.getCreatedAt()));
        lblDesc.setText(n.getDescription());
    }

    @FXML
    private void close() {
        ((Stage) lblId.getScene().getWindow()).close();
    }
}
