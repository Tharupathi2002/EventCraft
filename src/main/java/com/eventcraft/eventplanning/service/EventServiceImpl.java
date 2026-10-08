package com.eventcraft.eventplanning.service;

import com.eventcraft.eventplanning.dto.EventRequestDTO;
import com.eventcraft.eventplanning.dto.EventResponseDTO;
import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.eventplanning.entity.Visibility;
import com.eventcraft.eventplanning.exception.EventNotFoundException;
import com.eventcraft.eventplanning.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO requestDTO) {
        validateRequest(requestDTO);

        Event event = new Event();
        event.setEventName(requestDTO.getEventName().trim());
        event.setEventType(requestDTO.getEventType());
        event.setEventDate(requestDTO.getEventDate());
        event.setTheme(requestDTO.getTheme());
        event.setCoverImageUrl(requestDTO.getCoverImageUrl());
        event.setDescription(requestDTO.getDescription());
        event.setVisibility(requestDTO.getVisibility() != null ? requestDTO.getVisibility() : Visibility.PRIVATE);
        event.setHostUsername(requestDTO.getHostUsername().trim());
        event.setStatus(EventStatus.DRAFT);

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getEventsByHost(String hostUsername) {
        return eventRepository.findByHostUsername(hostUsername)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDTO getEventById(Long eventId) {
        return mapToResponse(findEventOrThrow(eventId));
    }

    @Override
    public EventResponseDTO updateEvent(Long eventId, EventRequestDTO requestDTO) {
        validateRequest(requestDTO);
        Event event = findEventOrThrow(eventId);

        event.setEventName(requestDTO.getEventName().trim());
        event.setEventType(requestDTO.getEventType());
        event.setEventDate(requestDTO.getEventDate());
        event.setTheme(requestDTO.getTheme());
        event.setCoverImageUrl(requestDTO.getCoverImageUrl());
        event.setDescription(requestDTO.getDescription());
        if (requestDTO.getVisibility() != null) {
            event.setVisibility(requestDTO.getVisibility());
        }

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    @Override
    public EventResponseDTO publishEvent(Long eventId) {
        Event event = findEventOrThrow(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        return mapToResponse(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO cancelEvent(Long eventId) {
        Event event = findEventOrThrow(eventId);
        event.setStatus(EventStatus.CANCELLED);
        return mapToResponse(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long eventId) {
        Event event = findEventOrThrow(eventId);
        eventRepository.delete(event);
    }

    // ---------- helpers ----------

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private void validateRequest(EventRequestDTO requestDTO) {
        if (requestDTO.getEventName() == null || requestDTO.getEventName().isBlank()) {
            throw new IllegalArgumentException("Event name is required.");
        }
        if (requestDTO.getEventType() == null) {
            throw new IllegalArgumentException("Event type is required.");
        }
        if (requestDTO.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required.");
        }
        if (requestDTO.getHostUsername() == null || requestDTO.getHostUsername().isBlank()) {
            throw new IllegalArgumentException("Host username is required.");
        }
    }

    private EventResponseDTO mapToResponse(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .eventType(event.getEventType())
                .eventDate(event.getEventDate())
                .theme(event.getTheme())
                .coverImageUrl(event.getCoverImageUrl())
                .description(event.getDescription())
                .visibility(event.getVisibility())
                .status(event.getStatus())
                .hostUsername(event.getHostUsername())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
