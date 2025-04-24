package pl.rozowi.app.models;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Project {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty projectName = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final IntegerProperty managerId = new SimpleIntegerProperty();

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getProjectName() {
        return projectName.get();
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }

    public int getManagerId() {
        return managerId.get();
    }

    public void setManagerId(int managerId) {
        this.managerId.set(managerId);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public IntegerProperty managerIdProperty() {
        return managerId;
    }

    public String getName() {
        return getProjectName();
    }

    public void setName(String name) {
        setProjectName(name);
    }

    public StringProperty nameProperty() {
        return projectName;
    }

    @Override
    public String toString() {
        return getId() + " - " + getProjectName();
    }
}
