package com.trainreservation.enums;

// SeatStatus tracks the current state of every seat
public enum SeatStatus {

    AVAILABLE,  // Nobody booked it — free to book
    BOOKED,     // Fully confirmed booking on this seat
    RAC,        // Seat is shared — passenger gets confirmed if someone cancels
    WAITING     // No seat at all — on waiting list
}