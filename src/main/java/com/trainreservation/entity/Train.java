package com.trainreservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "trains")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_number", nullable = false, unique = true)
    private String trainNumber;               // e.g. "12345"

    @Column(name = "train_name", nullable = false)
    private String trainName;                 // e.g. "Chennai Express"

    @Column(name = "source_station", nullable = false)
    private String sourceStation;             // e.g. "Chennai"

    @Column(name = "dest_station", nullable = false)
    private String destStation;               // e.g. "Mumbai"

    @Column(name = "departure_time")
    private LocalTime departureTime;          // e.g. 06:00

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;            // e.g. 22:00

    @Column(name = "total_distance")
    private Integer totalDistance;            // in km

    private boolean active = true;            // false = train decommissioned
}