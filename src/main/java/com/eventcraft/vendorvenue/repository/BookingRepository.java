package com.eventcraft.vendorvenue.repository;

import com.eventcraft.vendorvenue.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByEvent_HostUsernameOrderByBookingDateDescBookingIdDesc(String hostUsername);

    List<Booking> findByEvent_HostUsernameAndBookingStatusOrderByBookingDateDescBookingIdDesc(
            String hostUsername, String bookingStatus);

    List<Booking> findByEvent_EventIdAndEvent_HostUsernameOrderByBookingDateDescBookingIdDesc(
            Long eventId, String hostUsername);

    List<Booking> findByEvent_EventId(Long eventId);

    boolean existsByVenueId(Long venueId);

    boolean existsByVendorId(Long vendorId);

    /** Is this venue already reserved (not cancelled) for another event on the same date? */
    @Query("select count(b) > 0 from Booking b where b.venueId = :venueId "
            + "and b.event.eventDate = :date and b.bookingStatus <> 'CANCELLED'")
    boolean venueTaken(@Param("venueId") Long venueId, @Param("date") LocalDate date);

    @Query("select count(b) > 0 from Booking b where b.vendorId = :vendorId "
            + "and b.event.eventDate = :date and b.bookingStatus <> 'CANCELLED'")
    boolean vendorTaken(@Param("vendorId") Long vendorId, @Param("date") LocalDate date);

    @Query("select count(b) from Booking b where b.event.hostUsername = :host and b.bookingStatus = 'CONFIRMED'")
    long countConfirmedForHost(@Param("host") String host);

    @Query("select coalesce(sum(b.amount), 0) from Booking b where b.event.hostUsername = :host and b.bookingStatus = 'CONFIRMED'")
    BigDecimal sumConfirmedForHost(@Param("host") String host);
}
