package com.eventcraft.vendorvenue.repository;

import com.eventcraft.vendorvenue.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookingStatus(String bookingStatus);

    List<Booking> findByHostEmail(String hostEmail);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED'")
    long countActiveBookings();

    @Query("SELECT SUM(b.amount) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED'")
    BigDecimal calculateTotalConfirmedAmount();
}
