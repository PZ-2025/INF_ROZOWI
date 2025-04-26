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
}