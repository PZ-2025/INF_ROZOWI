package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminTeamsController {

    @FXML
    private TableView<Team> teamsTable;
    @FXML
    private TableColumn<Team, Integer> colId;
    @FXML
    private TableColumn<Team, String> colName;

    @FXML
    private TableView<User> membersTable;
    @FXML
    private TableColumn<User, Integer> colMemberId;
    @FXML
    private TableColumn<User, String> colMemberName;
    @FXML
    private TableColumn<User, String> colMemberEmail;
    @FXML
    private TableColumn<User, Boolean> colIsLeader;

    private final TeamDAO teamDAO = new TeamDAO();
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<Team> teamData = FXCollections.observableArrayList();
    private ObservableList<User> memberData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("teamName"));

        colMemberId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMemberName.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName() + " " + cellData.getValue().getLastName()));
        colMemberEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Słuchacz wyboru zespołu w tabeli
        teamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadTeamMembers(newSelection.getId());
            } else {
                memberData.clear();
            }
        });

        loadAllTeams();
    }

    private void loadAllTeams() {
        try {
            List<Team> teams = teamDAO.getAllTeams();
            teamData.setAll(teams);
            teamsTable.setItems(teamData);
        } catch (SQLException e) {
            showError("Błąd ładowania zespołów", e.getMessage());
        }
    }

    private void loadTeamMembers(int teamId) {
        List<User> members = teamDAO.getTeamMembers(teamId);
        memberData.setAll(members);
        membersTable.setItems(memberData);
    }

    @FXML
    private void onAddTeam() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nowy zespół");
        dialog.setHeaderText("Utwórz nowy zespół");
        dialog.setContentText("Nazwa zespołu:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Team team = new Team();
                team.setTeamName(name);
                try {
                    teamDAO.insertTeam(team);
                    loadAllTeams();
                } catch (Exception e) {
                    showError("Błąd dodawania zespołu", e.getMessage());
                }
            } else {
                showWarning("Błąd", "Nazwa zespołu nie może być pusta");
            }
        });
    }

    @FXML
    private void onEditTeam() {
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Wybierz zespół", "Musisz wybrać zespół do edycji");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedTeam.getTeamName());
        dialog.setTitle("Edycja zespołu");
        dialog.setHeaderText("Zmień nazwę zespołu");
        dialog.setContentText("Nazwa zespołu:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                selectedTeam.setTeamName(name);
                try {
                    teamDAO.updateTeam(selectedTeam);
                    loadAllTeams();
                } catch (SQLException e) {
                    showError("Błąd aktualizacji zespołu", e.getMessage());
                }
            } else {
                showWarning("Błąd", "Nazwa zespołu nie może być pusta");
            }
        });
    }

    @FXML
    private void onDeleteTeam() {
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Wybierz zespół", "Musisz wybrać zespół do usunięcia");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć zespół: " + selectedTeam.getTeamName() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Tu należy zaimplementować usuwanie zespołu
            // Brakuje tej funkcji w klasie TeamDAO
            // teamDAO.deleteTeam(selectedTeam.getId());
            // loadAllTeams();
        }
    }

    @FXML
    private void onAddMember() {
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Wybierz zespół", "Musisz wybrać zespół, do którego dodasz członka");
            return;
        }

        try {
            // Pobierz wszystkich użytkowników, którzy nie są jeszcze w tym zespole
            List<User> allUsers = userDAO.getAllUsers();
            List<User> teamMembers = teamDAO.getTeamMembers(selectedTeam.getId());

            allUsers.removeIf(user -> teamMembers.stream().anyMatch(member -> member.getId() == user.getId()));

            if (allUsers.isEmpty()) {
                showWarning("Brak użytkowników", "Wszyscy użytkownicy są już przypisani do tego zespołu");
                return;
            }

            // Tworzenie okna dialogowego wyboru użytkownika
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Dodaj członka zespołu");
            dialog.setHeaderText("Wybierz użytkownika do dodania do zespołu " + selectedTeam.getTeamName());

            // Dodanie przycisków OK i Anuluj
            ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

            // Tworzenie ComboBox z użytkownikami
            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setItems(FXCollections.observableArrayList(allUsers));
            userComboBox.setValue(allUsers.get(0));
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user != null ? user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")" : "";
                }

                @Override
                public User fromString(String string) {
                    return null;
                }
            });

            // Dodanie ComboBox do dialogu
            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
            vbox.getChildren().addAll(new Label("Użytkownik:"), userComboBox);
            vbox.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));
            dialog.getDialogPane().setContent(vbox);

            // Ustawienie wyniku
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    return userComboBox.getValue();
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            result.ifPresent(user -> {
                // Dodaj użytkownika do zespołu
                try {
                    boolean isLeader = false;  // Domyślnie nie jest liderem
                    teamDAO.insertTeamMember(selectedTeam.getId(), user.getId(), isLeader);
                    loadTeamMembers(selectedTeam.getId());
                } catch (SQLException e) {
                    showError("Błąd dodawania członka", e.getMessage());
                }
            });
        } catch (SQLException e) {
            showError("Błąd pobierania użytkowników", e.getMessage());
        }
    }

    @FXML
    private void onRemoveMember() {
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
        User selectedMember = membersTable.getSelectionModel().getSelectedItem();

        if (selectedTeam == null || selectedMember == null) {
            showWarning("Wybierz zespół i członka", "Musisz wybrać zespół i członka do usunięcia");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia");
        alert.setHeaderText("Czy na pewno chcesz usunąć użytkownika: " + selectedMember.getName() + " " +
                            selectedMember.getLastName() + " z zespołu " + selectedTeam.getTeamName() + "?");
        alert.setContentText("Ta operacja jest nieodwracalna.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                teamDAO.deleteTeamMember(selectedTeam.getId(), selectedMember.getId());
                loadTeamMembers(selectedTeam.getId());
            } catch (Exception e) {
                showError("Błąd usuwania członka", e.getMessage());
            }
        }
    }

    @FXML
    private void onToggleLeader() {
        Team selectedTeam = teamsTable.getSelectionModel().getSelectedItem();
        User selectedMember = membersTable.getSelectionModel().getSelectedItem();

        if (selectedTeam == null || selectedMember == null) {
            showWarning("Wybierz zespół i członka", "Musisz wybrać zespół i członka, aby zmienić status lidera");
            return;
        }

        // Ta funkcja wymaga implementacji w TeamDAO
        // Najpierw usunąć istniejącego członka, a potem dodać go ponownie z przeciwnym statusem lidera
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