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

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    /**
     * Ustawia usera i ładuje odpowiedni widok po zalogowaniu
     */
    public void setUser(User user) throws IOException {
        welcomeLabel.setText("Witaj, " + user.getName());

        if (user.getDefaultView() != null && !user.getDefaultView().isEmpty()) {
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
        } //else {
//            searchField.setVisible(true);
//            searchButton.setVisible(true);
//        }
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
