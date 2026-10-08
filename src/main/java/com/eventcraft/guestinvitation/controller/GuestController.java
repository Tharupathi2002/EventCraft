package com.eventcraft.guestinvitation.controller;

import com.eventcraft.auth.AuthSession;
import com.eventcraft.guestinvitation.entity.Invitation;
import com.eventcraft.guestinvitation.entity.User;
import com.eventcraft.guestinvitation.repository.InvitationRepository;
import com.eventcraft.guestinvitation.repository.UserRepository;
import com.eventcraft.guestinvitation.service.GuestManagementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GuestController {
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final GuestManagementService guestService;

    @GetMapping("/guest/dashboard")
    public String guestDashboard(Model model, HttpSession session) {
        Optional<User> current = userRepository.findById(AuthSession.userId(session));
        if (current.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        User guest = current.get();
        model.addAttribute("guestName", guest.getName());
        // Only this guest's own invitations
        model.addAttribute("invitations", invitationRepository.findByGuest_Id(guest.getId()));
        return "guestinvitation/guest-dashboard";
    }

    @PostMapping("/guest/seat-selection")
    public String showSeatSelection(@RequestParam Long invitationId, @RequestParam String status,
                                    Model model, HttpSession session) {
        Optional<Invitation> owned = ownInvitation(invitationId, session);
        if (owned.isEmpty()) {
            return "redirect:/guest/dashboard";
        }
        Invitation invitation = owned.get();

        if ("DECLINED".equals(status)) {
            guestService.updateRsvpStatus(invitationId, status, null);
            return "redirect:/guest/dashboard";
        }

        List<String> takenSeats = guestService.takenSeats(invitation.getEvent().getEventId());

        model.addAttribute("invitationId", invitationId);
        model.addAttribute("takenSeats", takenSeats);
        model.addAttribute("rows", guestService.rowLabels(invitation.getEvent()));
        model.addAttribute("cols", guestService.columnNumbers(invitation.getEvent()));

        return "guestinvitation/seat-selection";
    }

    // 2. Process the Final Seat Choice
    @PostMapping("/guest/rsvp")
    public String processRsvp(@RequestParam Long invitationId, @RequestParam String seatNumber,
                              HttpSession session) {
        if (ownInvitation(invitationId, session).isEmpty()) {
            return "redirect:/guest/dashboard";
        }
        try {
            guestService.updateRsvpStatus(invitationId, "ACCEPTED", seatNumber);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "redirect:/guest/dashboard?seatError";
        }
        return "redirect:/guest/dashboard";
    }

    /** An invitation, but only if it belongs to the logged-in guest. */
    private Optional<Invitation> ownInvitation(Long invitationId, HttpSession session) {
        Long userId = AuthSession.userId(session);
        return invitationRepository.findById(invitationId)
                .filter(inv -> inv.getGuest() != null && inv.getGuest().getId().equals(userId));
    }
}
