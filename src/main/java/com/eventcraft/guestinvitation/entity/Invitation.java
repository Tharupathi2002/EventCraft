package com.eventcraft.guestinvitation.entity;

import com.eventcraft.eventplanning.entity.Event;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invitations")
@Data
@NoArgsConstructor
public class Invitation {

    private String seatNumber;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User guest;

    // "PENDING", "ACCEPTED", or "DECLINED"
    private String rsvpStatus = "PENDING";
}