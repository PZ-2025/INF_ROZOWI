package pl.rozowi.app.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class Task {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty projectId = new SimpleIntegerProperty();
    private final IntegerProperty teamId = new SimpleIntegerProperty();
    private final IntegerProperty assignedTo = new SimpleIntegerProperty();

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty priority = new SimpleStringProperty();
    private final StringProperty startDate = new SimpleStringProperty();
    private final StringProperty endDate = new SimpleStringProperty();

    private final StringProperty teamName = new SimpleStringProperty();
    private final StringProperty assignedEmail = new SimpleStringProperty();

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getProjectId() {
        return projectId.get();
    }

    public void setProjectId(int projectId) {
        this.projectId.set(projectId);
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    public int getTeamId() {
        return teamId.get();
    }

    public void setTeamId(int teamId) {
        this.teamId.set(teamId);
    }

    public IntegerProperty teamIdProperty() {
        return teamId;
    }

    public int getAssignedTo() {
        return assignedTo.get();
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo.set(assignedTo);
    }

    public IntegerProperty assignedToProperty() {
        return assignedTo;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getPriority() {
        return priority.get();
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public String getStartDate() {
        return startDate.get();
    }

    public void setStartDate(String startDate) {
        this.startDate.set(startDate);
    }

    public StringProperty startDateProperty() {
        return startDate;
    }

    public String getEndDate() {
        return endDate.get();
    }

    public void setEndDate(String endDate) {
        this.endDate.set(endDate);
    }

    public StringProperty endDateProperty() {
        return endDate;
    }

    public String getTeamName() {
        return teamName.get();
    }

    public void setTeamName(String teamName) {
        this.teamName.set(teamName);
    }

    public StringProperty teamNameProperty() {
        return teamName;
    }

    public String getAssignedEmail() {
        return assignedEmail.get();
    }

    public void setAssignedEmail(String email) {
        this.assignedEmail.set(email);
    }

    public StringProperty assignedEmailProperty() {
        return assignedEmail;
    }
}
