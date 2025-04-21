package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.dao.UserDAO;
import pl.rozowi.app.models.Team;
import pl.rozowi.app.models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManagerTeamsController {

    @FXML
    private TableView<Team> teamsTable;
    @FXML
    private TableColumn<Team, Number> colId;
    @FXML
    private TableColumn<Team, String> colName;

    private final TeamDAO teamDAO = new TeamDAO();
    private final ObservableList<Team> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colName.setCellValueFactory(c -> c.getValue().teamNameProperty());
        loadAll();
    }

    private void loadAll() throws SQLException {
        data.setAll(teamDAO.getAllTeams());
        teamsTable.setItems(data);
    }

    @FXML
    private void onAddTeam() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Nazwa nowego zespołu:");
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(name -> {
            Team t = new Team();
            t.setTeamName(name);
            teamDAO.insertTeam(t);
            try {
                loadAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    private void onEditTeam() {
        Team sel = teamsTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        TextInputDialog dlg = new TextInputDialog(sel.getTeamName());
        dlg.setHeaderText("Nowa nazwa:");
        Optional<String> res = dlg.showAndWait();
        res.ifPresent(name -> {
            sel.setTeamName(name);
            try {
                teamDAO.updateTeam(sel);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                loadAll();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    private void onAssignMembers() throws SQLException {
        Team selected = teamsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        List<User> all = new UserDAO().getAllUsers();
        List<User> members = new TeamDAO().getTeamMembers(selected.getId());

        Dialog<List<User>> dlg = new Dialog<>();
        dlg.setTitle("Przypisz członków do zespołu");
        ListView<User> listView = new ListView<>(FXCollections.observableArrayList(all));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // wstępnie zaznacz obecnych
        for (User u : members) listView.getSelectionModel().select(u);
        dlg.getDialogPane().setContent(listView);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setResultConverter(btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedItems() : null);

        Optional<List<User>> res = dlg.showAndWait();
        res.ifPresent(chosen -> {
            TeamDAO dao = new TeamDAO();
            // usuń wszystkich starych
            for (User u : members) dao.deleteTeamMember(selected.getId(), u.getId());
            // wstaw nowych (domyślnie bez lidera)
            for (User u : chosen) {
                try {
                    dao.insertTeamMember(selected.getId(), u.getId(), false);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            // to do: rozbudować checkboxem „lider zespołu”
        });
    }
}
