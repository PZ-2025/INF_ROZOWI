package pl.rozowi.app.services;

public class RegistrationResult {

    private final boolean success;
    private final String message;

    private RegistrationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static RegistrationResult success(String msg) {
        return new RegistrationResult(true, msg);
    }

    public static RegistrationResult fail(String msg) {
        return new RegistrationResult(false, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}