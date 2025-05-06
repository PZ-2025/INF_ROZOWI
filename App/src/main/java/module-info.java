module pl.rozowi.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    requires transitive itextpdf;
    requires java.desktop;
    requires task.app.reports;

    // Pozostałe wymagania modułu
    opens pl.rozowi.app.controllers to javafx.fxml;
    exports pl.rozowi.app;
    opens pl.rozowi.app.util to javafx.fxml;
}