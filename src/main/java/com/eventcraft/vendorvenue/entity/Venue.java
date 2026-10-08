package com.eventcraft.vendorvenue.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private Long venueId;

    @NotBlank(message = "Venue name is required")
    @Size(max = 150, message = "Venue name cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Street address is required")
    @Column(nullable = false, length = 150)
    private String street;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 100)
    private String city;

    // No longer entered on the venue form: 0 means "not specified" (the column is NOT NULL in existing databases).
    @Min(value = 0, message = "Capacity cannot be negative")
    @Column(nullable = false)
    private Integer capacity = 0;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
    @Column(name = "rate_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerDay;

    @Lob
    @Column
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("4.5");

    @Column(length = 50)
    private String status = "AVAILABLE"; // AVAILABLE, BOOKED, MAINTENANCE

    @Column(name = "provider_id")
    private Long providerId;

    public Venue() {}

    public Venue(String name, String street, String city, Integer capacity, BigDecimal ratePerDay, String description, String imageUrl) {
        this.name = name;
        this.street = street;
        this.city = city;
        this.capacity = capacity;
        this.ratePerDay = ratePerDay;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public Long getVenueId() { return venueId; }
    public void setVenueId(Long venueId) { this.venueId = venueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public BigDecimal getRatePerDay() { return ratePerDay; }
    public void setRatePerDay(BigDecimal ratePerDay) { this.ratePerDay = ratePerDay; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
}
