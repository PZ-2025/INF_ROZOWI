package pl.rozowi.app.services;

/**
 * Klasa reprezentująca wynik operacji rejestracji użytkownika.
 * Przechowuje informację o powodzeniu operacji oraz ewentualny komunikat.
 */
public class RegistrationResult {

    private final boolean success;
    private final String message;

    /**
     * Prywatny konstruktor tworzący obiekt wyniku rejestracji.
     *
     * @param success true jeśli rejestracja zakończyła się sukcesem, false w przeciwnym wypadku
     * @param message komunikat związany z wynikiem operacji
     */
    private RegistrationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Tworzy obiekt RegistrationResult dla udanej rejestracji.
     *
     * @param msg komunikat sukcesu
     * @return obiekt RegistrationResult z ustawionym flagą sukcesu
     */
    public static RegistrationResult success(String msg) {
        return new RegistrationResult(true, msg);
    }

    /**
     * Tworzy obiekt RegistrationResult dla nieudanej rejestracji.
     *
     * @param msg komunikat błędu
     * @return obiekt RegistrationResult z ustawioną flagą niepowodzenia
     */
    public static RegistrationResult fail(String msg) {
        return new RegistrationResult(false, msg);
    }

    /**
     * Sprawdza czy rejestracja zakończyła się sukcesem.
     *
     * @return true jeśli rejestracja była udana, false w przeciwnym wypadku
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Zwraca komunikat związany z wynikiem rejestracji.
     *
     * @return komunikat sukcesu lub błędu
     */
    public String getMessage() {
        return message;
    }
}