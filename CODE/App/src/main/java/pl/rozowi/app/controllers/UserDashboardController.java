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
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserDashboardController extends BaseDashboardController {

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

    @FXML
    private Button myTasksButton;
    @FXML
    private Button allTasksButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;

    private static final String ACTIVE_BUTTON_STYLE = "sidebar-button-active";

    @FXML
    private void initialize() {
    }

    private void setActiveButton(Button activeButton) {
        myTasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        allTasksButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);
        settingsButton.getStyleClass().remove(ACTIVE_BUTTON_STYLE);

        if (activeButton != null) {
            activeButton.getStyleClass().add(ACTIVE_BUTTON_STYLE);
        }
    }

    @Override
    protected void onUserSet(User user) {
        welcomeLabel.setText("Witaj, " + user.getName());

        try {
            String def = user.getDefaultView();
            if (def != null) {
                switch (def) {
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
                goToMyTasks();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Scene getScene() {
        return mainPane != null ? mainPane.getScene() : null;
    }

    @FXML
    private void goToMyTasks() throws IOException {
        setActiveButton(myTasksButton);
        loadView("/fxml/user/myTasks.fxml");
    }

    @FXML
    private void goToAllTasks() throws IOException {
        setActiveButton(allTasksButton);
        loadView("/fxml/user/tasks.fxml");
    }

    @FXML
    private void goToSettings() throws IOException {
        setActiveButton(settingsButton);
        loadView("/fxml/user/settings.fxml");
    }

    @FXML
    private void logout() throws IOException {
        MainApplication.setCurrentUser(null);
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Logowanie");
    }

    private void loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();

        Object controller = loader.getController();
        if (controller instanceof SettingsController) {
            ((SettingsController) controller).setUser(currentUser);
        } else if (controller instanceof UserAwareController) {
            ((UserAwareController) controller).setUser(currentUser);
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