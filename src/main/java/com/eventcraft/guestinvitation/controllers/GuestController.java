package com.eventcraft.guestinvitation.controllers;

import com.eventcraft.guestinvitation.models.Invitation;
import com.eventcraft.models.User;
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
public class GuestController {
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GuestManagementService guestService;

    @GetMapping("/guest/dashboard")
    public String guestDashboard(Model model) {
        // For the prototype, we automatically log in our mock guest
        List<User> guests = userRepository.findByRole("GUEST");
        if (!guests.isEmpty()) {
            User currentGuest = guests.get(0);
            model.addAttribute("guestName", currentGuest.getName());
            model.addAttribute("invitations", invitationRepository.findByGuestId(currentGuest.getId()));
        }
        return "guestinvitation/guest-dashboard";
    }

    @PostMapping("/guest/seat-selection")
    public String showSeatSelection(@RequestParam Long invitationId, @RequestParam String status, Model model) {
        if ("DECLINED".equals(status)) {
            guestService.updateRsvpStatus(invitationId, status, null);
            return "redirect:/guest/dashboard";
        }

        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();
        List<Invitation> allEventInvites = invitationRepository.findByEventId(invitation.getEvent().getId());
        List<String> takenSeats = allEventInvites.stream()
                .filter(inv -> "ACCEPTED".equals(inv.getRsvpStatus()) && inv.getSeatNumber() != null)
                .map(Invitation::getSeatNumber)
                .toList();

        model.addAttribute("invitationId", invitationId);
        model.addAttribute("takenSeats", takenSeats);
        model.addAttribute("rows", List.of("A", "B", "C", "D"));
        model.addAttribute("cols", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        // Update this line to point to the correct folder
        return "guestinvitation/seat-selection";
    }

    // 2. Process the Final Seat Choice
    @PostMapping("/guest/rsvp")
    public String processRsvp(@RequestParam Long invitationId, @RequestParam String seatNumber) {
        guestService.updateRsvpStatus(invitationId, "ACCEPTED", seatNumber);
        return "redirect:/guest/dashboard";
    }
}