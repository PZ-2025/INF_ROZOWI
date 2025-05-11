package pl.rozowi.app.util;

import pl.rozowi.app.dao.SettingsDAO;
import pl.rozowi.app.models.Settings;
import pl.rozowi.app.models.User;

public class DefaultViewManager {

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