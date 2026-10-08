package com.eventcraft.eventplanning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for Member 1's function: Event Planning & Customization.
 * Maps to the "events" table.
 *
 * NOTE (DB SYNC - TO DO LATER): This is the JPA/ORM mapping only.
 * The actual SQL Server connection (spring.datasource.* properties
 * in application.properties) has NOT been configured yet on purpose.
 * Once that is added, Spring Boot + Hibernate will create/use the
 * "events" table automatically (or you can generate the schema
 * manually in SSMS and match these column names).
 */
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false, length = 150)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "theme", length = 100)
    private String theme;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    // TODO: Replace this plain string with a proper @ManyToOne relationship
    // to the shared User/Host entity once the login/auth module (built by
    // another team member) is merged into the project.
    @Column(name = "host_username", nullable = false, length = 100)
    private String hostUsername;

    // Seating layout chosen by the host: rows are lettered (A, B, C...) and
    // columns are numbered (1, 2, 3...), so a seat is e.g. "B7".
    // Columns are nullable so Hibernate can add them to an existing table;
    // null (older events) falls back to a 4 x 10 grid.
    @Column(name = "seat_rows")
    private Integer seatRows = 4;

    @Column(name = "seat_columns")
    private Integer seatColumns = 10;

    // Always seatRows * seatColumns (kept in sync automatically).
    @Column(name = "total_seats")
    private Integer totalSeats = 40;

    @Column(name = "seats_available")
    private Integer seatsAvailable = 40;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        normalizeSeating();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PostLoad
    protected void onLoad() {
        normalizeSeating();
    }

    private void normalizeSeating() {
        if (seatRows == null) seatRows = 4;
        if (seatColumns == null) seatColumns = 10;
        totalSeats = seatRows * seatColumns;
        if (seatsAvailable == null) seatsAvailable = totalSeats;
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeSeating();
        this.updatedAt = LocalDateTime.now();
    }
}
