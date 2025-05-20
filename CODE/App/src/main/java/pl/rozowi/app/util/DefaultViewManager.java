package pl.rozowi.app.util;

import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.models.User;

/**
 * Klasa zarządzająca domyślnym widokiem użytkownika.
 * Pozwala na zapisywanie preferencji dotyczących domyślnego widoku dla danego użytkownika.
 */
public class DefaultViewManager {

    /**
     * Zapisuje domyślny widok dla określonego użytkownika.
     * Jeśli użytkownik istnieje w systemie, aktualizuje jego ustawienia.
     * Jeśli użytkownik nie ma jeszcze ustawień, tworzy nowy rekord z preferencjami.
     * Metoda również aktualizuje obiekt użytkownika przekazany jako parametr.
     *
     * @param user obiekt użytkownika, dla którego zapisywane są preferencje
     * @param defaultView nazwa domyślnego widoku do zapisania
     * @throws IllegalArgumentException jeśli parametr defaultView jest null
     */
    public static void saveDefaultView(User user, String defaultView) {
        if (user == null) {
            return;
        }

        SettingsDAO settingsDAO = new SettingsDAO();
        Settings userSettings = settingsDAO.getSettingsByUserId(user.getId());

        if (userSettings != null) {
            userSettings.setDefaultView(defaultView);
            settingsDAO.updateSettings(userSettings);
        } else {
            Settings newSettings = new Settings();
            newSettings.setUserId(user.getId());
            newSettings.setDefaultView(defaultView);
            settingsDAO.insertSettings(newSettings);
        }

        user.setDefaultView(defaultView);
    }
}