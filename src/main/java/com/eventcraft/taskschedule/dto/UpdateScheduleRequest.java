package com.eventcraft.taskschedule.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Payload for updating an existing Schedule item.
 */
public class UpdateScheduleRequest {

    @Size(max = 150, message = "Activity name must not exceed 150 characters")
    private String activityName;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String activityType;

    @Size(max = 100, message = "Location/Stage must not exceed 100 characters")
    private String locationOrStage;

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getLocationOrStage() { return locationOrStage; }
    public void setLocationOrStage(String locationOrStage) { this.locationOrStage = locationOrStage; }
}
