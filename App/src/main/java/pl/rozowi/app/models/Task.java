package pl.rozowi.app.models;

import javafx.beans.property.*;

public class Task {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty projectId = new SimpleIntegerProperty();
    private final IntegerProperty assignedTo = new SimpleIntegerProperty();
    private final IntegerProperty teamId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty priority = new SimpleStringProperty(); // LOW, MEDIUM, HIGH
    private final StringProperty startDate = new SimpleStringProperty();
    private final StringProperty endDate = new SimpleStringProperty();
    private final StringProperty attachment = new SimpleStringProperty();
    private final StringProperty comment = new SimpleStringProperty();
    private final StringProperty team = new SimpleStringProperty();

    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    public IntegerProperty assignedToProperty() {
        return assignedTo;
    }

    public IntegerProperty teamIdProperty() {
        return teamId;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public StringProperty startDateProperty() {
        return startDate;
    }

    public StringProperty endDateProperty() {
        return endDate;
    }

    public StringProperty attachmentProperty() {
        return attachment;
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public StringProperty teamProperty() {
        return team;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getProjectId() {
        return projectId.get();
    }

    public void setProjectId(int projectId) {
        this.projectId.set(projectId);
    }

    public int getAssignedTo() {
        return assignedTo.get();
    }

    public void setAssignedTo(int assignedTo) {
        this.assignedTo.set(assignedTo);
    }

    public int getTeamId() {
        return teamId.get();
    }

    public void setTeamId(int teamId) {
        this.teamId.set(teamId);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getPriority() {
        return priority.get();
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public String getStartDate() {
        return startDate.get();
    }

    public void setStartDate(String startDate) {
        this.startDate.set(startDate);
    }

    public String getEndDate() {
        return endDate.get();
    }

    public void setEndDate(String endDate) {
        this.endDate.set(endDate);
    }

    public String getAttachment() {
        return attachment.get();
    }

    public void setAttachment(String attachment) {
        this.attachment.set(attachment);
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public String getTeam() {
        return team.get();
    }

    public void setTeam(String team) {
        this.team.set(team);
    }
}
