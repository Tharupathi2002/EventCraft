package com.eventcraft.eventplanning.controller;

import com.eventcraft.eventplanning.dto.EventRequestDTO;
import com.eventcraft.eventplanning.dto.EventResponseDTO;
import com.eventcraft.eventplanning.entity.EventType;
import com.eventcraft.eventplanning.service.EventService;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * REST API for Member 1's function: Event Planning & Customization.
 * Base path: /api/events
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // CREATE
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO requestDTO) {
        EventResponseDTO created = eventService.createEvent(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ (all, or filtered by host)
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestParam(required = false) String hostUsername) {
        if (hostUsername != null && !hostUsername.isBlank()) {
            return ResponseEntity.ok(eventService.getEventsByHost(hostUsername));
        }
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // READ (single event)
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    // UPDATE
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventRequestDTO requestDTO) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, requestDTO));
    }

    // Publish a draft event
    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventResponseDTO> publishEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.publishEvent(eventId));
    }

    // Cancel a published/draft event
    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponseDTO> cancelEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.cancelEvent(eventId));
    }

    // DELETE
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    // Supplies the dropdown options for the "Select event type" step
    @GetMapping("/templates")
    public ResponseEntity<EventType[]> getEventTemplates() {
        return ResponseEntity.ok(EventType.values());
    }
}
