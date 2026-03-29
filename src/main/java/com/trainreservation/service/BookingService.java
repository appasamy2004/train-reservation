package com.trainreservation.service;

import com.trainreservation.dto.*;
import com.trainreservation.entity.*;
import com.trainreservation.entity.Booking.BookingStatus;
import com.trainreservation.enums.*;
import com.trainreservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository  bookingRepo;
    private final SeatRepository     seatRepo;
    private final TrainScheduleRepository scheduleRepo;
    private final UserRepository     userRepo;

    // ═══════════════════════════════════════════
    // BOOK A TICKET
    // Flow:
    // 1. Seat available?    → CONFIRMED
    // 2. Seat full, RAC ok? → RAC
    // 3. RAC full?          → WAITING LIST
    // ═══════════════════════════════════════════
    @Transactional  // If anything fails, ALL changes are rolled back
    public BookingResponse bookTicket(
            BookingRequest request, Long userId) {

        // Fetch schedule and user — throw error if not found
        TrainSchedule schedule = scheduleRepo
                .findById(request.getScheduleId())
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        SeatType seatType = request.getSeatType();

        // Build the booking object (not saved yet)
        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .passengerName(request.getPassengerName())
                .passengerAge(request.getPassengerAge())
                .gender(request.getGender())
                .seatType(seatType)
                .pnrNumber(generatePNR())
                .amountPaid(BigDecimal.valueOf(seatType.getBasePrice()))
                .bookedAt(LocalDateTime.now())
                .build();

        // ── Step 1: Check for AVAILABLE seat ──
        List<Seat> availableSeats =
                seatRepo.findAvailableSeats(schedule.getId(), seatType);

        if (!availableSeats.isEmpty()) {

            // ✅ Confirmed — grab first available seat
            Seat seat = availableSeats.get(0);
            seat.setStatus(SeatStatus.BOOKED);  // Mark seat as booked
            seatRepo.save(seat);

            booking.setSeat(seat);
            booking.setBookingStatus(BookingStatus.CONFIRMED);

        } else {

            // ── Step 2: Check RAC capacity ──
            long racCount = bookingRepo
                    .findRacBookings(schedule.getId(), seatType).size();

            if (racCount < schedule.getRacCapacity()) {

                // 🟡 RAC booking
                int nextRac = bookingRepo
                        .findMaxRacNumber(schedule.getId(), seatType) + 1;

                booking.setBookingStatus(BookingStatus.RAC);
                booking.setRacNumber(nextRac);

                // Assign a shared RAC seat
                assignRacSeat(schedule, seatType, booking);

            } else {

                // 🔴 Waiting List
                int nextWl = bookingRepo
                        .findMaxWaitingNumber(schedule.getId(), seatType) + 1;

                booking.setBookingStatus(BookingStatus.WAITING);
                booking.setWaitingNumber(nextWl);
                // No seat assigned for waiting list
            }
        }

        // Save booking and return response
        bookingRepo.save(booking);
        return BookingResponse.from(booking);
    }


    // ═══════════════════════════════════════════
    // CANCEL A BOOKING + PROMOTE QUEUE
    // Flow:
    // CONFIRMED cancelled → promote RAC/1 to CONFIRMED
    //                     → promote WL/1 to RAC
    // RAC cancelled       → promote WL/1 to RAC
    // WAITING cancelled   → renumber waiting list
    // ═══════════════════════════════════════════
    @Transactional
    public void cancelBooking(String pnrNumber, Long userId) {

        Booking booking = bookingRepo.findByPnrNumber(pnrNumber)
                .orElseThrow(() ->
                        new RuntimeException("PNR not found: " + pnrNumber));

        // Security check — only the owner can cancel
        if (!booking.getUser().getId().equals(userId))
            throw new RuntimeException("Unauthorized to cancel this booking");

        BookingStatus originalStatus = booking.getBookingStatus();

        // Mark as cancelled
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        if (originalStatus == BookingStatus.CONFIRMED
                && booking.getSeat() != null) {

            // Free the confirmed seat
            Seat releasedSeat = booking.getSeat();
            releasedSeat.setStatus(SeatStatus.AVAILABLE);
            seatRepo.save(releasedSeat);
            booking.setSeat(null);

            // Promote first RAC → CONFIRMED
            promoteRacToConfirmed(
                    booking.getSchedule(),
                    booking.getSeatType(),
                    releasedSeat);

        } else if (originalStatus == BookingStatus.RAC) {

            // Promote first WAITING → RAC
            promoteWaitingToRac(
                    booking.getSchedule(),
                    booking.getSeatType(),
                    booking.getRacNumber());
        }
        // If WAITING cancelled — just renumber the list
        else if (originalStatus == BookingStatus.WAITING) {
            reNumberWaitingList(
                    booking.getSchedule(),
                    booking.getSeatType());
        }

        bookingRepo.save(booking);
    }


    // ═══════════════════════════════════════════
    // GET BOOKINGS FOR A USER
    // Called on "My Bookings" page
    // ═══════════════════════════════════════════
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepo.findByUserIdOrderByBookedAtDesc(userId);
    }


    // ═══════════════════════════════════════════
    // GET BOOKING BY PNR
    // Called on PNR status check page
    // ═══════════════════════════════════════════
    public Booking getBookingByPnr(String pnrNumber) {
        return bookingRepo.findByPnrNumber(pnrNumber)
                .orElseThrow(() ->
                        new RuntimeException("PNR not found: " + pnrNumber));
    }


    // ═══════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════

    // Promote first RAC booking → CONFIRMED
    private void promoteRacToConfirmed(TrainSchedule schedule,
                                       SeatType seatType,
                                       Seat freeSeat) {

        List<Booking> racList =
                bookingRepo.findRacBookings(schedule.getId(), seatType);

        if (!racList.isEmpty()) {
            Booking firstRac = racList.get(0);

            // Give the freed seat to this RAC passenger
            freeSeat.setStatus(SeatStatus.BOOKED);
            seatRepo.save(freeSeat);

            firstRac.setSeat(freeSeat);
            firstRac.setBookingStatus(BookingStatus.CONFIRMED);
            firstRac.setRacNumber(null); // No longer RAC
            bookingRepo.save(firstRac);

            // Now a RAC slot is free — promote first Waiting → RAC
            promoteWaitingToRac(schedule, seatType, 1);
        }
    }

    // Promote first WAITING booking → RAC
    private void promoteWaitingToRac(TrainSchedule schedule,
                                     SeatType seatType,
                                     int newRacNumber) {

        List<Booking> waitList =
                bookingRepo.findWaitingBookings(schedule.getId(), seatType);

        if (!waitList.isEmpty()) {
            Booking firstWaiting = waitList.get(0);

            firstWaiting.setBookingStatus(BookingStatus.RAC);
            firstWaiting.setRacNumber(newRacNumber);
            firstWaiting.setWaitingNumber(null); // No longer Waiting
            bookingRepo.save(firstWaiting);

            // Renumber remaining waiting list
            // WL/2 becomes WL/1, WL/3 becomes WL/2 etc.
            reNumberWaitingList(schedule, seatType);
        }
    }

    // Renumber the waiting list after someone is promoted
    private void reNumberWaitingList(TrainSchedule schedule,
                                     SeatType seatType) {

        List<Booking> remaining =
                bookingRepo.findWaitingBookings(schedule.getId(), seatType);

        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setWaitingNumber(i + 1);
        }
        bookingRepo.saveAll(remaining);
    }

    // Assign a RAC seat — RAC passengers share a berth
    private void assignRacSeat(TrainSchedule schedule,
                               SeatType seatType,
                               Booking booking) {

        // Check if there's already a seat with RAC status
        Optional<Seat> racSeat =
                seatRepo.findFirstRacSeat(schedule.getId(), seatType);

        if (racSeat.isEmpty()) {
            // No RAC seat yet — designate an available seat as RAC
            List<Seat> available =
                    seatRepo.findAvailableSeats(schedule.getId(), seatType);

            if (!available.isEmpty()) {
                Seat s = available.get(0);
                s.setStatus(SeatStatus.RAC);
                seatRepo.save(s);
                booking.setSeat(s);
            }
        } else {
            // Share the existing RAC seat
            booking.setSeat(racSeat.get());
        }
    }

    // Generate a unique PNR number
    // e.g. PNR17234567890001
    private String generatePNR() {
        return "PNR" + System.currentTimeMillis()
                + String.format("%04d", new Random().nextInt(9999));
    }
}