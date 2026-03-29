package com.trainreservation.dto;

import com.trainreservation.enums.SeatType;
import lombok.*;
import java.util.Map;

// Holds how many seats are available, booked, or RAC
// for each seat class (AC, Sleeper, General etc.)
@Data
@Builder
public class SeatAvailabilityDTO {

    private Long scheduleId;

    // Map<SeatType, count> — e.g. {SLEEPER=45, AC_3TIER=30, GENERAL=100}
    private Map<SeatType, Long> available;

    // How many are already booked per class
    private Map<SeatType, Long> booked;

    // How many are in RAC per class
    private Map<SeatType, Long> rac;
}