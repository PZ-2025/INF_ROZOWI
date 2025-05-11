package pl.rozowi.app.util;

import javafx.scene.Scene;
import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.models.User;

public class ThemeManager {

    private static final String LIGHT_THEME_PATH = "/css/lightTheme.css";
    private static final String DARK_THEME_PATH = "/css/darkTheme.css";

    private static final String DEFAULT_THEME = "Light";

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

    public static void saveThemeToDatabase(User user, String themeName) {
        if (user == null) {
            return;
        }

        SettingsDAO settingsDAO = new SettingsDAO();
        Settings userSettings = settingsDAO.getSettingsByUserId(user.getId());

        if (userSettings != null) {
            userSettings.setTheme(themeName);
            settingsDAO.updateSettings(userSettings);
        } else {
            Settings newSettings = new Settings();
            newSettings.setUserId(user.getId());
            newSettings.setTheme(themeName);
            settingsDAO.insertSettings(newSettings);
        }

        user.setTheme(themeName);
    }
}