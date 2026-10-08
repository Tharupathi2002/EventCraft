package com.eventcraft.vendorvenue.service;

import com.eventcraft.vendorvenue.entity.Vendor;
import com.eventcraft.vendorvenue.repository.BookingRepository;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public VendorService(VendorRepository vendorRepository, BookingRepository bookingRepository) {
        this.vendorRepository = vendorRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    /** Vendors that can be picked for an event on this date: status AVAILABLE and not already booked that day. */
    public List<Vendor> findAvailableForDate(java.time.LocalDate date) {
        return vendorRepository.findAll().stream()
                .filter(v -> isAvailableOn(v, date))
                .sorted(java.util.Comparator.comparing(Vendor::getBusinessName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public boolean isAvailableOn(Vendor vendor, java.time.LocalDate date) {
        String status = vendor.getStatus();
        boolean statusOk = status == null || "AVAILABLE".equalsIgnoreCase(status);
        return statusOk && (date == null || !bookingRepository.vendorTaken(vendor.getVendorId(), date));
    }

    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    public List<Vendor> searchVendors(String keyword, String serviceType) {
        if ((keyword == null || keyword.trim().isEmpty()) && (serviceType == null || serviceType.trim().isEmpty())) {
            return vendorRepository.findAll();
        }
        return vendorRepository.searchVendors(keyword, serviceType);
    }

    public Vendor saveVendor(Vendor vendor) {
        if (vendor.getStatus() == null || vendor.getStatus().trim().isEmpty()) {
            vendor.setStatus("AVAILABLE");
        }
        if (vendor.getImageUrl() == null || vendor.getImageUrl().trim().isEmpty()) {
            vendor.setImageUrl("https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=800&q=80");
        }
        return vendorRepository.save(vendor);
    }

    public void deleteVendor(Long id) {
        if (bookingRepository.existsByVendorId(id)) {
            throw new IllegalStateException("This vendor has bookings. Cancel and delete those bookings first.");
        }
        vendorRepository.deleteById(id);
    }

    public long getTotalVendorsCount() {
        return vendorRepository.count();
    }
}
