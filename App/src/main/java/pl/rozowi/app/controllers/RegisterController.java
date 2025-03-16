package pl.rozowi.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pl.rozowi.app.MainApplication;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.User;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField firstNameField, lastNameField, emailField;
    @FXML
    private PasswordField passwordField, confirmPasswordField;

    private UserDAO userDAO = new UserDAO();

    public void initialize() {
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

    @FXML
    private void handleRegister() {
        // Walidacja imienia i nazwiska - muszą zaczynać się od wielkiej litery
        if (!isCapitalized(firstNameField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Imię musi zaczynać się od wielkiej litery!");
            return;
        }
        if (!isCapitalized(lastNameField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Nazwisko musi zaczynać się od wielkiej litery!");
            return;
        }

        // Walidacja emaila - musi zawierać przynajmniej jeden @ oraz co najmniej 2 znaki przed i po '@'
        if (!isValidEmail(emailField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Email musi zawierać znak '@' z co najmniej dwoma znakami przed i po nim!");
            return;
        }

        // Walidacja hasła - musi zawierać przynajmniej jeden znak specjalny
        if (!hasSpecialChar(passwordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Hasło musi zawierać przynajmniej jeden znak specjalny!");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Hasła nie są takie same!");
            return;
        }

        User newUser = new User();
        newUser.setName(firstNameField.getText());
        newUser.setLastName(lastNameField.getText());
        newUser.setEmail(emailField.getText());
        newUser.setPassword(passwordField.getText());
        newUser.setRoleId(4); // domyślny: PRACOWNIK
        newUser.setStanowisko("Pracownik");
        newUser.setPasswordHint("");

        boolean inserted = userDAO.insertUser(newUser);
        if (inserted) {
            showAlert(Alert.AlertType.INFORMATION, "Rejestracja udana", "Rejestracja udana!");
            try {
                MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Rejestracja nieudana", "Rejestracja nie powiodła się!");
        }
    }

    @FXML
    private void goBack() throws IOException {
        MainApplication.switchScene("/fxml/login.fxml", "TaskApp - Panel logowania");
    }

    // Metoda pomocnicza do wyświetlania modali
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // W zależności od typu alertu możesz dostosować styl (na przykład zmieniając tło)
        if (type == Alert.AlertType.ERROR) {
            alert.getDialogPane().setStyle("-fx-background-color: red;");
        } else if (type == Alert.AlertType.INFORMATION) {
            alert.getDialogPane().setStyle("-fx-background-color: green;");
        }
        alert.showAndWait();
    }

    // Sprawdza, czy ciąg zaczyna się od wielkiej litery
    private boolean isCapitalized(String text) {
        if (text == null || text.isEmpty()) return false;
        return Character.isUpperCase(text.charAt(0));
    }

    // Walidacja emaila: minimum 2 znaki przed i po '@'
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        // Prosty regex: minimum 2 znaki przed @, jeden @, minimum 2 znaki po @
        String regex = "^.{2,}@.{2,}$";
        return email.matches(regex);
    }

    // Walidacja hasła: sprawdza, czy zawiera przynajmniej jeden znak specjalny (nie literę ani cyfrę)
    private boolean hasSpecialChar(String password) {
        if (password == null) return false;
        Pattern pattern = Pattern.compile(".*[^A-Za-z0-9].*");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
