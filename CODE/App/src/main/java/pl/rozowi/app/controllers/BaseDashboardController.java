package pl.rozowi.app.controllers;

import javafx.scene.Scene;
import pl.rozowi.app.models.User;
import pl.rozowi.app.util.ThemeManager;

/**
 * Bazowy kontroler dla wszystkich dashboardów.
 * Zawiera wspólne funkcje, jak zarządzanie użytkownikiem i motywem.
 */
public abstract class BaseDashboardController {

    protected User currentUser;

    /**
     * Ustawia użytkownika dla kontrolera i stosuje jego motyw
     * @param user Obiekt użytkownika
     */
    public void setUser(User user) {
        this.currentUser = user;

        applyUserTheme();

        onUserSet(user);
    }

    /**
     * Metoda wywoływana po ustawieniu użytkownika
     * @param user Obiekt użytkownika
     */
    protected abstract void onUserSet(User user);

    /**
     * Stosuje motyw użytkownika do aktualnej sceny
     */
    protected void applyUserTheme() {
        if (currentUser != null && getScene() != null) {
            ThemeManager.applyTheme(getScene(), currentUser);
        }
    }

    /**
     * Zwraca aktualną scenę dla kontrolera
     * @return Obiekt sceny lub null jeśli scena nie jest jeszcze dostępna
     */
    protected abstract Scene getScene();
}