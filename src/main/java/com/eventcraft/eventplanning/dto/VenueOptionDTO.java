package com.eventcraft.eventplanning.dto;

import java.math.BigDecimal;

/** One entry in the "Select a venue" dropdown of the event creation form. */
public record VenueOptionDTO(Long venueId, String name, String city, Integer capacity, BigDecimal ratePerDay) {
}
