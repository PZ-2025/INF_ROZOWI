package pl.rozowi.app.controllers;

import pl.rozowi.app.models.User;

/**
 * Interfejs dla kontrolerów, które potrzebują dostępu do obiektu użytkownika
 */
public interface UserAwareController {

    /**
     * Ustawia obiekt użytkownika w kontrolerze
     * @param user Obiekt użytkownika
     */
    void setUser(User user);
}