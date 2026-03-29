package com.trainreservation.entity;

import com.trainreservation.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many bookings belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many bookings belong to one Schedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedule schedule;

    // Each booking links to one Seat (nullable for RAC/Waiting)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;                        // NULL if waiting list

    @Column(name = "pnr_number", nullable = false, unique = true)
    private String pnrNumber;                 // e.g. "PNR17234567890001"

    @Column(name = "passenger_name", nullable = false)
    private String passengerName;

    @Column(name = "passenger_age", nullable = false)
    private int passengerAge;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

    @Column(name = "rac_number")
    private Integer racNumber;                // e.g. RAC/1, RAC/2

    @Column(name = "waiting_number")
    private Integer waitingNumber;            // e.g. WL/1, WL/2

    @Column(name = "amount_paid")
    private BigDecimal amountPaid;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;        // Filled when booking cancelled

    public enum BookingStatus {
        CONFIRMED, RAC, WAITING, CANCELLED
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
