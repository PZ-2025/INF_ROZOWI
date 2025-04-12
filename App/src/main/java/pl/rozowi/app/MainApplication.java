package pl.rozowi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.rozowi.app.database.DatabaseManager;
import pl.rozowi.app.models.User;

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
        Scene scene = new Scene(loader.load());
        stage.setTitle("TaskApp - Start");
        stage.setFullScreen(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
        primaryStage.getScene().setRoot(loader.load());
        primaryStage.setTitle(title);
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        launch();
    }
}
