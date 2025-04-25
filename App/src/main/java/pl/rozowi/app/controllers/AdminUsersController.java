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
import pl.rozowi.app.dao.TeamMemberDAO;
import pl.rozowi.app.dao.RoleDAO;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Role;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;
import pl.rozowi.app.services.PasswordChangeService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    private TableColumn<User, String> colGroup;

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
    private Label detailGroup;
    @FXML
    private Label detailCreatedAt;
    @FXML
    private Label detailLastPasswordChange;

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final TeamMemberDAO teamMemberDAO = new TeamMemberDAO();
    private final PasswordChangeService passwordService = new PasswordChangeService();

    // Mapy z nazwami ról i grup pobierane z bazy danych
    private Map<Integer, String> groupNames = new HashMap<>();
    private Map<Integer, String> roleNames = new HashMap<>();

    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Inicjalizacja map ról i grup z bazy danych
        loadRolesAndGroups();

        // Konfiguracja kolumn
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colLastName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colGroup.setCellValueFactory(data -> {
            int groupId = data.getValue().getGroupId();
            return new SimpleStringProperty(groupNames.getOrDefault(groupId, "Brak grupy"));
        });

        // Konfiguracja kolumny roli - używa map z bazy danych
        colRole.setCellValueFactory(data -> {
            int roleId = data.getValue().getRoleId();
            return new SimpleStringProperty(roleNames.getOrDefault(roleId, String.valueOf(roleId)));
        });

        colTeam.setCellValueFactory(data -> {
            int userId = data.getValue().getId();
            String teamName = "Brak przypisania";
            try {
                int teamId = teamMemberDAO.getTeamIdForUser(userId);
                if (teamId > 0) {
                    teamName = teamDAO.getTeamNameById(teamId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new SimpleStringProperty(teamName);
        });

        // Obsługa kliknięcia na wiersz
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showUserDetails(newSelection);
            }
        });

        // Wczytaj dane
        loadUsers();
    }

    /**
     * Pobiera role i grupy z bazy danych
     */
    private void loadRolesAndGroups() {
        try {
            // Pobierz role z bazy
            roleNames = userDAO.getAllRolesMap();
            if (roleNames.isEmpty()) {
                // Awaryjnie, jeśli nie udało się pobrać z bazy - używamy wartości domyślnych
                roleNames.put(1, "Administrator");
                roleNames.put(2, "Kierownik");
                roleNames.put(3, "Team Leader");
                roleNames.put(4, "Pracownik");
            }

            // Pobierz grupy z bazy
            groupNames = userDAO.getAllGroupsMap();
            if (groupNames.isEmpty()) {
                // Awaryjnie, jeśli nie udało się pobrać z bazy - używamy wartości domyślnych
                groupNames.put(1, "Deweloperzy");
                groupNames.put(2, "Testerzy");
                groupNames.put(3, "Projektanci");
                groupNames.put(4, "Analitycy");
                groupNames.put(5, "DevOps");
                groupNames.put(6, "Wsparcie");
                groupNames.put(7, "QA");
                groupNames.put(8, "Business Analyst");
                groupNames.put(9, "HR");
                groupNames.put(10, "Kierownictwo");
            }
        } catch (Exception ex) {
            showError("Błąd podczas ładowania ról i grup: " + ex.getMessage());
            ex.printStackTrace();
        }
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

        // Pobierz nazwę roli z mapy
        detailRole.setText(roleNames.getOrDefault(user.getRoleId(), "Nieznana"));
        detailGroup.setText(groupNames.getOrDefault(user.getGroupId(), "Brak grupy"));

        // Pobierz nazwę zespołu
        try {
            int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
            String teamName = "Brak przypisania";
            if (teamId > 0) {
                teamName = teamDAO.getTeamNameById(teamId);
            }
            detailTeam.setText(teamName);
        } catch (Exception e) {
            detailTeam.setText("Błąd pobierania zespołu");
            e.printStackTrace();
        }

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
        Dialog<UserTeamPair> dialog = createUserDialog(null);
        Optional<UserTeamPair> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            User user = pair.user;
            Team selectedTeam = pair.team;

            // Hash hasła
            user.setPassword(passwordService.hashPassword("DefaultPass123!"));

            // Dodaj użytkownika do bazy
            boolean success = userDAO.insertUser(user);
            if (success) {
                // Pobierz ID nowego użytkownika, aby utworzyć relację z zespołem
                User addedUser = userDAO.getUserByEmail(user.getEmail());
                if (addedUser != null && selectedTeam != null) {
                    try {
                        // Dodanie relacji użytkownik-zespół
                        teamMemberDAO.insertTeamMember(selectedTeam.getId(), addedUser.getId(), false);
                    } catch (SQLException e) {
                        showError("Użytkownik został dodany, ale wystąpił błąd przy przypisywaniu zespołu: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
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

        // Pobierz pełne dane użytkownika, łącznie z hasłem
        User fullUserData = userDAO.getUserById(selectedUser.getId());
        if (fullUserData == null) {
            showError("Nie można pobrać pełnych danych użytkownika");
            return;
        }

        Dialog<UserTeamPair> dialog = createUserDialog(fullUserData);
        Optional<UserTeamPair> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            User updatedUser = pair.user;
            Team selectedTeam = pair.team;

            // Zachowanie oryginalnego hasła
            updatedUser.setPassword(fullUserData.getPassword());

            // Aktualizuj użytkownika w bazie
            boolean success = userDAO.updateUser(updatedUser);

            // Aktualizuj przypisanie do zespołu, jeśli zostało zmienione
            if (success && selectedTeam != null) {
                teamMemberDAO.updateUserTeam(updatedUser.getId(), selectedTeam.getId());
            }

            if (success) {
                loadUsers();
                showInfo("Zaktualizowano użytkownika");
            } else {
                showError("Błąd podczas aktualizacji użytkownika");
            }
        });
    }

    /**
     * Klasa pomocnicza przechowująca parę użytkownik-zespół
     */
    private static class UserTeamPair {
        User user;
        Team team;

        UserTeamPair(User user, Team team) {
            this.user = user;
            this.team = team;
        }
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
            try {
                // Najpierw usuń powiązania użytkownika z zespołami
                int teamId = teamMemberDAO.getTeamIdForUser(selectedUser.getId());
                if (teamId > 0) {
                    teamMemberDAO.deleteTeamMember(teamId, selectedUser.getId());
                }

                // Następnie usuń użytkownika
                boolean deleted = userDAO.deleteUser(selectedUser.getId());
                if (deleted) {
                    allUsers.remove(selectedUser);
                    showInfo("Użytkownik został usunięty");
                } else {
                    showError("Nie udało się usunąć użytkownika z bazy danych");
                }
            } catch (Exception e) {
                showError("Błąd podczas usuwania użytkownika: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do zresetowania hasła");
            return;
        }

        // Pobierz pełne dane użytkownika, łącznie z hasłem
        User fullUserData = userDAO.getUserById(selectedUser.getId());
        if (fullUserData == null) {
            showError("Nie można pobrać pełnych danych użytkownika");
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

            // Skopiuj dane z pełnego obiektu użytkownika
            fullUserData.setPassword(hashedPassword);

            // Aktualizacja w bazie
            boolean success = userDAO.updateUser(fullUserData);
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

        // Pobierz pełne dane użytkownika
        User fullUserData = userDAO.getUserById(selectedUser.getId());
        if (fullUserData == null) {
            showError("Nie można pobrać pełnych danych użytkownika");
            return;
        }

        // Tworzenie dialogu wyboru roli
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Zmiana roli");
        dialog.setHeaderText("Wybierz nową rolę dla użytkownika:\n" + selectedUser.getName() + " " + selectedUser.getLastName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Przygotuj ComboBox z rolami
        ComboBox<String> roleComboBox = new ComboBox<>();

        // Dodaj role z mapy ról
        for (Map.Entry<Integer, String> entry : roleNames.entrySet()) {
            roleComboBox.getItems().add(entry.getValue());
        }

        // Ustaw domyślną wartość na obecną rolę
        int currentRoleId = selectedUser.getRoleId();
        String currentRoleName = roleNames.getOrDefault(currentRoleId, "Nieznana");
        roleComboBox.setValue(currentRoleName);

        VBox content = new VBox(10);
        content.getChildren().add(roleComboBox);
        dialogPane.setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedRole = roleComboBox.getValue();
                // Znajdź ID wybranej roli
                for (Map.Entry<Integer, String> entry : roleNames.entrySet()) {
                    if (entry.getValue().equals(selectedRole)) {
                        return entry.getKey();
                    }
                }
                return currentRoleId; // Jeśli nie znaleziono, zwróć obecne ID
            }
            return null;
        });

        Optional<Integer> newRoleId = dialog.showAndWait();
        newRoleId.ifPresent(roleId -> {
            // Aktualizuj rolę użytkownika
            fullUserData.setRoleId(roleId);
            boolean success = userDAO.updateUser(fullUserData);
            if (success) {
                // Zaktualizuj również widok lokalny
                selectedUser.setRoleId(roleId);
                loadUsers(); // Odśwież całą listę
                showInfo("Rola użytkownika została zmieniona");
            } else {
                showError("Błąd podczas zmiany roli użytkownika");
            }
        });
    }

    @FXML
    private void handleChangeGroup() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Wybierz użytkownika do zmiany grupy");
            return;
        }

        // Pobierz pełne dane użytkownika
        User fullUserData = userDAO.getUserById(selectedUser.getId());
        if (fullUserData == null) {
            showError("Nie można pobrać pełnych danych użytkownika");
            return;
        }

        // Tworzenie dialogu wyboru grupy
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Zmiana grupy");
        dialog.setHeaderText("Wybierz nową grupę dla użytkownika:\n" + selectedUser.getName() + " " + selectedUser.getLastName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Przygotuj ComboBox z grupami
        ComboBox<String> groupComboBox = new ComboBox<>();

        // Dodaj grupy z mapy grup
        for (Map.Entry<Integer, String> entry : groupNames.entrySet()) {
            groupComboBox.getItems().add(entry.getValue());
        }

        // Ustaw domyślną wartość na obecną grupę
        int currentGroupId = selectedUser.getGroupId();
        String currentGroupName = groupNames.getOrDefault(currentGroupId, "Brak grupy");
        groupComboBox.setValue(currentGroupName);

        VBox content = new VBox(10);
        content.getChildren().add(groupComboBox);
        dialogPane.setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String selectedGroup = groupComboBox.getValue();
                // Znajdź ID wybranej grupy
                for (Map.Entry<Integer, String> entry : groupNames.entrySet()) {
                    if (entry.getValue().equals(selectedGroup)) {
                        return entry.getKey();
                    }
                }
                return currentGroupId; // Jeśli nie znaleziono, zwróć obecne ID
            }
            return null;
        });

        Optional<Integer> newGroupId = dialog.showAndWait();
        newGroupId.ifPresent(groupId -> {
            // Aktualizuj grupę użytkownika
            fullUserData.setGroupId(groupId);
            boolean success = userDAO.updateUser(fullUserData);
            if (success) {
                // Zaktualizuj również widok lokalny
                selectedUser.setGroupId(groupId);
                loadUsers(); // Odśwież całą listę
                showInfo("Grupa użytkownika została zmieniona");
            } else {
                showError("Błąd podczas zmiany grupy użytkownika");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadRolesAndGroups();
        loadUsers();
    }

    private Dialog<UserTeamPair> createUserDialog(User user) {
        // Tworzenie nowego dialogu
        Dialog<UserTeamPair> dialog = new Dialog<>();
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

        // Kombo dla roli - używamy dynamicznie pobranych ról
        ComboBox<String> roleComboBox = new ComboBox<>();
        for (Map.Entry<Integer, String> entry : roleNames.entrySet()) {
            roleComboBox.getItems().add(entry.getValue());
        }
        roleComboBox.setValue(roleNames.getOrDefault(4, "Pracownik")); // Domyślnie pracownik

        // Kombo dla grupy - używamy dynamicznie pobranych grup
        ComboBox<String> groupComboBox = new ComboBox<>();
        for (Map.Entry<Integer, String> entry : groupNames.entrySet()) {
            groupComboBox.getItems().add(entry.getValue());
        }
        groupComboBox.setValue(groupNames.getOrDefault(1, "Deweloperzy")); // Domyślnie deweloperzy

        // Kombo dla zespołu
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
            int roleId = user.getRoleId();
            String roleName = roleNames.getOrDefault(roleId, "Pracownik");
            roleComboBox.setValue(roleName);

            // Wybierz odpowiednią grupę
            int groupId = user.getGroupId();
            String groupName = groupNames.getOrDefault(groupId, "Deweloperzy");
            groupComboBox.setValue(groupName);

            // Próba znalezienia i ustawienia zespołu
            try {
                int teamId = teamMemberDAO.getTeamIdForUser(user.getId());
                if (teamId > 0) {
                    for (Team team : teamComboBox.getItems()) {
                        if (team.getId() == teamId) {
                            teamComboBox.setValue(team);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        grid.add(new Label("Grupa:"), 0, 4);
        grid.add(groupComboBox, 1, 4);
        grid.add(new Label("Zespół:"), 0, 5);
        grid.add(teamComboBox, 1, 5);

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

                // Znajdź ID roli na podstawie wybranej nazwy
                String selectedRole = roleComboBox.getValue();
                for (Map.Entry<Integer, String> entry : roleNames.entrySet()) {
                    if (entry.getValue().equals(selectedRole)) {
                        result.setRoleId(entry.getKey());
                        break;
                    }
                }

                // Znajdź ID grupy na podstawie wybranej nazwy
                String selectedGroup = groupComboBox.getValue();
                for (Map.Entry<Integer, String> entry : groupNames.entrySet()) {
                    if (entry.getValue().equals(selectedGroup)) {
                        result.setGroupId(entry.getKey());
                        break;
                    }
                }

                Team selectedTeam = teamComboBox.getValue();
                return new UserTeamPair(result, selectedTeam);
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