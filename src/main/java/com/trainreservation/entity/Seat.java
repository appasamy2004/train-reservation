package com.trainreservation.entity;

import com.trainreservation.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many seats belong to one Schedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedule schedule;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;                // e.g. "S1-1" (Coach S1, Seat 1)

    @Column(name = "coach_number", length = 10)
    private String coachNumber;               // e.g. "S1", "B2", "A1"

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;                // AC_FIRST, SLEEPER etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "berth_type")
    private BerthType berthType = BerthType.NONE; // LOWER, UPPER etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE; // Default = available
}