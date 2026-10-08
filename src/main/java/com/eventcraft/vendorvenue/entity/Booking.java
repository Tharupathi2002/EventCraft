package com.eventcraft.vendorvenue.entity;

import com.eventcraft.eventplanning.entity.Event;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A venue or vendor reservation for one of the host's events.
 * The event date and host are taken from the linked {@link Event}; when a booking is
 * confirmed an expense is added to that event's budget (see BookingService).
 */
@Entity
@Table(name = "bookings")
public class Booking {

    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "booking_type", nullable = false, length = 50)
    private String bookingType; // VENUE, VENDOR

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate = LocalDate.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "booking_status", nullable = false, length = 50)
    private String bookingStatus = PENDING;

    @Column(name = "special_requirements", length = 1000)
    private String specialRequirements;

    /** Id of the budget expense created when this booking was confirmed (null if none). */
    @Column(name = "expense_id")
    private Long expenseId;

    @Transient
    private Venue venue;

    @Transient
    private Vendor vendor;

    public Booking() {}

    // ---- values derived from the event (used by the templates) ----

    @Transient
    public Long getEventId() { return event == null ? null : event.getEventId(); }

    @Transient
    public String getEventName() { return event == null ? null : event.getEventName(); }

    @Transient
    public LocalDate getEventDate() { return event == null ? null : event.getEventDate(); }

    @Transient
    public String getHostName() { return event == null ? null : event.getHostUsername(); }

    // ---- getters / setters ----

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
}
