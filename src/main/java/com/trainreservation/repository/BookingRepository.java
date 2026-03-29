package com.trainreservation.repository;

import com.trainreservation.entity.Booking;
import com.trainreservation.entity.Booking.BookingStatus;
import com.trainreservation.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find booking by PNR — used for cancellation and status check
    Optional<Booking> findByPnrNumber(String pnrNumber);

    // Get all bookings of a user, newest first
    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    // Get RAC bookings in order — RAC/1 gets promoted first
    @Query("SELECT b FROM Booking b " +
            "WHERE b.schedule.id = :sid " +
            "AND b.seatType = :type " +
            "AND b.bookingStatus = 'RAC' " +
            "ORDER BY b.racNumber ASC")
    List<Booking> findRacBookings(
            @Param("sid")  Long scheduleId,
            @Param("type") SeatType type
    );

    // Get Waiting list in order — WL/1 gets promoted first
    @Query("SELECT b FROM Booking b " +
            "WHERE b.schedule.id = :sid " +
            "AND b.seatType = :type " +
            "AND b.bookingStatus = 'WAITING' " +
            "ORDER BY b.waitingNumber ASC")
    List<Booking> findWaitingBookings(
            @Param("sid")  Long scheduleId,
            @Param("type") SeatType type
    );

    // Get current highest RAC number — so next booking gets RAC/n+1
    @Query("SELECT COALESCE(MAX(b.racNumber), 0) FROM Booking b " +
            "WHERE b.schedule.id = :sid AND b.seatType = :type")
    int findMaxRacNumber(
            @Param("sid")  Long scheduleId,
            @Param("type") SeatType type
    );

    // Get current highest Waiting number — so next booking gets WL/n+1
    @Query("SELECT COALESCE(MAX(b.waitingNumber), 0) FROM Booking b " +
            "WHERE b.schedule.id = :sid AND b.seatType = :type")
    int findMaxWaitingNumber(
            @Param("sid")  Long scheduleId,
            @Param("type") SeatType type
    );

    // Used by Scheduler — get all non-cancelled bookings for a schedule
    List<Booking> findByScheduleIdAndBookingStatusNot(
            Long scheduleId,
            BookingStatus status
    );
}
