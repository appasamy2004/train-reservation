package com.trainreservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "train_schedules")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many schedules belong to one Train
    // e.g. Train 12345 can run on Dec 1, Dec 2, Dec 3...
    @ManyToOne(fetch = FetchType.LAZY)        // LAZY = don't load Train data unless needed
    @JoinColumn(name = "train_id", nullable = false) // Foreign key column in DB
    private Train train;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;            // e.g. 2024-12-01

    // Seat capacities per class
    @Column(name = "ac_capacity")
    private int acCapacity = 50;

    @Column(name = "sleeper_capacity")
    private int sleeperCapacity = 100;

    @Column(name = "general_capacity")
    private int generalCapacity = 150;

    @Column(name = "rac_capacity")
    private int racCapacity = 10;             // Max RAC passengers allowed

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    // One schedule has many seats
    // mappedBy = "schedule" means Seat.java has a field called "schedule"
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;

    public enum ScheduleStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }
}