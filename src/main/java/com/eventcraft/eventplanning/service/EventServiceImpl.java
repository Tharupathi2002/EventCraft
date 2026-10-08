package com.eventcraft.eventplanning.service;

import com.eventcraft.budgetexpense.dto.BudgetForm;
import com.eventcraft.budgetexpense.service.BudgetService;
import com.eventcraft.eventplanning.dto.EventRequestDTO;
import com.eventcraft.eventplanning.dto.EventResponseDTO;
import com.eventcraft.eventplanning.dto.VenueOptionDTO;
import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.eventplanning.entity.Visibility;
import com.eventcraft.eventplanning.exception.EventNotFoundException;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.guestinvitation.entity.Invitation;
import com.eventcraft.guestinvitation.repository.InvitationRepository;
import com.eventcraft.taskschedule.service.ScheduleService;
import com.eventcraft.taskschedule.service.TaskService;
import com.eventcraft.vendorvenue.entity.Venue;
import com.eventcraft.vendorvenue.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final InvitationRepository invitationRepository;
    private final BudgetService budgetService;
    private final BookingService bookingService;
    private final TaskService taskService;
    private final ScheduleService scheduleService;

    /**
     * Creates the event and, in the same transaction: its budget, the venue booking, and the
     * venue's cost as an expense in that budget. If any step fails nothing is saved.
     * The budget starts at the venue's rate; the host can raise it from the budget page.
     */
    @Override
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO) {
        validateRequest(requestDTO);
        if (requestDTO.getVenueId() == null) {
            throw new IllegalArgumentException("Please select a venue for the event.");
        }
        // Checked before anything is saved so a bad venue gives a clear message.
        Venue venue = bookingService.requireAvailableVenue(requestDTO.getVenueId(), requestDTO.getEventDate());

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
        event.setSeatRows(requestDTO.getSeatRows());
        event.setSeatColumns(requestDTO.getSeatColumns());
        event.setTotalSeats(requestDTO.getSeatRows() * requestDTO.getSeatColumns());
        event.setSeatsAvailable(event.getTotalSeats());

        Event saved = eventRepository.save(event);
        String host = saved.getHostUsername();

        // 1. Budget for the new event.
        BudgetForm budgetForm = new BudgetForm();
        budgetForm.setEventId(saved.getEventId());
        budgetForm.setTotalBudget(venue.getRatePerDay());
        budgetService.create(budgetForm, host);

        // 2. Book + confirm the venue, which adds its cost to the budget.
        BookingService.Outcome outcome = bookingService.bookVenueForEvent(saved.getEventId(), venue.getVenueId(), host);

        EventResponseDTO response = mapToResponse(saved);
        response.setWarning(outcome.warning());
        return response;
    }

    @Override
    public List<VenueOptionDTO> getAvailableVenues(LocalDate date) {
        if (date != null && date.isBefore(LocalDate.now())) {
            return java.util.Collections.emptyList();
        }
        return bookingService.findAvailableVenues(date).stream()
                .map(v -> new VenueOptionDTO(v.getVenueId(), v.getName(), v.getCity(), v.getCapacity(), v.getRatePerDay()))
                .collect(Collectors.toList());
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

        boolean layoutChanged = !requestDTO.getSeatRows().equals(event.getSeatRows())
                || !requestDTO.getSeatColumns().equals(event.getSeatColumns());
        if (layoutChanged) {
            if (invitationRepository.existsByEvent_EventIdAndRsvpStatus(eventId, "ACCEPTED")) {
                throw new IllegalArgumentException(
                        "The seating layout can't be changed after guests have already chosen seats.");
            }
            event.setSeatRows(requestDTO.getSeatRows());
            event.setSeatColumns(requestDTO.getSeatColumns());
            event.setTotalSeats(requestDTO.getSeatRows() * requestDTO.getSeatColumns());
            event.setSeatsAvailable(event.getTotalSeats());
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
        // Remove this event's invitations first so the foreign key doesn't block the delete.
        List<Invitation> invitations = invitationRepository.findByEvent_EventId(eventId);
        invitationRepository.deleteAll(invitations);
        // The event's budget (and its expenses) must go too, or its foreign key blocks the delete.
        budgetService.deleteByEventId(eventId);
        // Vendor/venue bookings reference the event too.
        bookingService.deleteByEventId(eventId);
        // Tasks and schedule items belong to the event as well.
        taskService.deleteByEventId(eventId);
        scheduleService.deleteByEventId(eventId);
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
        if (requestDTO.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date must be today or an upcoming date.");
        }
        if (requestDTO.getHostUsername() == null || requestDTO.getHostUsername().isBlank()) {
            throw new IllegalArgumentException("Host username is required.");
        }
        Integer rows = requestDTO.getSeatRows();
        Integer cols = requestDTO.getSeatColumns();
        if (rows == null || rows < 1 || rows > 26) {
            throw new IllegalArgumentException("Number of seat rows is required (1 to 26).");
        }
        if (cols == null || cols < 1 || cols > 30) {
            throw new IllegalArgumentException("Number of seat columns is required (1 to 30).");
        }
    }

    private EventResponseDTO mapToResponse(Event event) {
        Venue venue = event.getEventId() == null ? null
                : bookingService.findActiveVenueForEvent(event.getEventId()).orElse(null);
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
                .seatRows(event.getSeatRows())
                .seatColumns(event.getSeatColumns())
                .totalSeats(event.getTotalSeats())
                .seatsAvailable(event.getSeatsAvailable())
                .hostUsername(event.getHostUsername())
                .venueId(venue != null ? venue.getVenueId() : null)
                .venueName(venue != null ? venue.getName() : null)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
