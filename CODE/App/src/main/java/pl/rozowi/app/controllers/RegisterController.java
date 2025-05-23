package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kontroler odpowiedzialny za proces rejestracji nowego użytkownika w systemie.
 * Zapewnia walidację danych wejściowych i bezpieczne tworzenie kont użytkowników.
 */
public class RegisterController {

    @FXML
    private TextField firstNameField, lastNameField, emailField;
    @FXML
    private PasswordField passwordField, confirmPasswordField;
    @FXML
    private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    /**
     * Inicjalizuje kontroler, ustawiając obsługę zdarzeń klawiatury.
     * Naciśnięcie Enter w dowolnym polu formularza wywołuje próbę rejestracji.
     */
    public void initialize() {
        addEnterKeyHandlers();
    }

    /**
     * Obsługuje zdarzenie naciśnięcia klawisza Enter w polach formularza.
     */
    private void addEnterKeyHandlers() {
        firstNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        lastNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) handleRegister();
        });
    }

    /**
     * Obsługuje proces rejestracji nowego użytkownika.
     * Wykonuje walidację danych i tworzy nowe konto użytkownika.
     */
    @FXML
    private void handleRegister() {
        errorLabel.setText("");

        if (!isCapitalized(firstNameField.getText())) {
            showError("Imię musi zaczynać się od wielkiej litery!");
            return;
        }
        if (!isCapitalized(lastNameField.getText())) {
            showError("Nazwisko musi zaczynać się od wielkiej litery!");
            return;
        }

        if (!isValidEmail(emailField.getText())) {
            showError("Email musi zawierać znak '@' z co najmniej dwoma znakami przed i po nim!");
            return;
        }

        if (!hasSpecialChar(passwordField.getText())) {
            showError("Hasło musi zawierać przynajmniej jeden znak specjalny!");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Hasła nie są takie same!");
            return;
        }

        User newUser = new User();
        newUser.setName(firstNameField.getText());
        newUser.setLastName(lastNameField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(hashPassword(passwordField.getText()));
        newUser.setRoleId(4);   
        newUser.setGroupId(1); 
        newUser.setPasswordHint("");

        boolean inserted = userDAO.insertUser(newUser);
        if (inserted) {
            showSuccessAlert("Rejestracja udana!", "Konto zostało utworzone pomyślnie.");
            try {
                MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Błąd podczas przechodzenia do panelu logowania.");
            }
        } else {
            showError("Rejestracja nie powiodła się! Sprawdź, czy email nie jest już zajęty.");
        }
    }

    /**
     * Przekierowuje użytkownika z powrotem do ekranu logowania.
     * @throws IOException w przypadku problemów z załadowaniem widoku logowania
     */
    @FXML
    private void goBack() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
    }

    /**
     * Wyświetla komunikat o błędzie w interfejsie użytkownika.
     * @param message Treść komunikatu o błędzie
     */
    private void showError(String message) {
        errorLabel.setText(message);
    }

    /**
     * Wyświetla okno dialogowe z informacją o sukcesie.
     * @param title Tytuł okna dialogowego
     * @param message Treść komunikatu
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Haszuje podane hasło algorytmem SHA-256.
     * @param password Hasło do zahaszowania
     * @return Zahaszowane hasło w postaci ciągu znaków hex
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sprawdza czy tekst zaczyna się od wielkiej litery.
     * @param text Tekst do sprawdzenia
     * @return true jeśli tekst zaczyna się od wielkiej litery, false w przeciwnym przypadku
     */
    private boolean isCapitalized(String text) {
        if (text == null || text.isEmpty()) return false;
        return Character.isUpperCase(text.charAt(0));
    }

    /**
     * Sprawdza poprawność formatu adresu email.
     * @param email Adres email do walidacji
     * @return true jeśli email jest poprawny, false w przeciwnym przypadku
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String regex = "^.{2,}@.{2,}$";
        return email.matches(regex);
    }

    /**
     * Sprawdza czy hasło zawiera przynajmniej jeden znak specjalny.
     * @param password Hasło do sprawdzenia
     * @return true jeśli hasło zawiera znak specjalny, false w przeciwnym przypadku
     */
    private boolean hasSpecialChar(String password) {
        if (password == null) return false;
        Pattern pattern = Pattern.compile(".*[^A-Za-z0-9].*");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}