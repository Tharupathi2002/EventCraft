package com.eventcraft.eventplanning.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.eventplanning.dto.EventRequestDTO;
import com.eventcraft.eventplanning.dto.EventResponseDTO;
import com.eventcraft.eventplanning.dto.VenueOptionDTO;
import com.eventcraft.eventplanning.entity.EventType;
import com.eventcraft.eventplanning.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API for Member 1's function: Event Planning & Customization.
 * Base path: /api/events
 *
 * Only logged-in hosts can reach this (see auth/WebConfig). The host is always
 * taken from the login session, and a host can only see/change their own events.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // CREATE
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO requestDTO,
                                                        HttpSession session) {
        requestDTO.setHostUsername(AuthSession.name(session));
        EventResponseDTO created = eventService.createEvent(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ (the logged-in host's events)
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(HttpSession session) {
        return ResponseEntity.ok(eventService.getEventsByHost(AuthSession.name(session)));
    }

    // READ (single event)
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long eventId, HttpSession session) {
        return ResponseEntity.ok(requireOwner(eventId, session));
    }

    // UPDATE
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventRequestDTO requestDTO,
            HttpSession session) {
        requireOwner(eventId, session);
        requestDTO.setHostUsername(AuthSession.name(session));
        return ResponseEntity.ok(eventService.updateEvent(eventId, requestDTO));
    }

    // Publish a draft event
    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventResponseDTO> publishEvent(@PathVariable Long eventId, HttpSession session) {
        requireOwner(eventId, session);
        return ResponseEntity.ok(eventService.publishEvent(eventId));
    }

    // Cancel a published/draft event
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(@PathVariable Long eventId, HttpSession session) {
        requireOwner(eventId, session);
        return ResponseEntity.ok(eventService.cancelEvent(eventId));
    }

    // DELETE
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, HttpSession session) {
        requireOwner(eventId, session);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    // Supplies the dropdown options for the "Select event type" step
    @GetMapping("/templates")
    public ResponseEntity<EventType[]> getEventTemplates() {
        return ResponseEntity.ok(EventType.values());
    }

    // Venues free on the chosen date, for the venue dropdown on the create form
    @GetMapping("/available-venues")
    public ResponseEntity<List<VenueOptionDTO>> getAvailableVenues(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(eventService.getAvailableVenues(date));
    }

    private EventResponseDTO requireOwner(Long eventId, HttpSession session) {
        EventResponseDTO event = eventService.getEventById(eventId);
        if (!event.getHostUsername().equals(AuthSession.name(session))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own events.");
        }
        return event;
    }
}
