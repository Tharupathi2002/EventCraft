package com.eventcraft.guestinvitation.service;

import com.eventcraft.eventplanning.entity.Event;
import com.eventcraft.guestinvitation.entity.Invitation;
import com.eventcraft.guestinvitation.entity.User;
import com.eventcraft.eventplanning.repository.EventRepository;
import com.eventcraft.guestinvitation.repository.InvitationRepository;
import com.eventcraft.guestinvitation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GuestManagementService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;

    public void inviteGuest(Long eventId, Long guestId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        User guest = userRepository.findById(guestId).orElseThrow();
        if (!"GUEST".equals(guest.getRole())) {
            throw new IllegalArgumentException("Only guests can be invited.");
        }
        boolean alreadyInvited = invitationRepository.findByEvent_EventId(eventId).stream()
                .anyMatch(inv -> inv.getGuest().getId().equals(guestId));
        if (alreadyInvited) {
            return;
        }

        Invitation invitation = new Invitation();
        invitation.setEvent(event);
        invitation.setGuest(guest);
        invitation.setRsvpStatus("PENDING");

        invitationRepository.save(invitation);
    }

    public void updateRsvpStatus(Long invitationId, String status, String seatNumber) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();
        Event event = invitation.getEvent();
        boolean wasAccepted = "ACCEPTED".equals(invitation.getRsvpStatus());

        if ("ACCEPTED".equals(status)) {
            validateSeat(invitation, seatNumber);
            invitation.setSeatNumber(seatNumber);
            if (!wasAccepted) {
                event.setSeatsAvailable(seatsLeft(event) - 1);
                eventRepository.save(event);
            }
        } else {
            if (wasAccepted) {
                event.setSeatsAvailable(seatsLeft(event) + 1);
                eventRepository.save(event);
            }
            invitation.setSeatNumber(null); // Clear seat if they decline
        }

        invitation.setRsvpStatus(status);
        invitationRepository.save(invitation);
    }

    // --- SEAT LAYOUT (driven by the rows/columns the host set on the event) ---

    /** Row labels A, B, C... for the event's number of rows. */
    public List<String> rowLabels(Event event) {
        int rows = event.getSeatRows() != null ? event.getSeatRows() : 4;
        return IntStream.range(0, rows).mapToObj(i -> String.valueOf((char) ('A' + i))).toList();
    }

    /** Column numbers 1, 2, 3... for the event's number of columns. */
    public List<Integer> columnNumbers(Event event) {
        int cols = event.getSeatColumns() != null ? event.getSeatColumns() : 10;
        return IntStream.rangeClosed(1, cols).boxed().toList();
    }

    /** Seat ids (e.g. "B7") already taken by guests who accepted. */
    public List<String> takenSeats(Long eventId) {
        return invitationRepository.findByEvent_EventId(eventId).stream()
                .filter(inv -> "ACCEPTED".equals(inv.getRsvpStatus()) && inv.getSeatNumber() != null)
                .map(Invitation::getSeatNumber)
                .toList();
    }

    private void validateSeat(Invitation invitation, String seatNumber) {
        Event event = invitation.getEvent();
        if (seatNumber == null || !seatNumber.matches("[A-Z][0-9]{1,2}")) {
            throw new IllegalArgumentException("Please choose a valid seat.");
        }
        int row = seatNumber.charAt(0) - 'A' + 1;
        int col = Integer.parseInt(seatNumber.substring(1));
        if (row > rowLabels(event).size() || col < 1 || col > columnNumbers(event).size()) {
            throw new IllegalArgumentException("That seat doesn't exist for this event.");
        }
        boolean taken = invitationRepository.findByEvent_EventId(event.getEventId()).stream()
                .anyMatch(inv -> !inv.getId().equals(invitation.getId())
                        && "ACCEPTED".equals(inv.getRsvpStatus())
                        && seatNumber.equals(inv.getSeatNumber()));
        if (taken) {
            throw new IllegalStateException("That seat has just been taken.");
        }
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
        requireUnregistered(existingGuest);
        existingGuest.setName(updatedInfo.getName());
        existingGuest.setEmail(updatedInfo.getEmail());
        userRepository.save(existingGuest);
    }

    // DELETE
    public void deleteGuest(Long guestId) {
        requireUnregistered(getGuestById(guestId));
        // Prevent SQL Foreign Key errors by deleting their invitations first
        List<Invitation> invites = invitationRepository.findByGuest_Id(guestId);
        invitationRepository.deleteAll(invites);

        userRepository.deleteById(guestId);
    }
    // CANCEL INVITATION
    public void cancelInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow();

        // If the guest already accepted, we must refund the seat to the event!
        if ("ACCEPTED".equals(invitation.getRsvpStatus())) {
            Event event = invitation.getEvent();
            event.setSeatsAvailable(seatsLeft(event) + 1);
            eventRepository.save(event);
        }

        // Delete the invite from the database
        invitationRepository.delete(invitation);
    }

    private int seatsLeft(Event event) {
        return event.getSeatsAvailable() != null ? event.getSeatsAvailable() : event.getTotalSeats();
    }

    /**
     * Guests who signed up themselves own their account (their email is their login),
     * so hosts can only edit/delete directory entries that were added by a host.
     */
    private void requireUnregistered(User guest) {
        if (!"GUEST".equals(guest.getRole()) || guest.getPassword() != null) {
            throw new IllegalArgumentException("This guest has their own account and can't be changed from the directory.");
        }
    }
}
