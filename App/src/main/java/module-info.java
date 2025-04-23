module pl.rozowi.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires java.desktop;

    opens pl.rozowi.app.controllers to javafx.fxml;
    opens pl.rozowi.app.models to javafx.base, javafx.fxml;
    opens pl.rozowi.app.util to javafx.fxml;

    exports pl.rozowi.app;
}