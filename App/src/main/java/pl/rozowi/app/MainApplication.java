package pl.rozowi.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
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

    public static void main(String[] args) {
        launch();
    }
}
