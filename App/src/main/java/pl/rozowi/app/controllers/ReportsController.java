package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ReportsController {

    @FXML
    private Button generateButton;
    @FXML
    private TextArea reportsArea;

    @FXML
    private void initialize() {
        // Inicjalizacja, np. wczytanie listy dostępnych raportów
    }

    @FXML
    private void handleGenerateReport() {
        // Przykładowa logika generowania raportu
        reportsArea.setText("Wygenerowano przykładowy raport...");
    }
}
