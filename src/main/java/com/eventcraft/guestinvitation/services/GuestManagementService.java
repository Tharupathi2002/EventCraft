package com.eventcraft.guestinvitation.services;

import com.eventcraft.models.Event;
import com.eventcraft.guestinvitation.models.Invitation;
import com.eventcraft.models.User;
import com.eventcraft.repositories.EventRepository;
import com.eventcraft.guestinvitation.repositories.InvitationRepository;
import com.eventcraft.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestManagementService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public void inviteGuest(Long eventId, Long guestId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        User guest = userRepository.findById(guestId).orElseThrow();

        Invitation invitation = new Invitation();
        invitation.setEvent(event);
        invitation.setGuest(guest);
        invitation.setRsvpStatus("PENDING");

        invitationRepository.save(invitation);
    }

    public void updateRsvpStatus(Long invitationId, String status, String seatNumber) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();
        invitation.setRsvpStatus(status);

        // If they accept, save their chosen seat and deduct availability
        if ("ACCEPTED".equals(status)) {
            invitation.setSeatNumber(seatNumber);
            Event event = invitation.getEvent();
            event.setSeatsAvailable(event.getSeatsAvailable() - 1);
            eventRepository.save(event);
        } else {
            invitation.setSeatNumber(null); // Clear seat if they decline
        }

        invitationRepository.save(invitation);
    }
    // --- CRUD OPERATIONS FOR GUESTS ---

    // READ
    public List<User> getAllGuests() {
        return userRepository.findByRole("GUEST");
    }

    public User getGuestById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    // CREATE
    public void addGuest(User guest) {
        guest.setRole("GUEST"); // Force role so they don't become admins!
        userRepository.save(guest);
    }

    // UPDATE
    public void updateGuest(Long id, User updatedInfo) {
        User existingGuest = getGuestById(id);
        existingGuest.setName(updatedInfo.getName());
        existingGuest.setEmail(updatedInfo.getEmail());
        userRepository.save(existingGuest);
    }

    // DELETE
    public void deleteGuest(Long guestId) {
        // Prevent SQL Foreign Key errors by deleting their invitations first
        List<Invitation> invites = invitationRepository.findByGuestId(guestId);
        invitationRepository.deleteAll(invites);

        userRepository.deleteById(guestId);
    }
    // CANCEL INVITATION
    public void cancelInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();

        // If the guest already accepted, we must refund the seat to the event!
        if ("ACCEPTED".equals(invitation.getRsvpStatus())) {
            Event event = invitation.getEvent();
            event.setSeatsAvailable(event.getSeatsAvailable() + 1);
            eventRepository.save(event);
        }

        // Delete the invite from the database
        invitationRepository.delete(invitation);
    }
}