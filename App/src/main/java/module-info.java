module pl.rozowi.app {
    requires javafx.controls;
    requires javafx.fxml;

    opens pl.rozowi.app.controllers to javafx.fxml;
    exports pl.rozowi.app;
}
