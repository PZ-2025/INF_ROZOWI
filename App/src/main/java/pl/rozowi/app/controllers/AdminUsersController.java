package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pl.rozowi.app.dao.RoleDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Role;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.PasswordChangeService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminUsersController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colLastName;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, Integer> colRole;
    @FXML
    private TextField searchField;

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final PasswordChangeService passwordService = new PasswordChangeService();
    private ObservableList<User> userData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colName.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colLastName.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));
        colEmail.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colRole.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRoleId()).asObject());

        loadAllUsers();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void loadAllUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            userData.setAll(users);
            usersTable.setItems(userData);
        } catch (SQLException e) {
            showError("Błąd ładowania użytkowników", e.getMessage());
        }
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            usersTable.setItems(userData);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<User> filteredData = FXCollections.observableArrayList();

        for (User user : userData) {
            if (user.getName().toLowerCase().contains(lowerCaseFilter) ||
                user.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                filteredData.add(user);
            }
        }

        usersTable.setItems(filteredData);
    }

    @FXML
    private void onAddUser() {
        showUserDialog(null);
    }

    @FXML
    private void onEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika", "Musisz wybrać użytkownika do edycji");
            return;
        }

        showUserDialog(selectedUser);
    }

    @FXML
    private void onDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika", "Musisz wybrać użytkownika do usunięcia");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć użytkownika: " + selectedUser.getName() + " " +
                            selectedUser.getLastName() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Ta funkcja wymaga implementacji w UserDAO
            // userDAO.deleteUser(selectedUser.getId());
            // loadAllUsers();
        }
    }

    private void showUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Dodaj nowego użytkownika" : "Edytuj użytkownika");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Tworzenie siatki formularza
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Imię");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nazwisko");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Hasło");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Powtórz hasło");
        TextField passwordHintField = new TextField();
        passwordHintField.setPromptText("Podpowiedź hasła");

        // Pobieranie listy ról
        ComboBox<Role> roleCombo = new ComboBox<>();
        try {
            // Ta funkcja wymaga implementacji w RoleDAO
            // List<Role> roles = roleDAO.getAllRoles();
            // roleCombo.setItems(FXCollections.observableArrayList(roles));

            // Tymczasowe rozwiązanie - statyczna lista ról
            Role adminRole = new Role();
            adminRole.setId(1);
            adminRole.setRoleName("Administrator");

            Role managerRole = new Role();
            managerRole.setId(2);
            managerRole.setRoleName("Kierownik");

            Role leaderRole = new Role();
            leaderRole.setId(3);
            leaderRole.setRoleName("Team Leader");

            Role userRole = new Role();
            userRole.setId(4);
            userRole.setRoleName("Użytkownik");

            roleCombo.setItems(FXCollections.observableArrayList(adminRole, managerRole, leaderRole, userRole));

            roleCombo.setConverter(new javafx.util.StringConverter<Role>() {
                @Override
                public String toString(Role role) {
                    return role != null ? role.getRoleName() : "";
                }

                @Override
                public Role fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ustawienie wartości dla edycji istniejącego użytkownika
        if (user != null) {
            nameField.setText(user.getName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            passwordHintField.setText(user.getPasswordHint());

            // Ustawienie roli
            int roleId = user.getRoleId();
            for (Role role : roleCombo.getItems()) {
                if (role.getId() == roleId) {
                    roleCombo.setValue(role);
                    break;
                }
            }

            // Przy edycji hasło jest opcjonalne
            passwordField.setPromptText("Pozostaw puste, aby nie zmieniać");
            confirmPasswordField.setPromptText("Pozostaw puste, aby nie zmieniać");
        }

        // Dodanie kontrolek do siatki
        grid.add(new Label("Imię:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Nazwisko:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Hasło:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Powtórz hasło:"), 0, 4);
        grid.add(confirmPasswordField, 1, 4);
        grid.add(new Label("Podpowiedź hasła:"), 0, 5);
        grid.add(passwordHintField, 1, 5);
        grid.add(new Label("Rola:"), 0, 6);
        grid.add(roleCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Konwersja wyniku
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Walidacja pól
                if (nameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    emailField.getText().isEmpty() || roleCombo.getValue() == null) {
                    showWarning("Brakujące dane", "Pola: Imię, Nazwisko, Email i Rola są wymagane.");
                    return null;
                }

                // Walidacja nowego hasła
                if (user == null && (passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty())) {
                    showWarning("Brakujące dane", "Hasło jest wymagane dla nowego użytkownika.");
                    return null;
                }

                // Sprawdzenie czy hasła są takie same
                if (!passwordField.getText().isEmpty() && !passwordField.getText().equals(confirmPasswordField.getText())) {
                    showWarning("Błędne hasło", "Hasła nie są takie same.");
                    return null;
                }

                User result = user != null ? user : new User();
                result.setName(nameField.getText());
                result.setLastName(lastNameField.getText());
                result.setEmail(emailField.getText());
                result.setRoleId(roleCombo.getValue().getId());
                result.setPasswordHint(passwordHintField.getText());

                // Ustawienie hasła tylko jeśli zostało zmienione
                if (!passwordField.getText().isEmpty()) {
                    try {
                        String hashedPassword = passwordService.hashPassword(passwordField.getText());
                        result.setPassword(hashedPassword);
                    } catch (Exception e) {
                        showError("Błąd hasła", "Nie udało się zahashować hasła: " + e.getMessage());
                        return null;
                    }
                }

                return result;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            try {
                if (user == null) {
                    // Nowy użytkownik
                    userDAO.insertUser(u);
                } else {
                    // Aktualizacja użytkownika
                    userDAO.updateUser(u);
                }
                loadAllUsers();
            } catch (Exception e) {
                showError("Błąd", e.getMessage());
            }
        });
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}