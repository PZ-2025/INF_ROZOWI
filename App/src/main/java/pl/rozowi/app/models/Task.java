package pl.rozowi.app.models;

import javafx.beans.property.*;

public class Task {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty startDate = new SimpleStringProperty();
    private final StringProperty endDate = new SimpleStringProperty();
    private final StringProperty team = new SimpleStringProperty();
    private final IntegerProperty assignedTo = new SimpleIntegerProperty();

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty statusProperty() { return status; }
    public StringProperty startDateProperty() { return startDate; }
    public StringProperty endDateProperty() { return endDate; }
    public StringProperty teamProperty() { return team; }
    public IntegerProperty assignedToProperty() { return assignedTo; }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public String getStartDate() { return startDate.get(); }
    public void setStartDate(String startDate) { this.startDate.set(startDate); }
    public String getEndDate() { return endDate.get(); }
    public void setEndDate(String endDate) { this.endDate.set(endDate); }
    public String getTeam() { return team.get(); }
    public void setTeam(String team) { this.team.set(team); }
    public int getAssignedTo() { return assignedTo.get(); }
    public void setAssignedTo(int assignedTo) { this.assignedTo.set(assignedTo); }
}
