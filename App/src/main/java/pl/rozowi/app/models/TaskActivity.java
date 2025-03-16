package pl.rozowi.app.models;

import java.sql.Timestamp;

public class TaskActivity {
    private int id;
    private int taskId;
    private int userId;
    private String activityDescription;
    private Timestamp activityDate;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }
    public Timestamp getActivityDate() { return activityDate; }
    public void setActivityDate(Timestamp activityDate) { this.activityDate = activityDate; }
}
