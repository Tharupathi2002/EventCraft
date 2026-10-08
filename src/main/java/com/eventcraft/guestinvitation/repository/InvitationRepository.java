package com.eventcraft.guestinvitation.repository;
import com.eventcraft.guestinvitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByEvent_EventId(Long eventId);
    boolean existsByEvent_EventIdAndRsvpStatus(Long eventId, String rsvpStatus);
    List<Invitation> findByGuest_Id(Long guestId);
}