package com.eventcraft.vendorvenue.config;

import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.entity.Venue;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import com.eventcraft.vendorvenue.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VendorVenueDataInitializer implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final VendorRepository vendorRepository;

    @Autowired
    public VendorVenueDataInitializer(VenueRepository venueRepository,
                           VendorRepository vendorRepository) {
        this.venueRepository = venueRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed sample data only into empty tables; never wipe existing data
        // (the shared users table belongs to the auth module and is left untouched).
        if (venueRepository.count() > 0 || vendorRepository.count() > 0) {
            return;
        }

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
    }
}
