package com.eventcraft.vendorvenue.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "event_id")
    private Long eventId = 1L;

    @NotBlank(message = "Host name is required")
    @Column(name = "host_name", nullable = false, length = 100)
    private String hostName;

    @NotBlank(message = "Host email is required")
    @Email(message = "Valid email is required")
    @Column(name = "host_email", nullable = false, length = 100)
    private String hostEmail;

    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @NotBlank(message = "Booking type is required")
    @Column(name = "booking_type", nullable = false, length = 50)
    private String bookingType; // VENUE, VENDOR

    @NotNull(message = "Booking date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate = LocalDate.now();

    @NotNull(message = "Event date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Event date must be today or in the future")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "booking_status", nullable = false, length = 50)
    private String bookingStatus = "PENDING"; // PENDING, CONFIRMED, CANCELLED

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    // Transient fields for UI convenience
    @Transient
    private Venue venue;

    @Transient
    private Vendor vendor;

    public Booking() {}

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostEmail() { return hostEmail; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
}
