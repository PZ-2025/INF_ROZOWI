package pl.rozowi.app.services;

import pl.rozowi.app.dao.TaskActivityDAO;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.util.Session;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Service class for logging activities in the system
 */
public class ActivityService {

    private static final TaskActivityDAO activityDAO = new TaskActivityDAO();

    /**
     * Log task creation activity
     *
     * @param taskId The ID of the created task
     * @param taskTitle The title of the created task
     * @param assignedUserId The ID of the user assigned to the task
     * @return true if the activity was logged successfully
     */
    public static boolean logTaskCreation(int taskId, String taskTitle, int assignedUserId) {
        TaskActivity activity = createActivity(taskId, "CREATE");
        activity.setDescription("Utworzono zadanie \"" + taskTitle + "\" i przypisano do użytkownika ID: " + assignedUserId);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Log task status change activity
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param oldStatus The previous status
     * @param newStatus The new status
     * @return true if the activity was logged successfully
     */
    public static boolean logStatusChange(int taskId, String taskTitle, String oldStatus, String newStatus) {
        TaskActivity activity = createActivity(taskId, "STATUS");
        activity.setDescription("Zmieniono status zadania \"" + taskTitle + "\" z \"" + oldStatus + "\" na \"" + newStatus + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Log task assignment activity
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param oldUserId The ID of the previously assigned user (0 if none)
     * @param newUserId The ID of the newly assigned user
     * @return true if the activity was logged successfully
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
     * Log task update activity
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param fieldName The name of the updated field
     * @param oldValue The previous value
     * @param newValue The new value
     * @return true if the activity was logged successfully
     */
    public static boolean logTaskUpdate(int taskId, String taskTitle, String fieldName, String oldValue, String newValue) {
        TaskActivity activity = createActivity(taskId, "UPDATE");
        activity.setDescription("Zaktualizowano pole \"" + fieldName + "\" zadania \"" + taskTitle + "\" z \"" + oldValue + "\" na \"" + newValue + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Log task comment activity
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param comment The comment text
     * @return true if the activity was logged successfully
     */
    public static boolean logTaskComment(int taskId, String taskTitle, String comment) {
        TaskActivity activity = createActivity(taskId, "COMMENT");
        activity.setDescription("Dodano komentarz do zadania \"" + taskTitle + "\": " + comment);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Log any custom activity
     *
     * @param taskId The ID of the task
     * @param activityType The type of activity
     * @param description The activity description
     * @return true if the activity was logged successfully
     */
    public static boolean logCustomActivity(int taskId, String activityType, String description) {
        TaskActivity activity = createActivity(taskId, activityType);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Create a basic activity object with common fields set
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
     * Loguje aktywność związaną ze zmianą hasła
     *
     * @param userId ID użytkownika, którego hasło zostało zmienione
     * @param changedByAdmin czy zmiana została wykonana przez administratora
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logPasswordChange(int userId, boolean changedByAdmin) {
        TaskActivity activity = createActivity(0, "PASSWORD");
        String description;

        if (changedByAdmin) {
            description = "Administrator zresetował hasło dla użytkownika ID: " + userId;
        } else {
            description = "Użytkownik ID: " + userId + " zmienił swoje hasło";
        }

        activity.setUserId(userId);  // Ustaw użytkownika, którego dotyczy zmiana
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje ogólne zdarzenie systemowe, które nie jest powiązane z konkretnym zadaniem
     *
     * @param userId ID użytkownika, który wykonał akcję
     * @param activityType Typ aktywności
     * @param description Opis aktywności
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logSystemActivity(int userId, String activityType, String description) {
        TaskActivity activity = createActivity(0, activityType);
        activity.setUserId(userId);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie logowania do systemu
     *
     * @param userId ID użytkownika, który się zalogował
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logLogin(int userId) {
        TaskActivity activity = createActivity(0, "LOGIN");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " zalogował się do systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie wylogowania z systemu
     *
     * @param userId ID użytkownika, który się wylogował
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logLogout(int userId) {
        TaskActivity activity = createActivity(0, "LOGOUT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wylogował się z systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie związane z zarządzaniem użytkownikami
     *
     * @param adminId ID administratora wykonującego akcję
     * @param targetUserId ID użytkownika, którego dotyczy akcja
     * @param action Rodzaj akcji (np. "CREATE", "UPDATE", "DELETE")
     * @param details Dodatkowe szczegóły akcji
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logUserManagement(int adminId, int targetUserId, String action, String details) {
        TaskActivity activity = createActivity(0, "USER_MANAGEMENT");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " wykonał akcję '" + action +
                              "' na użytkowniku ID: " + targetUserId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie związane z zarządzaniem zespołami
     *
     * @param userId ID użytkownika wykonującego akcję
     * @param teamId ID zespołu, którego dotyczy akcja
     * @param action Rodzaj akcji (np. "CREATE", "UPDATE", "DELETE", "ASSIGN")
     * @param details Dodatkowe szczegóły akcji
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logTeamManagement(int userId, int teamId, String action, String details) {
        TaskActivity activity = createActivity(0, "TEAM_MANAGEMENT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wykonał akcję '" + action +
                              "' na zespole ID: " + teamId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie związane z raportami
     *
     * @param userId ID użytkownika generującego raport
     * @param reportType Typ raportu
     * @param details Dodatkowe szczegóły
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logReportGeneration(int userId, String reportType, String details) {
        TaskActivity activity = createActivity(0, "REPORT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wygenerował raport typu '" +
                              reportType + "'. " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie związane z konfiguracją systemu
     *
     * @param adminId ID administratora zmieniającego konfigurację
     * @param setting Nazwa zmienianego ustawienia
     * @param oldValue Stara wartość
     * @param newValue Nowa wartość
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logConfigChange(int adminId, String setting, String oldValue, String newValue) {
        TaskActivity activity = createActivity(0, "CONFIG");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " zmienił ustawienie '" +
                              setting + "' z '" + oldValue + "' na '" + newValue + "'");
        return activityDAO.insertTaskActivity(activity);
    }

    /**
     * Loguje zdarzenie związane z błędem lub wyjątkiem w systemie
     *
     * @param userId ID użytkownika, dla którego wystąpił błąd
     * @param errorType Typ błędu
     * @param message Komunikat błędu
     * @param stackTrace Opcjonalny stack trace
     * @return true jeśli aktywność została zalogowana pomyślnie
     */
    public static boolean logSystemError(int userId, String errorType, String message, String stackTrace) {
        TaskActivity activity = createActivity(0, "ERROR");
        activity.setUserId(userId);
        activity.setDescription("Błąd typu '" + errorType + "': " + message +
                              (stackTrace != null && !stackTrace.isEmpty() ? "\nStack trace: " + stackTrace : ""));
        return activityDAO.insertTaskActivity(activity);
    }
}