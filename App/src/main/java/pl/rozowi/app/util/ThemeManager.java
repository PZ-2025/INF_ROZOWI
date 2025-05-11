package pl.rozowi.app.util;

import javafx.scene.Scene;
import pl.rozowi.app.models.User;

/**
 * Klasa zarządzająca motywami aplikacji.
 * Umożliwia aplikację i zmianę motywu dla aktualnej sceny.
 */
public class ThemeManager {

    private static final String LIGHT_THEME_PATH = "/css/lightTheme.css";
    private static final String DARK_THEME_PATH = "/css/darkTheme.css";

    private static final String DEFAULT_THEME = "Light";

    /**
     * Aplikuje motyw do podanej sceny na podstawie ustawień użytkownika
     *
     * @param scene Scena, dla której ma zostać zastosowany motyw
     * @param user Użytkownik, którego ustawienia motywu mają być użyte
     */
    public static void applyTheme(Scene scene, User user) {
        if (scene == null) {
            return;
        }

        scene.getStylesheets().clear();

        String themeName = (user != null && user.getTheme() != null) ? user.getTheme() : DEFAULT_THEME;

        if ("Dark".equalsIgnoreCase(themeName)) {
            scene.getStylesheets().add(ThemeManager.class.getResource(DARK_THEME_PATH).toExternalForm());
        } else {
            scene.getStylesheets().add(ThemeManager.class.getResource(LIGHT_THEME_PATH).toExternalForm());
        }
    }

    /**
     * Zmienia motyw dla konkretnej sceny
     *
     * @param scene Scena, dla której ma zostać zmieniony motyw
     * @param themeName Nazwa motywu ("Light" lub "Dark")
     */
    public static void changeTheme(Scene scene, String themeName) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();

        if ("Dark".equalsIgnoreCase(themeName)) {
            scene.getStylesheets().add(ThemeManager.class.getResource(DARK_THEME_PATH).toExternalForm());
        } else {
            scene.getStylesheets().add(ThemeManager.class.getResource(LIGHT_THEME_PATH).toExternalForm());
        }
    }
}