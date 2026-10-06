package com.eventcraft.vendorvenue.service;

import com.eventcraft.vendorvenue.model.Venue;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
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
        if (venue.getImageUrl() == null || venue.getImageUrl().trim().isEmpty()) {
            venue.setImageUrl("https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=800&q=80");
        }
        return venueRepository.save(venue);
    }

    public void deleteVenue(Long id) {
        venueRepository.deleteById(id);
    }

    public long getTotalVenuesCount() {
        return venueRepository.count();
    }
}
