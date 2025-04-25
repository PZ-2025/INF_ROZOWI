module pl.rozowi.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens pl.rozowi.app.controllers to javafx.fxml;
    exports pl.rozowi.app;
    opens pl.rozowi.app.util to javafx.fxml;
}
