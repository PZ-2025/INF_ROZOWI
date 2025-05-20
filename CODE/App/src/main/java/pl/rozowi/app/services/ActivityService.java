package pl.rozowi.app.services;

import pl.rozowi.app.dao.TaskActivityDAO;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.util.Session;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Klasa odpowiedzialna za logowanie aktywności w systemie.
 * Zapewnia metody do rejestrowania różnych typów zdarzeń związanych z zadaniami,
 * użytkownikami, zespołami oraz innymi działaniami systemowymi.
 */
public class ActivityService {

    private static final TaskActivityDAO activityDAO = new TaskActivityDAO();

    /**
     * Loguje utworzenie nowego zadania.
     *
     * @param taskId ID tworzonego zadania
     * @param taskTitle tytuł zadania
     * @param assignedUserId ID użytkownika, do którego zadanie jest przypisane
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logTaskCreation(int taskId, String taskTitle, int assignedUserId) {
        TaskActivity activity = createActivity(taskId, "CREATE");
        activity.setDescription("Utworzono zadanie \"" + taskTitle + "\" i przypisano do użytkownika ID: " + assignedUserId);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zmianę statusu zadania.
     *
     * @param taskId ID zadania
     * @param taskTitle tytuł zadania
     * @param oldStatus poprzedni status
     * @param newStatus nowy status
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logStatusChange(int taskId, String taskTitle, String oldStatus, String newStatus) {
        TaskActivity activity = createActivity(taskId, "STATUS");
        activity.setDescription("Zmieniono status zadania \"" + taskTitle + "\" z \"" + oldStatus + "\" na \"" + newStatus + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zmianę przypisania zadania do użytkownika.
     *
     * @param taskId ID zadania
     * @param taskTitle tytuł zadania
     * @param oldUserId ID poprzedniego użytkownika (0 jeśli brak)
     * @param newUserId ID nowego użytkownika
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logAssignment(int taskId, String taskTitle, int oldUserId, int newUserId) {
        TaskActivity activity = createActivity(taskId, "ASSIGN");
        String desc = oldUserId > 0
                ? "Zmieniono przypisanie zadania \"" + taskTitle + "\" z użytkownika ID: " + oldUserId + " na użytkownika ID: " + newUserId
                : "Przypisano zadanie \"" + taskTitle + "\" do użytkownika ID: " + newUserId;
        activity.setDescription(desc);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje aktualizację pola w zadaniu.
     *
     * @param taskId ID zadania
     * @param taskTitle tytuł zadania
     * @param fieldName nazwa aktualizowanego pola
     * @param oldValue poprzednia wartość
     * @param newValue nowa wartość
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logTaskUpdate(int taskId, String taskTitle, String fieldName, String oldValue, String newValue) {
        TaskActivity activity = createActivity(taskId, "UPDATE");
        activity.setDescription("Zaktualizowano pole \"" + fieldName + "\" zadania \"" + taskTitle + "\" z \"" + oldValue + "\" na \"" + newValue + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje dodanie komentarza do zadania.
     *
     * @param taskId ID zadania
     * @param taskTitle tytuł zadania
     * @param comment treść komentarza
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logTaskComment(int taskId, String taskTitle, String comment) {
        TaskActivity activity = createActivity(taskId, "COMMENT");
        activity.setDescription("Dodano komentarz do zadania \"" + taskTitle + "\": " + comment);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje niestandardową aktywność związaną z zadaniem.
     *
     * @param taskId ID zadania
     * @param activityType typ aktywności
     * @param description opis aktywności
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logCustomActivity(int taskId, String activityType, String description) {
        TaskActivity activity = createActivity(taskId, activityType);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Tworzy obiekt aktywności z podstawowymi danymi.
     *
     * @param taskId ID zadania
     * @param activityType typ aktywności
     * @return obiekt TaskActivity z uzupełnionymi podstawowymi danymi
     */
    private static TaskActivity createActivity(int taskId, String activityType) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setUserId(Session.currentUserId);
        activity.setActivityType(activityType);
        activity.setActivityDate(Timestamp.from(Instant.now()));
        return activity;
    }

    /**
     * Loguje zmianę hasła użytkownika.
     *
     * @param userId ID użytkownika
     * @param changedByAdmin true jeśli hasło zostało zmienione przez administratora
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logPasswordChange(int userId, boolean changedByAdmin) {
        TaskActivity activity = createActivity(0, "PASSWORD");
        String description;

        if (changedByAdmin) {
            description = "Administrator zresetował hasło dla użytkownika ID: " + userId;
        } else {
            description = "Użytkownik ID: " + userId + " zmienił swoje hasło";
        }

        activity.setUserId(userId);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje niestandardową aktywność systemową.
     *
     * @param userId ID użytkownika wykonującego akcję
     * @param activityType typ aktywności
     * @param description opis aktywności
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logSystemActivity(int userId, String activityType, String description) {
        TaskActivity activity = createActivity(0, activityType);
        activity.setUserId(userId);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zalogowanie użytkownika do systemu.
     *
     * @param userId ID użytkownika
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logLogin(int userId) {
        TaskActivity activity = createActivity(0, "LOGIN");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " zalogował się do systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje wylogowanie użytkownika z systemu.
     *
     * @param userId ID użytkownika
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logLogout(int userId) {
        TaskActivity activity = createActivity(0, "LOGOUT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wylogował się z systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje akcję zarządzania użytkownikiem wykonaną przez administratora.
     *
     * @param adminId ID administratora wykonującego akcję
     * @param targetUserId ID użytkownika, na którym wykonano akcję
     * @param action typ akcji (np. "create", "update", "delete")
     * @param details szczegóły akcji
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logUserManagement(int adminId, int targetUserId, String action, String details) {
        TaskActivity activity = createActivity(0, "USER_MANAGEMENT");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " wykonał akcję '" + action +
                "' na użytkowniku ID: " + targetUserId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje akcję zarządzania zespołem.
     *
     * @param userId ID użytkownika wykonującego akcję
     * @param teamId ID zespołu
     * @param action typ akcji (np. "create", "update", "delete")
     * @param details szczegóły akcji
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logTeamManagement(int userId, int teamId, String action, String details) {
        TaskActivity activity = createActivity(0, "TEAM_MANAGEMENT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wykonał akcję '" + action +
                "' na zespole ID: " + teamId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje generowanie raportu.
     *
     * @param userId ID użytkownika generującego raport
     * @param reportType typ raportu
     * @param details szczegóły raportu
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logReportGeneration(int userId, String reportType, String details) {
        TaskActivity activity = createActivity(0, "REPORT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wygenerował raport typu '" +
                reportType + "'. " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zmianę konfiguracji systemu.
     *
     * @param adminId ID administratora wykonującego zmianę
     * @param setting nazwa ustawienia
     * @param oldValue poprzednia wartość
     * @param newValue nowa wartość
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logConfigChange(int adminId, String setting, String oldValue, String newValue) {
        TaskActivity activity = createActivity(0, "CONFIG");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " zmienił ustawienie '" +
                setting + "' z '" + oldValue + "' na '" + newValue + "'");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje błąd systemowy.
     *
     * @param userId ID użytkownika związany z błędem (0 jeśli systemowy)
     * @param errorType typ błędu
     * @param message komunikat błędu
     * @param stackTrace stack trace błędu (może być null)
     * @return true jeśli operacja logowania zakończyła się sukcesem, false w przeciwnym wypadku
     */
    public static boolean logSystemError(int userId, String errorType, String message, String stackTrace) {
        TaskActivity activity = createActivity(0, "ERROR");
        activity.setUserId(userId);
        activity.setDescription("Błąd typu '" + errorType + "': " + message +
                (stackTrace != null && !stackTrace.isEmpty() ? "\nStack trace: " + stackTrace : ""));
        return activityDAO.insertTaskActivity(activity);
    }
}