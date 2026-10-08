package com.eventcraft.taskschedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A task for an event (UC-17). Maps to the "tasks" table.
 * eventId / assignedTo / createdBy are plain ids pointing at events.event_id and users.id.
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "Medium";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "Pending";

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Filled in by TaskService for display; not stored.
    @Transient
    private String assigneeName;
    @Transient
    private String creatorName;
    @Transient
    private String eventTitle;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
