package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.RoleDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Role;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.PasswordChangeService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colTeam;
    @FXML
    private TableColumn<User, Timestamp> colLastLogin;

    @FXML
    private TextField searchField;

    @FXML
    private Label detailId;
    @FXML
    private Label detailName;
    @FXML
    private Label detailEmail;
    @FXML
    private Label detailRole;
    @FXML
    private Label detailTeam;
    @FXML
    private Label detailCreatedAt;
    @FXML
    private Label detailLastPasswordChange;

    private UserDAO userDAO = new UserDAO();
    private RoleDAO roleDAO = new RoleDAO();
    private TeamDAO teamDAO = new TeamDAO();
    private PasswordChangeService passwordService = new PasswordChangeService();

    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Konfiguracja kolumn
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colLastName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> {
            int roleId = data.getValue().getRoleId();
            String roleName = "Nieznana";
            try {
                // Tu powinna być implementacja pobierająca nazwę roli
                switch (roleId) {
                    case 1 -> roleName = "Administrator";
                    case 2 -> roleName = "Kierownik";
                    case 3 -> roleName = "Team Leader";
                    case 4 -> roleName = "Pracownik";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(roleName);
        });
        colTeam.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTeamName()));

        // Obsługa kliknięcia na wiersz
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showUserDetails(newSelection);
            }
        });

        // Wczytaj dane
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            allUsers.setAll(users);
            usersTable.setItems(allUsers);
        } catch (SQLException e) {
            showError("Błąd podczas wczytywania użytkowników: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showUserDetails(User user) {
        detailId.setText(String.valueOf(user.getId()));
        detailName.setText(user.getName() + " " + user.getLastName());
        detailEmail.setText(user.getEmail());

        // Pobierz nazwę roli
        String roleName = "Nieznana";
        switch (user.getRoleId()) {
            case 1 -> roleName = "Administrator";
            case 2 -> roleName = "Kierownik";
            case 3 -> roleName = "Team Leader";
            case 4 -> roleName = "Pracownik";
        }
        detailRole.setText(roleName);

        // Pobierz nazwę zespołu
        detailTeam.setText(user.getTeamName() != null ? user.getTeamName() : "Brak przypisania");

        // Data utworzenia i ostatnia zmiana hasła (przykładowe dane)
        detailCreatedAt.setText("2025-04-01");
        detailLastPasswordChange.setText("2025-04-15");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            usersTable.setItems(allUsers);
            return;
        }

        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : allUsers) {
            if (String.valueOf(user.getId()).contains(searchText) ||
                user.getName().toLowerCase().contains(searchText) ||
                user.getLastName().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText)) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    @FXML
    private void handleAddUser() {
        Dialog<User> dialog = createUserDialog(null);
        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            // Hash hasła
            user.setPassword(passwordService.hashPassword("DefaultPass123!"));

            // Dodaj użytkownika do bazy
            boolean success = userDAO.insertUser(user);
            if (success) {
                loadUsers();
                showInfo("Dodano nowego użytkownika");
            } else {
                showError("Błąd podczas dodawania użytkownika");
            }
        });
    }

    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do edycji");
            return;
        }

        Dialog<User> dialog = createUserDialog(selectedUser);
        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            // Zachowaj oryginalne hasło użytkownika
            user.setPassword(selectedUser.getPassword());

            // Zaktualizuj użytkownika w bazie
            boolean success = userDAO.updateUser(user);
            if (success) {
                loadUsers();
                showInfo("Zaktualizowano użytkownika");
            } else {
                showError("Błąd podczas aktualizacji użytkownika");
            }
        });
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do usunięcia");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć użytkownika?");
        confirmDialog.setContentText("Użytkownik: " + selectedUser.getName() + " " + selectedUser.getLastName()
                                    + " (" + selectedUser.getEmail() + ")");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Tu powinna być implementacja usuwania użytkownika
            // boolean success = userDAO.deleteUser(selectedUser.getId());

            // Tymczasowo: symulacja usunięcia
            allUsers.remove(selectedUser);
            showInfo("Użytkownik został usunięty");
        }
    }

    @FXML
    private void handleResetPassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do zresetowania hasła");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Reset hasła");
        confirmDialog.setHeaderText("Czy na pewno chcesz zresetować hasło dla użytkownika?");
        confirmDialog.setContentText("Użytkownik: " + selectedUser.getName() + " " + selectedUser.getLastName());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Generowanie nowego standardowego hasła i hashowanie
            String newPassword = "ResetPass123!";
            String hashedPassword = passwordService.hashPassword(newPassword);
            selectedUser.setPassword(hashedPassword);

            // Aktualizacja w bazie
            boolean success = userDAO.updateUser(selectedUser);
            if (success) {
                showInfo("Hasło zostało zresetowane do: " + newPassword);
            } else {
                showError("Błąd podczas resetowania hasła");
            }
        }
    }

    @FXML
    private void handleChangeRole() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do zmiany roli");
            return;
        }

        // Tworzenie dialogu wyboru roli
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Zmiana roli");
        dialog.setHeaderText("Wybierz nową rolę dla użytkownika:\n" + selectedUser.getName() + " " + selectedUser.getLastName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Role> roleComboBox = new ComboBox<>();
        ObservableList<Role> roles = FXCollections.observableArrayList();

        // Dodaj role (zwykle powinny być pobrane z bazy)
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setRoleName("Administrator");

        Role managerRole = new Role();
        managerRole.setId(2);
        managerRole.setRoleName("Kierownik");

        Role teamLeaderRole = new Role();
        teamLeaderRole.setId(3);
        teamLeaderRole.setRoleName("Team Leader");

        Role employeeRole = new Role();
        employeeRole.setId(4);
        employeeRole.setRoleName("Pracownik");

        roles.addAll(adminRole, managerRole, teamLeaderRole, employeeRole);
        roleComboBox.setItems(roles);

        // Ustaw domyślną wartość na obecną rolę
        roles.stream()
             .filter(r -> r.getId() == selectedUser.getRoleId())
             .findFirst()
             .ifPresent(roleComboBox::setValue);

        // Konwerter do wyświetlania nazw ról
        roleComboBox.setConverter(new javafx.util.StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role != null ? role.getRoleName() : "";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        VBox content = new VBox(10);
        content.getChildren().add(roleComboBox);
        dialogPane.setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Role selectedRole = roleComboBox.getValue();
                return selectedRole != null ? selectedRole.getId() : null;
            }
            return null;
        });

        Optional<Integer> newRoleId = dialog.showAndWait();
        newRoleId.ifPresent(roleId -> {
            // Aktualizuj rolę użytkownika
            selectedUser.setRoleId(roleId);
            boolean success = userDAO.updateUser(selectedUser);
            if (success) {
                loadUsers();
                showInfo("Rola użytkownika została zmieniona");
            } else {
                showError("Błąd podczas zmiany roli użytkownika");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private Dialog<User> createUserDialog(User user) {
        // Tworzenie nowego dialogu
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Dodaj nowego użytkownika" : "Edytuj użytkownika");
        dialog.setHeaderText(user == null ? "Wprowadź dane nowego użytkownika" : "Edytuj dane użytkownika");

        // Dodanie przycisków
        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Tworzenie pól formularza
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Imię");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nazwisko");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        ComboBox<Role> roleComboBox = new ComboBox<>();
        ObservableList<Role> roles = FXCollections.observableArrayList();

        // Dodaj role (zwykle powinny być pobrane z bazy)
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setRoleName("Administrator");

        Role managerRole = new Role();
        managerRole.setId(2);
        managerRole.setRoleName("Kierownik");

        Role teamLeaderRole = new Role();
        teamLeaderRole.setId(3);
        teamLeaderRole.setRoleName("Team Leader");

        Role employeeRole = new Role();
        employeeRole.setId(4);
        employeeRole.setRoleName("Pracownik");

        roles.addAll(adminRole, managerRole, teamLeaderRole, employeeRole);
        roleComboBox.setItems(roles);

        // Konwerter dla ComboBox
        roleComboBox.setConverter(new javafx.util.StringConverter<Role>() {
            @Override
            public String toString(Role role) {
                return role != null ? role.getRoleName() : "";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        ComboBox<Team> teamComboBox = new ComboBox<>();
        try {
            List<Team> teams = teamDAO.getAllTeams();
            teamComboBox.setItems(FXCollections.observableArrayList(teams));

            // Konwerter dla teamComboBox
            teamComboBox.setConverter(new javafx.util.StringConverter<Team>() {
                @Override
                public String toString(Team team) {
                    return team != null ? team.getTeamName() : "";
                }

                @Override
                public Team fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Jeśli edytujemy użytkownika, ustawiamy wartości pól
        if (user != null) {
            nameField.setText(user.getName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());

            // Wybierz odpowiednią rolę
            roles.stream()
                 .filter(r -> r.getId() == user.getRoleId())
                 .findFirst()
                 .ifPresent(roleComboBox::setValue);

            // Próba znalezienia i ustawienia zespołu
            try {
                int teamId = new TeamMemberDAO().getTeamIdForUser(user.getId());
                if (teamId > 0) {
                    teamComboBox.getItems().stream()
                        .filter(t -> t.getId() == teamId)
                        .findFirst()
                        .ifPresent(teamComboBox::setValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Domyślne wartości dla nowego użytkownika
            roleComboBox.setValue(employeeRole);  // Domyślnie pracownik
        }

        // Dodanie pól do formularza
        grid.add(new Label("Imię:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Nazwisko:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Rola:"), 0, 3);
        grid.add(roleComboBox, 1, 3);
        grid.add(new Label("Zespół:"), 0, 4);
        grid.add(teamComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Ustawienie fokusa
        Platform.runLater(nameField::requestFocus);

        // Walidacja danych przed zapisem
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Dodanie listenerów do pól formularza do walidacji
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm(saveButton, nameField, lastNameField, emailField);
        });

        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm(saveButton, nameField, lastNameField, emailField);
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm(saveButton, nameField, lastNameField, emailField);
        });

        // Konwersja wyniku dialogu
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User result = user != null ? user : new User();
                result.setName(nameField.getText());
                result.setLastName(lastNameField.getText());
                result.setEmail(emailField.getText());

                Role selectedRole = roleComboBox.getValue();
                if (selectedRole != null) {
                    result.setRoleId(selectedRole.getId());
                }

                Team selectedTeam = teamComboBox.getValue();
                if (selectedTeam != null) {
                    // Przypisanie zespołu (powinno być obsługiwane w DAO)
                    // userTeamDAO.assignUserToTeam(result.getId(), selectedTeam.getId());
                    result.setTeamName(selectedTeam.getTeamName());
                }

                return result;
            }
            return null;
        });

        return dialog;
    }

    private void validateForm(Node saveButton, TextField nameField, TextField lastNameField, TextField emailField) {
        boolean isValid = !nameField.getText().trim().isEmpty() &&
                         !lastNameField.getText().trim().isEmpty() &&
                         isValidEmail(emailField.getText().trim());

        saveButton.setDisable(!isValid);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacja");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ostrzeżenie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}