package com.eventcraft.vendorvenue.service;

import com.eventcraft.vendorvenue.model.Vendor;
import com.eventcraft.vendorvenue.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;

    @Autowired
    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
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
        if (vendor.getImageUrl() == null || vendor.getImageUrl().trim().isEmpty()) {
            vendor.setImageUrl("https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=800&q=80");
        }
        return vendorRepository.save(vendor);
    }

    public void deleteVendor(Long id) {
        vendorRepository.deleteById(id);
    }

    public long getTotalVendorsCount() {
        return vendorRepository.count();
    }
}
