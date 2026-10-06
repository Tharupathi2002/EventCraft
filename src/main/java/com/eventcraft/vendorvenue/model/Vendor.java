package com.eventcraft.vendorvenue.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_id")
    private Long vendorId;

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @NotBlank(message = "Service type is required")
    @Column(name = "service_type", nullable = false, length = 100)
    private String serviceType; // Catering, Photography, Decor, Music/DJ, Event Planner

    @NotBlank(message = "Contact information is required")
    @Column(name = "contact_info", nullable = false, length = 150)
    private String contactInfo;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("4.8");

    @Column(length = 50)
    private String status = "AVAILABLE"; // AVAILABLE, BUSY, INACTIVE

    @Column(name = "user_id")
    private Long userId;

    public Vendor() {}

    public Vendor(String businessName, String serviceType, String contactInfo, BigDecimal basePrice, String description, String imageUrl) {
        this.businessName = businessName;
        this.serviceType = serviceType;
        this.contactInfo = contactInfo;
        this.basePrice = basePrice;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
