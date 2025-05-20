package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Kontroler odpowiedzialny za zarządzanie wyświetlaniem i wyszukiwaniem zespołów w systemie.
 * Umożliwia przeglądanie listy zespołów wraz z podstawowymi informacjami oraz wyszukiwanie zespołów po nazwie.
 */
public class TeamsController {

    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private TableView<?> teamsTable;
    @FXML
    private TableColumn<?, ?> colTeamId;
    @FXML
    private TableColumn<?, ?> colTeamName;
    @FXML
    private TableColumn<?, ?> colMembersCount;

    /**
     * Obsługuje akcję wyszukiwania zespołów.
     * Pobiera tekst z pola wyszukiwania i wyświetla go w konsoli (w obecnej wersji).
     * W przyszłości powinna filtrować listę zespołów w tabeli.
     */
    @FXML
    private void handleSearch() {
        String filter = searchField.getText();
        System.out.println("Szukaj zespołu: " + filter);
    }
}
