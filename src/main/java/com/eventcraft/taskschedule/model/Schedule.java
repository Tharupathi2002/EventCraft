package com.eventcraft.taskschedule.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Schedule Model Entity.
 * Represents an agenda activity, milestone, or timeline slot in the Schedules table.
 */
public class Schedule {

    private Integer scheduleId;
    private Integer eventId;
    private String activityName;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String activityType;
    private String locationOrStage;
    private LocalDateTime createdAt;

    // Joined/Calculated presentation fields
    private String eventTitle;
    private Long durationMinutes;

    public Schedule() {
    }

    public Schedule(Integer scheduleId, Integer eventId, String activityName, String description,
                    LocalDateTime startTime, LocalDateTime endTime, String activityType,
                    String locationOrStage, LocalDateTime createdAt) {
        this.scheduleId = scheduleId;
        this.eventId = eventId;
        this.activityName = activityName;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityType = activityType;
        this.locationOrStage = locationOrStage;
        this.createdAt = createdAt;
        if (startTime != null && endTime != null) {
            this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
        }
    }

    // Getters and Setters
    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { 
        this.startTime = startTime; 
        calculateDuration();
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { 
        this.endTime = endTime; 
        calculateDuration();
    }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getLocationOrStage() { return locationOrStage; }
    public void setLocationOrStage(String locationOrStage) { this.locationOrStage = locationOrStage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    private void calculateDuration() {
        if (this.startTime != null && this.endTime != null) {
            this.durationMinutes = Duration.between(this.startTime, this.endTime).toMinutes();
        }
    }
}
