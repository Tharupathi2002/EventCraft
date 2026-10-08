package com.eventcraft.vendorvenue.repository;

import com.eventcraft.vendorvenue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByStatus(String status);

    @Query("SELECT v FROM Venue v WHERE " +
           "(:keyword IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.city) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(v.city) = LOWER(:city))")
    List<Venue> searchVenues(@Param("keyword") String keyword, @Param("city") String city);
}
