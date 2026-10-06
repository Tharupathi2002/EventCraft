package com.eventcraft.vendorvenue.service;

import com.eventcraft.vendorvenue.model.Booking;
import com.eventcraft.vendorvenue.repository.BookingRepository;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final VendorRepository vendorRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, VenueRepository venueRepository, VendorRepository vendorRepository) {
        this.bookingRepository = bookingRepository;
        this.venueRepository = venueRepository;
        this.vendorRepository = vendorRepository;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        bookings.forEach(this::enrichBookingDetails);
        return bookings;
    }

    public Optional<Booking> getBookingById(Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        booking.ifPresent(this::enrichBookingDetails);
        return booking;
    }

    public List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = bookingRepository.findByBookingStatus(status);
        bookings.forEach(this::enrichBookingDetails);
        return bookings;
    }

    public Booking createBooking(Booking booking) {
        if (booking.getVenueId() != null) {
            venueRepository.findById(booking.getVenueId()).ifPresent(venue -> {
                if (booking.getAmount() == null) {
                    booking.setAmount(venue.getRatePerDay());
                }
                booking.setBookingType("VENUE");
            });
        } else if (booking.getVendorId() != null) {
            vendorRepository.findById(booking.getVendorId()).ifPresent(vendor -> {
                if (booking.getAmount() == null) {
                    booking.setAmount(vendor.getBasePrice());
                }
                booking.setBookingType("VENDOR");
            });
        }
        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        booking.setBookingStatus(status);
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    public long getTotalBookingsCount() {
        return bookingRepository.count();
    }

    public long getActiveBookingsCount() {
        return bookingRepository.countActiveBookings();
    }

    public BigDecimal getTotalSpendAmount() {
        BigDecimal total = bookingRepository.calculateTotalConfirmedAmount();
        return total != null ? total : BigDecimal.ZERO;
    }

    private void enrichBookingDetails(Booking booking) {
        if (booking.getVenueId() != null) {
            venueRepository.findById(booking.getVenueId()).ifPresent(booking::setVenue);
        }
        if (booking.getVendorId() != null) {
            vendorRepository.findById(booking.getVendorId()).ifPresent(booking::setVendor);
        }
    }
}
