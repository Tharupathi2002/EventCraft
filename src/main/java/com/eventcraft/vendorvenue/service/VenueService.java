package com.eventcraft.vendorvenue.service;

import com.eventcraft.vendorvenue.entity.Venue;
import com.eventcraft.vendorvenue.repository.BookingRepository;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository, BookingRepository bookingRepository) {
        this.venueRepository = venueRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    public List<Venue> searchVenues(String keyword, String city) {
        if ((keyword == null || keyword.trim().isEmpty()) && (city == null || city.trim().isEmpty())) {
            return venueRepository.findAll();
        }
        return venueRepository.searchVenues(keyword, city);
    }

    public Venue saveVenue(Venue venue) {
        if (venue.getVenueId() != null) {
            // The form no longer has capacity/status (or rating/provider), so keep what is stored.
            venueRepository.findById(venue.getVenueId()).ifPresent(existing -> {
                venue.setStatus(existing.getStatus());
                venue.setCapacity(existing.getCapacity());
                venue.setRating(existing.getRating());
                venue.setProviderId(existing.getProviderId());
            });
        }
        if (venue.getCapacity() == null) {
            venue.setCapacity(0);
        }
        if (venue.getImageUrl() == null || venue.getImageUrl().trim().isEmpty()) {
            venue.setImageUrl("https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=800&q=80");
        }
        return venueRepository.save(venue);
    }

    public void deleteVenue(Long id) {
        if (bookingRepository.existsByVenueId(id)) {
            throw new IllegalStateException("This venue has bookings. Cancel and delete those bookings first.");
        }
        venueRepository.deleteById(id);
    }

    public long getTotalVenuesCount() {
        return venueRepository.count();
    }
}
