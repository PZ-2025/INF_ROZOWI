package pl.rozowi.app.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import pl.rozowi.app.dao.RoleDAO;
import pl.rozowi.app.models.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AdminRolesController {

    @FXML
    private ListView<Role> rolesListView;
    @FXML
    private Label selectedRoleLabel;

    @FXML
    private CheckBox permViewUsers;
    @FXML
    private CheckBox permEditUsers;
    @FXML
    private CheckBox permDeleteUsers;
    @FXML
    private CheckBox permResetPasswords;
    @FXML
    private CheckBox permViewRoles;
    @FXML
    private CheckBox permEditRoles;

    @FXML
    private CheckBox permViewProjects;
    @FXML
    private CheckBox permCreateProjects;
    @FXML
    private CheckBox permEditProjects;
    @FXML
    private CheckBox permDeleteProjects;
    @FXML
    private CheckBox permAssignProjects;

    @FXML
    private CheckBox permViewTeams;
    @FXML
    private CheckBox permCreateTeams;
    @FXML
    private CheckBox permEditTeams;
    @FXML
    private CheckBox permDeleteTeams;
    @FXML
    private CheckBox permAssignTeamMembers;

    @FXML
    private CheckBox permViewTasks;
    @FXML
    private CheckBox permCreateTasks;
    @FXML
    private CheckBox permEditTasks;
    @FXML
    private CheckBox permDeleteTasks;
    @FXML
    private CheckBox permAssignTasks;

    private RoleDAO roleDAO = new RoleDAO();
    private ObservableList<Role> allRoles = FXCollections.observableArrayList();
    private Role currentRole;

    @FXML
    private void initialize() {
        configureRolesListView();

        setPermissionsDisabled(true);

        loadRoles();
    }

    private void configureRolesListView() {
        rolesListView.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                } else {
                    setText(role.getRoleName());
                }
            }
        });

        rolesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRoleLabel.setText(newVal.getRoleName());
                currentRole = newVal;
                loadPermissions(newVal);
                setPermissionsDisabled(false);
            } else {
                selectedRoleLabel.setText("[Wybierz rolę]");
                setPermissionsDisabled(true);
            }
        });
    }

    private void loadRoles() {
        try {
            Role adminRole = new Role();
            adminRole.setId(1);
            adminRole.setRoleName("Administrator");
            adminRole.setPermissions("ALL");

            Role managerRole = new Role();
            managerRole.setId(2);
            managerRole.setRoleName("Kierownik");
            managerRole.setPermissions("VIEW_USERS,VIEW_PROJECTS,EDIT_PROJECTS,VIEW_TEAMS,EDIT_TEAMS,VIEW_TASKS,EDIT_TASKS");

            Role teamLeaderRole = new Role();
            teamLeaderRole.setId(3);
            teamLeaderRole.setRoleName("Team Leader");
            teamLeaderRole.setPermissions("VIEW_USERS,VIEW_PROJECTS,VIEW_TEAMS,VIEW_TASKS,EDIT_TASKS,ASSIGN_TASKS");

            Role employeeRole = new Role();
            employeeRole.setId(4);
            employeeRole.setRoleName("Pracownik");
            employeeRole.setPermissions("VIEW_TASKS,EDIT_OWN_TASKS");

            allRoles.setAll(adminRole, managerRole, teamLeaderRole, employeeRole);
            rolesListView.setItems(allRoles);
        } catch (Exception e) {
            showError("Błąd podczas wczytywania ról", e.getMessage());
        }
    }

    private void loadPermissions(Role role) {

        resetPermissions();

        if (role == null || role.getPermissions() == null) {
            return;
        }

        String[] permissions = role.getPermissions().split(",");

        if (role.getPermissions().equals("ALL")) {
            setAllPermissions(true);
            return;
        }

        for (String perm : permissions) {
            switch (perm.trim()) {
                case "VIEW_USERS" -> permViewUsers.setSelected(true);
                case "EDIT_USERS" -> permEditUsers.setSelected(true);
                case "DELETE_USERS" -> permDeleteUsers.setSelected(true);
                case "RESET_PASSWORDS" -> permResetPasswords.setSelected(true);
                case "VIEW_ROLES" -> permViewRoles.setSelected(true);
                case "EDIT_ROLES" -> permEditRoles.setSelected(true);

                case "VIEW_PROJECTS" -> permViewProjects.setSelected(true);
                case "CREATE_PROJECTS" -> permCreateProjects.setSelected(true);
                case "EDIT_PROJECTS" -> permEditProjects.setSelected(true);
                case "DELETE_PROJECTS" -> permDeleteProjects.setSelected(true);
                case "ASSIGN_PROJECTS" -> permAssignProjects.setSelected(true);

                case "VIEW_TEAMS" -> permViewTeams.setSelected(true);
                case "CREATE_TEAMS" -> permCreateTeams.setSelected(true);
                case "EDIT_TEAMS" -> permEditTeams.setSelected(true);
                case "DELETE_TEAMS" -> permDeleteTeams.setSelected(true);
                case "ASSIGN_TEAM_MEMBERS" -> permAssignTeamMembers.setSelected(true);

                case "VIEW_TASKS" -> permViewTasks.setSelected(true);
                case "CREATE_TASKS" -> permCreateTasks.setSelected(true);
                case "EDIT_TASKS" -> permEditTasks.setSelected(true);
                case "DELETE_TASKS" -> permDeleteTasks.setSelected(true);
                case "ASSIGN_TASKS" -> permAssignTasks.setSelected(true);
            }
        }
    }

    private void resetPermissions() {
        permViewUsers.setSelected(false);
        permEditUsers.setSelected(false);
        permDeleteUsers.setSelected(false);
        permResetPasswords.setSelected(false);
        permViewRoles.setSelected(false);
        permEditRoles.setSelected(false);

        permViewProjects.setSelected(false);
        permCreateProjects.setSelected(false);
        permEditProjects.setSelected(false);
        permDeleteProjects.setSelected(false);
        permAssignProjects.setSelected(false);

        permViewTeams.setSelected(false);
        permCreateTeams.setSelected(false);
        permEditTeams.setSelected(false);
        permDeleteTeams.setSelected(false);
        permAssignTeamMembers.setSelected(false);

        permViewTasks.setSelected(false);
        permCreateTasks.setSelected(false);
        permEditTasks.setSelected(false);
        permDeleteTasks.setSelected(false);
        permAssignTasks.setSelected(false);
    }

    private void setAllPermissions(boolean value) {
        permViewUsers.setSelected(value);
        permEditUsers.setSelected(value);
        permDeleteUsers.setSelected(value);
        permResetPasswords.setSelected(value);
        permViewRoles.setSelected(value);
        permEditRoles.setSelected(value);

        permViewProjects.setSelected(value);
        permCreateProjects.setSelected(value);
        permEditProjects.setSelected(value);
        permDeleteProjects.setSelected(value);
        permAssignProjects.setSelected(value);

        permViewTeams.setSelected(value);
        permCreateTeams.setSelected(value);
        permEditTeams.setSelected(value);
        permDeleteTeams.setSelected(value);
        permAssignTeamMembers.setSelected(value);

        permViewTasks.setSelected(value);
        permCreateTasks.setSelected(value);
        permEditTasks.setSelected(value);
        permDeleteTasks.setSelected(value);
        permAssignTasks.setSelected(value);
    }

    private void setPermissionsDisabled(boolean disabled) {
        permViewUsers.setDisable(disabled);
        permEditUsers.setDisable(disabled);
        permDeleteUsers.setDisable(disabled);
        permResetPasswords.setDisable(disabled);
        permViewRoles.setDisable(disabled);
        permEditRoles.setDisable(disabled);

        permViewProjects.setDisable(disabled);
        permCreateProjects.setDisable(disabled);
        permEditProjects.setDisable(disabled);
        permDeleteProjects.setDisable(disabled);
        permAssignProjects.setDisable(disabled);

        permViewTeams.setDisable(disabled);
        permCreateTeams.setDisable(disabled);
        permEditTeams.setDisable(disabled);
        permDeleteTeams.setDisable(disabled);
        permAssignTeamMembers.setDisable(disabled);

        permViewTasks.setDisable(disabled);
        permCreateTasks.setDisable(disabled);
        permEditTasks.setDisable(disabled);
        permDeleteTasks.setDisable(disabled);
        permAssignTasks.setDisable(disabled);
    }

    @FXML
    private void handleAddRole() {
        Dialog<Role> dialog = createRoleDialog(null);
        Optional<Role> result = dialog.showAndWait();

        result.ifPresent(role -> {
            allRoles.add(role);
            rolesListView.setItems(allRoles);
            showInfo("Dodano nową rolę");
        });
    }

    @FXML
    private void handleEditRole() {
        Role selectedRole = rolesListView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Wybierz rolę do edycji");
            return;
        }

        Dialog<Role> dialog = createRoleDialog(selectedRole);
        Optional<Role> result = dialog.showAndWait();

        result.ifPresent(role -> {
            int index = allRoles.indexOf(selectedRole);
            if (index >= 0) {
                allRoles.set(index, role);
                rolesListView.refresh();

                if (selectedRole.equals(currentRole)) {
                    selectedRoleLabel.setText(role.getRoleName());
                    currentRole = role;
                }

                showInfo("Zaktualizowano rolę");
            }
        });
    }

    @FXML
    private void handleDeleteRole() {
        Role selectedRole = rolesListView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Wybierz rolę do usunięcia");
            return;
        }

        if (selectedRole.getId() <= 4) {
            showWarning("Nie można usunąć domyślnej roli systemowej");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdzenie usunięcia");
        confirmDialog.setHeaderText("Czy na pewno chcesz usunąć rolę?");
        confirmDialog.setContentText("Rola: " + selectedRole.getRoleName());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allRoles.remove(selectedRole);

            if (currentRole != null && currentRole.equals(selectedRole)) {
                selectedRoleLabel.setText("[Wybierz rolę]");
                resetPermissions();
                setPermissionsDisabled(true);
                currentRole = null;
            }

            showInfo("Usunięto rolę");
        }
    }

    @FXML
    private void handleSavePermissions() {
        if (currentRole == null) {
            showWarning("Wybierz rolę, dla której chcesz zapisać uprawnienia");
            return;
        }

        StringBuilder permissions = new StringBuilder();

        if (permViewUsers.isSelected()) permissions.append("VIEW_USERS,");
        if (permEditUsers.isSelected()) permissions.append("EDIT_USERS,");
        if (permDeleteUsers.isSelected()) permissions.append("DELETE_USERS,");
        if (permResetPasswords.isSelected()) permissions.append("RESET_PASSWORDS,");
        if (permViewRoles.isSelected()) permissions.append("VIEW_ROLES,");
        if (permEditRoles.isSelected()) permissions.append("EDIT_ROLES,");

        if (permViewProjects.isSelected()) permissions.append("VIEW_PROJECTS,");
        if (permCreateProjects.isSelected()) permissions.append("CREATE_PROJECTS,");
        if (permEditProjects.isSelected()) permissions.append("EDIT_PROJECTS,");
        if (permDeleteProjects.isSelected()) permissions.append("DELETE_PROJECTS,");
        if (permAssignProjects.isSelected()) permissions.append("ASSIGN_PROJECTS,");

        if (permViewTeams.isSelected()) permissions.append("VIEW_TEAMS,");
        if (permCreateTeams.isSelected()) permissions.append("CREATE_TEAMS,");
        if (permEditTeams.isSelected()) permissions.append("EDIT_TEAMS,");
        if (permDeleteTeams.isSelected()) permissions.append("DELETE_TEAMS,");
        if (permAssignTeamMembers.isSelected()) permissions.append("ASSIGN_TEAM_MEMBERS,");

        if (permViewTasks.isSelected()) permissions.append("VIEW_TASKS,");
        if (permCreateTasks.isSelected()) permissions.append("CREATE_TASKS,");
        if (permEditTasks.isSelected()) permissions.append("EDIT_TASKS,");
        if (permDeleteTasks.isSelected()) permissions.append("DELETE_TASKS,");
        if (permAssignTasks.isSelected()) permissions.append("ASSIGN_TASKS,");

        boolean allSelected = areAllPermissionsSelected();
        String permissionsStr = allSelected ? "ALL" :
                               (permissions.length() > 0 ? permissions.substring(0, permissions.length() - 1) : "");

        currentRole.setPermissions(permissionsStr);

        int index = allRoles.indexOf(currentRole);
        if (index >= 0) {
            allRoles.set(index, currentRole);
            rolesListView.refresh();
            showInfo("Uprawnienia zostały zaktualizowane");
        }
    }

    private boolean areAllPermissionsSelected() {
        return permViewUsers.isSelected() && permEditUsers.isSelected() && permDeleteUsers.isSelected() &&
               permResetPasswords.isSelected() && permViewRoles.isSelected() && permEditRoles.isSelected() &&
               permViewProjects.isSelected() && permCreateProjects.isSelected() &&
               permEditProjects.isSelected() && permDeleteProjects.isSelected() && permAssignProjects.isSelected() &&
               permViewTeams.isSelected() && permCreateTeams.isSelected() &&
               permEditTeams.isSelected() && permDeleteTeams.isSelected() && permAssignTeamMembers.isSelected() &&
               permViewTasks.isSelected() && permCreateTasks.isSelected() &&
               permEditTasks.isSelected() && permDeleteTasks.isSelected() && permAssignTasks.isSelected();
    }

    private Dialog<Role> createRoleDialog(Role role) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle(role == null ? "Dodaj nową rolę" : "Edytuj rolę");
        dialog.setHeaderText(role == null ? "Podaj nazwę nowej roli" : "Edytuj nazwę roli");

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Nazwa roli");

        if (role != null) {
            nameField.setText(role.getRoleName());
        }

        grid.add(new Label("Nazwa roli:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Role result = role != null ? role : new Role();
                result.setRoleName(nameField.getText());

                if (role == null) {
                    result.setPermissions("VIEW_TASKS");

                    result.setId(allRoles.size() + 1);
                }

                return result;
            }
            return null;
        });

        return dialog;
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}