package com.eventcraft.eventplanning.dto;

import com.eventcraft.eventplanning.entity.EventType;
import com.eventcraft.eventplanning.entity.Visibility;
import lombok.Data;

import java.time.LocalDate;

/**
 * Shape of the JSON body the frontend sends when creating or updating an event.
 */
@Data
public class EventRequestDTO {

    private String eventName;
    private EventType eventType;
    private LocalDate eventDate;
    private String theme;
    private String coverImageUrl;
    private String description;
    private Visibility visibility;

    // Seating layout: number of lettered rows and numbered columns.
    private Integer seatRows;
    private Integer seatColumns;

    // The venue chosen for the event (required when creating). Booked and charged to
    // the event's budget automatically.
    private Long venueId;

    // TODO: once the auth module is merged, stop sending this from the
    // frontend and instead take the logged-in host from the session/JWT.
    private String hostUsername;
}
