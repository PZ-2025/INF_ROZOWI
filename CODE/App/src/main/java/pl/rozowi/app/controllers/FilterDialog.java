package pl.rozowi.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.rozowi.app.dao.TeamDAO;
import pl.rozowi.app.models.Team;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alternatywne rozwiązanie - okno dialogowe do filtrowania raportów zamiast
 * zintegrowanych kontrolek w głównym interfejsie raportów.
 */
public class FilterDialog {

    private TeamDAO teamDAO = new TeamDAO();
    private List<Team> selectedTeams = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean showTasks = true;
    private boolean showMembers = true;
    private boolean showStatistics = true;

    public Map<String, Object> showFilterDialog(String reportType) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Opcje filtrowania raportu");
        dialog.setHeaderText("Dostosuj opcje dla raportu: " + reportType);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        switch (reportType) {
            case "Struktura Zespołów":
                setupTeamsFilterControls(grid);
                setupTaskMemberControls(grid);
                break;
            case "Użytkownicy Systemu":
                setupMemberFilterControls(grid);
                break;
            case "Przegląd Projektów":
                setupDateFilterControls(grid);
                setupTaskStatsControls(grid);
                break;
            default:
                setupTeamsFilterControls(grid);
                setupDateFilterControls(grid);
                setupTaskMemberControls(grid);
                setupTaskStatsControls(grid);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("selectedTeams", selectedTeams);
                result.put("startDate", startDate);
                result.put("endDate", endDate);
                result.put("showTasks", showTasks);
                result.put("showMembers", showMembers);
                result.put("showStatistics", showStatistics);
                return result;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void setupTeamsFilterControls(GridPane grid) {
        try {
            List<Team> teams = teamDAO.getAllTeams();

            Label teamsLabel = new Label("Wybierz zespoły:");
            grid.add(teamsLabel, 0, 0);

            ListView<CheckBox> teamsListView = new ListView<>();
            ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();

            for (Team team : teams) {
                CheckBox cb = new CheckBox(team.getTeamName());
                cb.setSelected(false);
                cb.setUserData(team);
                cb.setOnAction(e -> {
                    if (cb.isSelected()) {
                        selectedTeams.add((Team) cb.getUserData());
                    } else {
                        selectedTeams.remove(cb.getUserData());
                    }
                });
                checkBoxes.add(cb);
            }

            teamsListView.setItems(checkBoxes);
            teamsListView.setPrefHeight(150);
            grid.add(teamsListView, 1, 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupDateFilterControls(GridPane grid) {
        Label startDateLabel = new Label("Data początkowa:");
        grid.add(startDateLabel, 0, 1);

        DatePicker startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        startDatePicker.setOnAction(e -> startDate = startDatePicker.getValue());
        grid.add(startDatePicker, 1, 1);

        Label endDateLabel = new Label("Data końcowa:");
        grid.add(endDateLabel, 0, 2);

        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setOnAction(e -> endDate = endDatePicker.getValue());
        grid.add(endDatePicker, 1, 2);

        startDate = startDatePicker.getValue();
        endDate = endDatePicker.getValue();
    }

    private void setupTaskMemberControls(GridPane grid) {
        CheckBox showTasksCheckBox = new CheckBox("Pokaż zadania zespołów");
        showTasksCheckBox.setSelected(true);
        showTasksCheckBox.setOnAction(e -> showTasks = showTasksCheckBox.isSelected());
        grid.add(showTasksCheckBox, 0, 3, 2, 1);

        CheckBox showMembersCheckBox = new CheckBox("Pokaż członków zespołów");
        showMembersCheckBox.setSelected(true);
        showMembersCheckBox.setOnAction(e -> showMembers = showMembersCheckBox.isSelected());
        grid.add(showMembersCheckBox, 0, 4, 2, 1);
    }

    private void setupMemberFilterControls(GridPane grid) {
        CheckBox showMemberCheckBox = new CheckBox("Pokaż przynależność do zespołów");
        showMemberCheckBox.setSelected(true);
        showMemberCheckBox.setOnAction(e -> showMembers = showMemberCheckBox.isSelected());
        grid.add(showMemberCheckBox, 0, 0, 2, 1);
    }
    
    private void setupTaskStatsControls(GridPane grid) {
        CheckBox showTasksCheckBox = new CheckBox("Pokaż zadania projektów");
        showTasksCheckBox.setSelected(true);
        showTasksCheckBox.setOnAction(e -> showTasks = showTasksCheckBox.isSelected());
        grid.add(showTasksCheckBox, 0, 3, 2, 1);

        CheckBox showStatsCheckBox = new CheckBox("Pokaż statystyki projektów");
        showStatsCheckBox.setSelected(true);
        showStatsCheckBox.setOnAction(e -> showStatistics = showStatsCheckBox.isSelected());
        grid.add(showStatsCheckBox, 0, 4, 2, 1);
    }
}