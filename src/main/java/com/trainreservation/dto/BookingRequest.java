package com.trainreservation.dto;

import com.trainreservation.entity.Booking.Gender;
import com.trainreservation.enums.SeatType;
import jakarta.validation.constraints.*;
import lombok.*;

// This class holds the data a user submits when booking a ticket
// It travels from the HTML form → Controller → Service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    // Which train schedule they want to book
    // @NotNull means this field MUST be provided — can't be empty
    @NotNull(message = "Schedule is required")
    private Long scheduleId;

    // Passenger details
    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    // Age must be between 1 and 120
    @Min(value = 1,   message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be under 120")
    private int passengerAge;

    // Optional — can be MALE, FEMALE, or OTHER
    private Gender gender;

    // Which class they want — SLEEPER, AC_3TIER etc.
    @NotNull(message = "Please select a seat type")
    private SeatType seatType;
}