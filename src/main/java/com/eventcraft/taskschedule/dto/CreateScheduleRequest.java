package com.eventcraft.taskschedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Payload for creating a new Schedule activity / timeline item.
 */
public class CreateScheduleRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Activity name is required")
    @Size(max = 150, message = "Activity name must not exceed 150 characters")
    private String activityName;

    private String description;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String activityType = "Activity";

    @Size(max = 100, message = "Location/Stage must not exceed 100 characters")
    private String locationOrStage;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

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
