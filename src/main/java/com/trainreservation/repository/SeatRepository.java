package com.trainreservation.repository;

import com.trainreservation.entity.Seat;
import com.trainreservation.enums.SeatStatus;
import com.trainreservation.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Find all available seats for a schedule + class
    // Returns ordered by seat number so we assign seat S1-1 before S1-2
    @Query("SELECT s FROM Seat s " +
            "WHERE s.schedule.id = :scheduleId " +
            "AND s.seatType = :type " +
            "AND s.status = 'AVAILABLE' " +
            "ORDER BY s.seatNumber ASC")
    List<Seat> findAvailableSeats(
            @Param("scheduleId") Long scheduleId,
            @Param("type")       SeatType type
    );

    // Count seats by schedule + class + status
    // Used to show "50 Available", "10 Booked" on UI
    long countByScheduleIdAndSeatTypeAndStatus(
            Long scheduleId,
            SeatType seatType,
            SeatStatus status
    );

    // Get all seats for a schedule (for seat map display on UI)
    List<Seat> findByScheduleIdOrderBySeatTypeAscSeatNumberAsc(
            Long scheduleId
    );

    // Find a seat that is currently RAC status
    // Used when cancellation happens — promote RAC to Confirmed
    @Query("SELECT s FROM Seat s " +
            "WHERE s.schedule.id = :sid " +
            "AND s.seatType = :type " +
            "AND s.status = 'RAC' " +
            "ORDER BY s.seatNumber ASC")
    Optional<Seat> findFirstRacSeat(
            @Param("sid")  Long scheduleId,
            @Param("type") SeatType type
    );
}