package com.eventcraft.taskschedule.model;

/**
 * TaskMetrics DTO/Model.
 * Provides task completion statistics for Member 6's Event Dashboard (UC-06).
 */
public class TaskMetrics {

    private Integer eventId;
    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int pendingTasks;
    private double completionPercentage;

    public TaskMetrics() {
    }

    public TaskMetrics(Integer eventId, int totalTasks, int completedTasks,
                       int inProgressTasks, int pendingTasks, double completionPercentage) {
        this.eventId = eventId;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.inProgressTasks = inProgressTasks;
        this.pendingTasks = pendingTasks;
        this.completionPercentage = completionPercentage;
    }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
