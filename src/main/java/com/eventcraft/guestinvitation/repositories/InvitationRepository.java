package com.eventcraft.guestinvitation.repositories;
import com.eventcraft.guestinvitation.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByEventId(Long eventId);
    List<Invitation> findByGuestId(Long guestId);
}