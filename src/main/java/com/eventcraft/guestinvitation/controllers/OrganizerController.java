package com.eventcraft.guestinvitation.controllers;

import com.eventcraft.models.Event;
import com.eventcraft.guestinvitation.models.Invitation;
import com.eventcraft.models.User;
import com.eventcraft.repositories.EventRepository;
import com.eventcraft.guestinvitation.repositories.InvitationRepository;
import com.eventcraft.repositories.UserRepository;
import com.eventcraft.guestinvitation.services.GuestManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrganizerController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final GuestManagementService guestService;

    @GetMapping("/organizer/events")
    public String viewEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "guestinvitation/organizer-events";
    }

    @GetMapping("/organizer/event/{id}")
    public String eventDashboard(@PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id).orElseThrow();

        // Fetch all invitations
        List<Invitation> invitations = invitationRepository.findByEventId(id);

        // Filter to find which specific seats are taken
        List<String> takenSeats = invitations.stream()
                .filter(inv -> "ACCEPTED".equals(inv.getRsvpStatus()) && inv.getSeatNumber() != null)
                .map(Invitation::getSeatNumber)
                .toList();

        model.addAttribute("event", event);
        model.addAttribute("invitations", invitations);
        model.addAttribute("availableUsers", userRepository.findByRole("GUEST"));

        // Pass the seat grid data to the organizer view
        model.addAttribute("takenSeats", takenSeats);
        model.addAttribute("rows", List.of("A", "B", "C", "D"));
        model.addAttribute("cols", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        return "guestinvitation/event-dashboard";
    }

    @PostMapping("/organizer/invite")
    public String inviteGuest(@RequestParam Long eventId, @RequestParam Long guestId) {
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
    public String cancelInvite(@RequestParam Long invitationId, @RequestParam Long eventId) {
        guestService.cancelInvitation(invitationId);
        // Redirect right back to the specific event dashboard
        return "redirect:/organizer/event/" + eventId;
    }
}