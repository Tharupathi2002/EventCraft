package com.eventcraft.vendorvenue.repository;

import com.eventcraft.vendorvenue.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByServiceType(String serviceType);

    @Query("SELECT v FROM Vendor v WHERE " +
           "(:keyword IS NULL OR LOWER(v.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.serviceType) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:serviceType IS NULL OR :serviceType = '' OR LOWER(v.serviceType) = LOWER(:serviceType))")
    List<Vendor> searchVendors(@Param("keyword") String keyword, @Param("serviceType") String serviceType);
}
