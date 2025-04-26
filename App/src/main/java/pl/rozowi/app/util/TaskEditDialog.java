package pl.rozowi.app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.controllers.TaskEditController;
import pl.rozowi.app.models.Task;

import java.io.IOException;

/**
 * Utility class for creating and showing the task edit dialog.
 * This provides a convenient way for any controller to open the task editing dialog.
 */
public class TaskEditDialog {

    /**
     * Shows the task edit dialog for the given task.
     *
     * @param task The task to edit
     * @return true if the dialog was shown successfully, false otherwise
     */
    public static boolean showEditDialog(Task task) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/fxml/taskEdit.fxml"));
            Parent root = loader.load();

            // Get the controller and set the task
            TaskEditController controller = loader.getController();
            controller.setTask(task);

            // Create and configure the stage
            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // Show the dialog and wait for it to be closed
            stage.showAndWait();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}