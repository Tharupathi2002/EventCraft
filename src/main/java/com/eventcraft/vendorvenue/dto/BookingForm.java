package com.eventcraft.vendorvenue.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** What the booking forms submit. The price and event date are never taken from the form. */
public class BookingForm {

    @NotNull(message = "Please choose one of your events")
    private Long eventId;

    private Long venueId;
    private Long vendorId;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String specialRequirements;

    /** Display only (quoted price of the chosen venue/vendor); ignored on submit. */
    private BigDecimal amount;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
