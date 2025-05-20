package pl.rozowi.app.util;

import javafx.scene.Scene;
import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.models.User;

/**
 * Klasa zarządzająca motywami (skórkami) aplikacji.
 * Umożliwia aplikowanie, zmianę oraz zapis motywów dla użytkowników.
 * Obsługuje dwa podstawowe motywy: Light (domyślny) i Dark.
 */
public class ThemeManager {

    private static final String LIGHT_THEME_PATH = "/css/lightTheme.css";
    private static final String DARK_THEME_PATH = "/css/darkTheme.css";

    private static final String DEFAULT_THEME = "Light";

    /**
     * Stosuje odpowiedni motyw dla podanej sceny na podstawie preferencji użytkownika.
     * Jeśli użytkownik jest null lub nie ma ustawionego motywu, stosuje motyw domyślny.
     * Czyści wszystkie istniejące style przed zastosowaniem nowego motywu.
     *
     * @param scene Scena, do której ma zostać zastosowany motyw
     * @param user Użytkownik, którego preferencje motywu mają zostać użyte (może być null)
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
     * Zmienia motyw dla podanej sceny na określony.
     * Czyści wszystkie istniejące style przed zastosowaniem nowego motywu.
     *
     * @param scene Scena, do której ma zostać zastosowany motyw
     * @param themeName Nazwa motywu do zastosowania ("Dark" lub cokolwiek innego dla motywu jasnego)
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

    /**
     * Zapisuje preferencje motywu użytkownika w bazie danych.
     * Jeśli użytkownik ma już ustawienia, aktualizuje je.
     * Jeśli nie, tworzy nowy rekord z preferencjami.
     * Aktualizuje również obiekt użytkownika przekazany jako parametr.
     *
     * @param user Użytkownik, którego preferencje mają zostać zapisane
     * @param themeName Nazwa motywu do zapisania
     */
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