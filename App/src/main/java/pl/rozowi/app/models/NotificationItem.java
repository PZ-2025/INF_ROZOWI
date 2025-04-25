package pl.rozowi.app.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NotificationItem {
    private final StringProperty name;
    private final StringProperty description;
    private final StringProperty date;

    public NotificationItem(String name, String description, String date) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleStringProperty(date);
    }

    public StringProperty nameProperty() {
        return name;
    }
    public StringProperty descriptionProperty() {
        return description;
    }
    public StringProperty dateProperty() {
        return date;
    }

    // Dodane metody getter, aby móc używać ich w filtrze
    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getDate() {
        return date.get();
    }
}
