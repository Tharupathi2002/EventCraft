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

    // TODO: once the auth module is merged, stop sending this from the
    // frontend and instead take the logged-in host from the session/JWT.
    private String hostUsername;
}
