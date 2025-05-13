package pl.rozowi.app.services;

import pl.rozowi.app.dao.TaskActivityDAO;
import pl.rozowi.app.models.TaskActivity;
import pl.rozowi.app.util.Session;

import java.sql.Timestamp;
import java.time.Instant;

public class ActivityService {

    private static final TaskActivityDAO activityDAO = new TaskActivityDAO();

    public static boolean logTaskCreation(int taskId, String taskTitle, int assignedUserId) {
        TaskActivity activity = createActivity(taskId, "CREATE");
        activity.setDescription("Utworzono zadanie \"" + taskTitle + "\" i przypisano do użytkownika ID: " + assignedUserId);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logStatusChange(int taskId, String taskTitle, String oldStatus, String newStatus) {
        TaskActivity activity = createActivity(taskId, "STATUS");
        activity.setDescription("Zmieniono status zadania \"" + taskTitle + "\" z \"" + oldStatus + "\" na \"" + newStatus + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logAssignment(int taskId, String taskTitle, int oldUserId, int newUserId) {
        TaskActivity activity = createActivity(taskId, "ASSIGN");
        String desc = oldUserId > 0
                ? "Zmieniono przypisanie zadania \"" + taskTitle + "\" z użytkownika ID: " + oldUserId + " na użytkownika ID: " + newUserId
                : "Przypisano zadanie \"" + taskTitle + "\" do użytkownika ID: " + newUserId;
        activity.setDescription(desc);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logTaskUpdate(int taskId, String taskTitle, String fieldName, String oldValue, String newValue) {
        TaskActivity activity = createActivity(taskId, "UPDATE");
        activity.setDescription("Zaktualizowano pole \"" + fieldName + "\" zadania \"" + taskTitle + "\" z \"" + oldValue + "\" na \"" + newValue + "\"");
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logTaskComment(int taskId, String taskTitle, String comment) {
        TaskActivity activity = createActivity(taskId, "COMMENT");
        activity.setDescription("Dodano komentarz do zadania \"" + taskTitle + "\": " + comment);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logCustomActivity(int taskId, String activityType, String description) {
        TaskActivity activity = createActivity(taskId, activityType);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    private static TaskActivity createActivity(int taskId, String activityType) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setUserId(Session.currentUserId);
        activity.setActivityType(activityType);
        activity.setActivityDate(Timestamp.from(Instant.now()));
        return activity;
    }

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

    public static boolean logSystemActivity(int userId, String activityType, String description) {
        TaskActivity activity = createActivity(0, activityType);
        activity.setUserId(userId);
        activity.setDescription(description);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logLogin(int userId) {
        TaskActivity activity = createActivity(0, "LOGIN");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " zalogował się do systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logLogout(int userId) {
        TaskActivity activity = createActivity(0, "LOGOUT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wylogował się z systemu");
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logUserManagement(int adminId, int targetUserId, String action, String details) {
        TaskActivity activity = createActivity(0, "USER_MANAGEMENT");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " wykonał akcję '" + action +
                "' na użytkowniku ID: " + targetUserId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logTeamManagement(int userId, int teamId, String action, String details) {
        TaskActivity activity = createActivity(0, "TEAM_MANAGEMENT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wykonał akcję '" + action +
                "' na zespole ID: " + teamId + ". " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logReportGeneration(int userId, String reportType, String details) {
        TaskActivity activity = createActivity(0, "REPORT");
        activity.setUserId(userId);
        activity.setDescription("Użytkownik ID: " + userId + " wygenerował raport typu '" +
                reportType + "'. " + details);
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logConfigChange(int adminId, String setting, String oldValue, String newValue) {
        TaskActivity activity = createActivity(0, "CONFIG");
        activity.setUserId(adminId);
        activity.setDescription("Administrator ID: " + adminId + " zmienił ustawienie '" +
                setting + "' z '" + oldValue + "' na '" + newValue + "'");
        return activityDAO.insertTaskActivity(activity);
    }

    public static boolean logSystemError(int userId, String errorType, String message, String stackTrace) {
        TaskActivity activity = createActivity(0, "ERROR");
        activity.setUserId(userId);
        activity.setDescription("Błąd typu '" + errorType + "': " + message +
                (stackTrace != null && !stackTrace.isEmpty() ? "\nStack trace: " + stackTrace : ""));
        return activityDAO.insertTaskActivity(activity);
    }
}