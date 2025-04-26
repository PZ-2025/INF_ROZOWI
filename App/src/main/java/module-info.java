module pl.rozowi.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

<<<<<<< HEAD
    // Tylko jeden moduł dla iText 5.x
    requires itextpdf;

    // Pozostałe wymagania modułu
    opens pl.rozowi.app.controllers to javafx.fxml;
    exports pl.rozowi.app;
    opens pl.rozowi.app.util to javafx.fxml;
}
=======
    opens pl.rozowi.app.controllers to javafx.fxml;
    exports pl.rozowi.app;
    opens pl.rozowi.app.util to javafx.fxml;
}
>>>>>>> 88cd853 (Zaktualizowana struktura projektu)
