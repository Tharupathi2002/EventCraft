package com.eventcraft.eventplanning.service;

import com.eventcraft.eventplanning.dto.EventRequestDTO;
import com.eventcraft.eventplanning.dto.EventResponseDTO;
import com.eventcraft.eventplanning.dto.VenueOptionDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic contract for the Event Planning & Customization module.
 */
public interface EventService {

    EventResponseDTO createEvent(EventRequestDTO requestDTO);

    List<VenueOptionDTO> getAvailableVenues(LocalDate date);

    List<EventResponseDTO> getAllEvents();

    List<EventResponseDTO> getEventsByHost(String hostUsername);

    EventResponseDTO getEventById(Long eventId);

    EventResponseDTO updateEvent(Long eventId, EventRequestDTO requestDTO);

    EventResponseDTO publishEvent(Long eventId);

    EventResponseDTO cancelEvent(Long eventId);

    void deleteEvent(Long eventId);
}
