package com.trainreservation.dto;

import com.trainreservation.entity.Booking;
import com.trainreservation.entity.Booking.BookingStatus;
import lombok.*;

// This class holds the booking confirmation details
// It travels from Service → Controller → HTML page
@Data
@Builder
public class BookingResponse {

    private String pnrNumber;        // e.g. "PNR17234567890001"
    private String passengerName;    // e.g. "Rahul Kumar"
    private String seatNumber;       // e.g. "S1-3" or "N/A" for waiting
    private String seatType;         // e.g. "Sleeper"
    private String berthType;        // e.g. "LOWER" or "N/A"
    private BookingStatus status;    // CONFIRMED / RAC / WAITING
    private Integer racNumber;       // e.g. 1 if RAC/1
    private Integer waitingNumber;   // e.g. 3 if WL/3
    private double amountPaid;       // e.g. 250.0
    private String trainName;        // e.g. "Chennai Express"
    private String journeyDate;      // e.g. "2024-12-01"
    private String message;          // Human readable status message

    // Static factory method — converts a Booking entity into a BookingResponse DTO
    // Called in BookingService after saving the booking
    public static BookingResponse from(Booking b) {

        // Build a user-friendly message based on booking status
        String msg = switch (b.getBookingStatus()) {
            case CONFIRMED -> "Your seat is confirmed!";
            case RAC       -> "RAC/" + b.getRacNumber() +
                    " - You will be confirmed if someone cancels";
            case WAITING   -> "WL/" + b.getWaitingNumber() +
                    " - You are on the waiting list";
            default        -> "Booking cancelled";
        };

        return BookingResponse.builder()
                .pnrNumber(b.getPnrNumber())
                .passengerName(b.getPassengerName())
                // If no seat assigned (RAC/Waiting), show "N/A"
                .seatNumber(b.getSeat() != null
                        ? b.getSeat().getSeatNumber() : "N/A")
                .seatType(b.getSeatType().getDisplayName())
                .berthType(b.getSeat() != null
                        ? b.getSeat().getBerthType().name() : "N/A")
                .status(b.getBookingStatus())
                .racNumber(b.getRacNumber())
                .waitingNumber(b.getWaitingNumber())
                .amountPaid(b.getAmountPaid().doubleValue())
                .trainName(b.getSchedule().getTrain().getTrainName())
                .journeyDate(b.getSchedule().getJourneyDate().toString())
                .message(msg)
                .build();
    }
}