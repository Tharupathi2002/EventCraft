package com.eventcraft.taskschedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * An agenda / timeline item for an event (UC-17). Maps to the "schedules" table.
 */
@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "activity_name", nullable = false, length = 150)
    private String activityName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType = "Activity";

    @Column(name = "location_or_stage", length = 100)
    private String locationOrStage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Filled in by ScheduleService for display; not stored.
    @Transient
    private String eventTitle;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Transient
    public Long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }
}
