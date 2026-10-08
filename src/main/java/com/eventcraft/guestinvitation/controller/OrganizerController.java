package com.eventcraft.guestinvitation.controller;

import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.eventplanning.entity.EventStatus;
import com.eventcraft.guestinvitation.entity.Invitation;
import com.eventcraft.guestinvitation.entity.User;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.guestinvitation.repository.InvitationRepository;
import com.eventcraft.guestinvitation.repository.UserRepository;
import com.eventcraft.guestinvitation.service.GuestManagementService;
import com.eventcraft.auth.AuthSession;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrganizerController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final GuestManagementService guestService;

    @GetMapping("/organizer/events")
    public String viewEvents(Model model, HttpSession session) {
        String hostName = AuthSession.name(session);
        // Only the logged-in host's own PUBLISHED events (drafts/cancelled stay in the event planner)
        model.addAttribute("events",
                eventRepository.findByHostUsernameAndStatus(hostName, EventStatus.PUBLISHED));
        model.addAttribute("hostName", hostName);
        return "guestinvitation/organizer-events";
    }

    @GetMapping("/organizer/event/{id}")
    public String eventDashboard(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Event> owned = ownEvent(id, session).filter(this::isPublished);
        if (owned.isEmpty()) {
            return "redirect:/organizer/events";
        }
        Event event = owned.get();

        // Fetch all invitations
        List<Invitation> invitations = invitationRepository.findByEvent_EventId(id);

        List<String> takenSeats = guestService.takenSeats(id);

        model.addAttribute("event", event);
        model.addAttribute("invitations", invitations);
        model.addAttribute("availableUsers", userRepository.findByRole("GUEST"));

        // Pass the seat grid data to the organizer view
        model.addAttribute("takenSeats", takenSeats);
        model.addAttribute("rows", guestService.rowLabels(event));
        model.addAttribute("cols", guestService.columnNumbers(event));

        return "guestinvitation/event-dashboard";
    }

    @PostMapping("/organizer/invite")
    public String inviteGuest(@RequestParam Long eventId, @RequestParam Long guestId, HttpSession session) {
        if (ownEvent(eventId, session).filter(this::isPublished).isEmpty()) {
            return "redirect:/organizer/events";
        }
        guestService.inviteGuest(eventId, guestId);
        return "redirect:/organizer/event/" + eventId;
    }

    // --- GUEST DIRECTORY CRUD ENDPOINTS ---

    // READ & CREATE FORM
    @GetMapping("/organizer/guests")
    public String guestDirectory(Model model) {
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("newGuest", new User()); // Empty object for the form
        return "guestinvitation/guest-directory";
    }

    // PROCESS CREATE
    @PostMapping("/organizer/guests/add")
    public String addGuest(@ModelAttribute User newGuest) {
        guestService.addGuest(newGuest);
        return "redirect:/organizer/guests";
    }

    // PROCESS DELETE
    @PostMapping("/organizer/guests/delete/{id}")
    public String deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return "redirect:/organizer/guests";
    }

    // SHOW UPDATE FORM
    @GetMapping("/organizer/guests/edit/{id}")
    public String editGuestForm(@PathVariable Long id, Model model) {
        model.addAttribute("guest", guestService.getGuestById(id));
        return "guestinvitation/guest-edit";
    }

    // PROCESS UPDATE
    @PostMapping("/organizer/guests/edit/{id}")
    public String updateGuest(@PathVariable Long id, @ModelAttribute User user) {
        guestService.updateGuest(id, user);
        return "redirect:/organizer/guests";
    }

    // PROCESS CANCEL INVITE
    @PostMapping("/organizer/invite/cancel")
    public String cancelInvite(@RequestParam Long invitationId, @RequestParam Long eventId, HttpSession session) {
        // Both the event and the invitation must belong to this host
        boolean allowed = ownEvent(eventId, session).isPresent()
                && invitationRepository.findById(invitationId)
                        .map(inv -> inv.getEvent().getEventId().equals(eventId))
                        .orElse(false);
        if (!allowed) {
            return "redirect:/organizer/events";
        }
        guestService.cancelInvitation(invitationId);
        // Redirect right back to the specific event dashboard
        return "redirect:/organizer/event/" + eventId;
    }

    /** An event, but only if it was created by the logged-in host. */
    private Optional<Event> ownEvent(Long eventId, HttpSession session) {
        String hostName = AuthSession.name(session);
        return eventRepository.findById(eventId)
                .filter(e -> hostName != null && hostName.equals(e.getHostUsername()));
    }

    private boolean isPublished(Event event) {
        return event.getStatus() == EventStatus.PUBLISHED;
    }
}
