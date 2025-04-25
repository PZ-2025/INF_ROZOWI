package pl.rozowi.app.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Team {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty teamName = new SimpleStringProperty();
    private final IntegerProperty projectId = new SimpleIntegerProperty();

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getTeamName() {
        return teamName.get();
    }

    public void setTeamName(String name) {
        this.teamName.set(name);
    }

    public int getProjectId() {
        return projectId.get();
    }

    public void setProjectId(int pid) {
        this.projectId.set(pid);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty teamNameProperty() {
        return teamName;
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    @Override
    public String toString() {
        return getId() + " – " + getTeamName();
    }
}
