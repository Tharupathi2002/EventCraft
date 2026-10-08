package com.eventcraft.eventplanning.dto;

import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.eventplanning.entity.EventType;
import com.eventcraft.eventplanning.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shape of the JSON the backend sends back to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {

    private Long eventId;
    private String eventName;
    private EventType eventType;
    private LocalDate eventDate;
    private String theme;
    private String coverImageUrl;
    private String description;
    private Visibility visibility;
    private EventStatus status;
    private Integer seatRows;
    private Integer seatColumns;
    private Integer totalSeats;
    private Integer seatsAvailable;
    private String hostUsername;
    private Long venueId;
    private String venueName;
    // Optional note for the host after creating an event (e.g. venue smaller than the seat map).
    private String warning;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
