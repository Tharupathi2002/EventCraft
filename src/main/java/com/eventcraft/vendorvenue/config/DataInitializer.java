package com.eventcraft.vendorvenue.config;

import com.eventcraft.vendorvenue.model.Booking;
import com.eventcraft.vendorvenue.model.User;
import com.eventcraft.vendorvenue.model.Vendor;
import com.eventcraft.vendorvenue.model.Venue;
import com.eventcraft.vendorvenue.repository.BookingRepository;
import com.eventcraft.vendorvenue.repository.UserRepository;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final VendorRepository vendorRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           VenueRepository venueRepository,
                           VendorRepository vendorRepository,
                           BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.vendorRepository = vendorRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Clear existing data so fresh LKR converted data (1 USD = 300 LKR) is initialized
        bookingRepository.deleteAll();
        venueRepository.deleteAll();
        vendorRepository.deleteAll();
        userRepository.deleteAll();

        // Seed Users
        userRepository.save(new User("Alexander Wright", "host@eventcraft.com", "pass123", "HOST"));
        userRepository.save(new User("Grand Luxe Venues", "contact@grandluxe.com", "pass123", "VENUE_PROVIDER"));
        userRepository.save(new User("Aura Photography", "info@auraphoto.com", "pass123", "VENDOR"));

        // Seed Venues (Converted at 1 USD = 300 LKR)
        Venue v1 = new Venue("The Grand Cinnamon Ballroom", "77 Galle Road", "Colombo 03", 450, new BigDecimal("750000.00"), 
                "A luxurious ballroom with panoramic sea views, state-of-the-art lighting, and grand chandeliers.", 
                "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=800&q=80");
        v1.setRating(new BigDecimal("4.9"));

        Venue v2 = new Venue("Lotus Pavilion & Gardens", "12 Parliament Road", "Kotte", 300, new BigDecimal("540000.00"), 
                "Open-air lush tropical garden venue with romantic pavilion suitable for weddings and evening galas.", 
                "https://images.unsplash.com/photo-1544078751-58fee2d8a03b?auto=format&fit=crop&w=800&q=80");
        v2.setRating(new BigDecimal("4.7"));

        Venue v3 = new Venue("Highland Haven Estate", "45 Nuwara Eliya Rd", "Nuwara Eliya", 200, new BigDecimal("450000.00"), 
                "Colonial style manor nestled in tea hills with indoor fireplace hall and scenic lawn.", 
                "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80");
        v3.setRating(new BigDecimal("4.8"));

        Venue v4 = new Venue("Citylights Sky Lounge", "100 Union Place", "Colombo 02", 150, new BigDecimal("360000.00"), 
                "Modern rooftop venue with glass infinity deck, ambient LED setup, ideal for corporate events & cocktail parties.", 
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80");
        v4.setRating(new BigDecimal("4.6"));

        venueRepository.save(v1);
        venueRepository.save(v2);
        venueRepository.save(v3);
        venueRepository.save(v4);

        // Seed Vendors (Converted at 1 USD = 300 LKR)
        Vendor vendor1 = new Vendor("Aura Cinematic Photography", "Photography", "+94 77 123 4567 | info@auraphoto.com", new BigDecimal("255000.00"), 
                "Award-winning wedding & event photographers specializing in candid storytelling and drone videography.", 
                "https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=800&q=80");
        vendor1.setRating(new BigDecimal("4.9"));

        Vendor vendor2 = new Vendor("Royal Feast Catering", "Catering", "+94 71 987 6543 | order@royalfeast.lk", new BigDecimal("360000.00"), 
                "Gourmet 5-star catering serving authentic Sri Lankan, Western & Asian fusion buffet spreads.", 
                "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=800&q=80");
        vendor2.setRating(new BigDecimal("4.8"));

        Vendor vendor3 = new Vendor("Enchanted Floral & Decor", "Decor", "+94 76 555 1212 | hello@enchanted.lk", new BigDecimal("195000.00"), 
                "Custom themed stage decorations, floral arches, entrance lighting and table centerpieces.", 
                "https://images.unsplash.com/photo-1478146896981-b80fe463b330?auto=format&fit=crop&w=800&q=80");
        vendor3.setRating(new BigDecimal("4.7"));

        Vendor vendor4 = new Vendor("Sonic Pulse Live Band & DJ", "Music/DJ", "+94 75 444 3322 | booking@sonicpulse.com", new BigDecimal("150000.00"), 
                "Energetic 5-piece live band with pro sound system, intelligent lighting rig and versatile DJ sets.", 
                "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80");
        vendor4.setRating(new BigDecimal("4.8"));

        vendorRepository.save(vendor1);
        vendorRepository.save(vendor2);
        vendorRepository.save(vendor3);
        vendorRepository.save(vendor4);

        // Seed Bookings (Converted at 1 USD = 300 LKR)
        Booking b1 = new Booking();
        b1.setHostName("Sujeewan A.");
        b1.setHostEmail("sujeewan@gmail.com");
        b1.setVenueId(v1.getVenueId());
        b1.setBookingType("VENUE");
        b1.setBookingDate(LocalDate.now().minusDays(5));
        b1.setEventDate(LocalDate.now().plusMonths(2));
        b1.setAmount(new BigDecimal("750000.00"));
        b1.setBookingStatus("CONFIRMED");
        b1.setSpecialRequirements("Require 10 VIP banquet tables setup by 3 PM.");

        Booking b2 = new Booking();
        b2.setHostName("Samantha Perera");
        b2.setHostEmail("samantha@yahoo.com");
        b2.setVendorId(vendor1.getVendorId());
        b2.setBookingType("VENDOR");
        b2.setBookingDate(LocalDate.now().minusDays(3));
        b2.setEventDate(LocalDate.now().plusMonths(3));
        b2.setAmount(new BigDecimal("255000.00"));
        b2.setBookingStatus("PENDING");
        b2.setSpecialRequirements("Drone coverage requested for outdoor garden ceremony.");

        bookingRepository.save(b1);
        bookingRepository.save(b2);
    }
}
