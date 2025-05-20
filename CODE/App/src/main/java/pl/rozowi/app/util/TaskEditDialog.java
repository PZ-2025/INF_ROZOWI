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
 * Klasa odpowiedzialna za wyświetlanie i zarządzanie oknem dialogowym edycji zadania.
 * Okno jest modalne i blokuje interakcję z innymi częściami aplikacji do momentu zamknięcia.
 */
public class TaskEditDialog {

    /**
     * Wyświetla modalne okno dialogowe do edycji zadania.
     * Ładuje widok z pliku FXML, ustawia kontroler i przekazuje do niego zadanie do edycji.
     * Okno jest wyświetlane w trybie modalnym blokującym interakcję z resztą aplikacji.
     *
     * @param task Zadanie do edycji, które będzie przekazane do kontrolera. Jeśli null, zostanie utworzone nowe zadanie.
     * @return true jeśli okno zostało poprawnie wyświetlone i zamknięte,
     *         false w przypadku błędu ładowania widoku FXML
     * @throws IOException jeśli wystąpi problem z ładowaniem pliku FXML
     * @see TaskEditController
     * @see Task
     */
    public static boolean showEditDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/fxml/taskEdit.fxml"));
            Parent root = loader.load();

            TaskEditController controller = loader.getController();
            controller.setTask(task);

            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            stage.showAndWait();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}