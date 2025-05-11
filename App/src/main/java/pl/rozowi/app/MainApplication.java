package pl.rozowi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.util.ThemeManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MainApplication extends Application {

    private static Stage primaryStage;
    private static User currentUser;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Connected to database successfully!");
        } catch (SQLException ex) {
            System.err.println("Failed to connect to database: " + ex.getMessage());
            ex.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SplashScreen.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1440, 900);

        ThemeManager.applyTheme(scene, null);

        stage.setTitle("TaskApp - Start");
        stage.setFullScreen(false);
        stage.setScene(scene);
        stage.setMinWidth(1440);
        stage.setMinHeight(900);
        stage.setResizable(true);
        stage.show();
    }

    public static void switchScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        Parent root = loader.load();
        Object controller = loader.getController();
        if (controller instanceof pl.rozowi.app.controllers.UserDashboardController) {
            ((pl.rozowi.app.controllers.UserDashboardController) controller).setUser(currentUser);
        } else if (controller instanceof pl.rozowi.app.controllers.AdminDashboardController) {
            ((pl.rozowi.app.controllers.AdminDashboardController) controller).setUser(currentUser);
        } else if (controller instanceof pl.rozowi.app.controllers.ManagerDashboardController) {
            ((pl.rozowi.app.controllers.ManagerDashboardController) controller).setUser(currentUser);
        }

        Scene currentScene = primaryStage.getScene();
        Scene newScene;

        if (currentScene != null) {
            newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
        } else {
            newScene = new Scene(root, 1440, 900);
        }

        ThemeManager.applyTheme(newScene, currentUser);

        primaryStage.setScene(newScene);
        primaryStage.setTitle(title);
    }

    public static void setCurrentUser(User user) {
        currentUser = user;

        if (user != null) {
            loadUserSettings(user);
        }

        if (primaryStage != null && primaryStage.getScene() != null) {
            ThemeManager.applyTheme(primaryStage.getScene(), user);
        }
    }

    private static void loadUserSettings(User user) {
        SettingsDAO settingsDAO = new SettingsDAO();
        Settings userSettings = settingsDAO.getSettingsByUserId(user.getId());

        if (userSettings != null) {
            if (userSettings.getTheme() != null) {
                user.setTheme(userSettings.getTheme());
            }

            if (userSettings.getDefaultView() != null) {
                user.setDefaultView(userSettings.getDefaultView());
            }
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}