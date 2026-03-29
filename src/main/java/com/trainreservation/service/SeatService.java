package com.trainreservation.service;

import com.trainreservation.dto.SeatAvailabilityDTO;
import com.trainreservation.entity.Seat;
import com.trainreservation.entity.TrainSchedule;
import com.trainreservation.enums.*;
import com.trainreservation.repository.SeatRepository;
import com.trainreservation.repository.TrainScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepo;
    private final TrainScheduleRepository scheduleRepo;

    // ─────────────────────────────────────────
    // Get seat availability counts per class
    // e.g. { SLEEPER=45, AC_3TIER=30, GENERAL=100 }
    // ─────────────────────────────────────────
    public SeatAvailabilityDTO getAvailability(Long scheduleId) {

        scheduleRepo.findById(scheduleId)
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found"));

        Map<SeatType, Long> available = new HashMap<>();
        Map<SeatType, Long> booked   = new HashMap<>();
        Map<SeatType, Long> rac      = new HashMap<>();

        // Loop through all seat types and count each status
        for (SeatType type : SeatType.values()) {
            available.put(type,
                    seatRepo.countByScheduleIdAndSeatTypeAndStatus(
                            scheduleId, type, SeatStatus.AVAILABLE));

            booked.put(type,
                    seatRepo.countByScheduleIdAndSeatTypeAndStatus(
                            scheduleId, type, SeatStatus.BOOKED));

            rac.put(type,
                    seatRepo.countByScheduleIdAndSeatTypeAndStatus(
                            scheduleId, type, SeatStatus.RAC));
        }

        return SeatAvailabilityDTO.builder()
                .scheduleId(scheduleId)
                .available(available)
                .booked(booked)
                .rac(rac)
                .build();
    }

    // ─────────────────────────────────────────
    // Get seat layout grouped by coach
    // e.g. { "SLEEPER-S1" → [seat1, seat2...],
    //         "AC_3TIER-B1" → [seat1, seat2...] }
    // Used to display the visual seat map on UI
    // ─────────────────────────────────────────
    public Map<String, List<Seat>> getSeatLayout(Long scheduleId) {

        List<Seat> seats = seatRepo
                .findByScheduleIdOrderBySeatTypeAscSeatNumberAsc(scheduleId);

        // Group seats by "SeatType-CoachNumber"
        // e.g. "SLEEPER-S1", "AC_3TIER-B2"
        return seats.stream().collect(
                Collectors.groupingBy(s ->
                        s.getSeatType().name() + "-" + s.getCoachNumber()));
    }

    // ─────────────────────────────────────────
    // Initialize all seats for a new schedule
    // Called when admin creates a new train schedule
    // ─────────────────────────────────────────
    public void initializeSeats(TrainSchedule schedule) {
        List<Seat> seats = new ArrayList<>();

        // Generate seats for each class
        seats.addAll(generateSleeperSeats(schedule));
        seats.addAll(generateAcSeats(
                schedule, SeatType.AC_3TIER, 6, "B"));
        seats.addAll(generateAcSeats(
                schedule, SeatType.AC_2TIER, 4, "A"));
        seats.addAll(generateGeneralSeats(schedule));

        seatRepo.saveAll(seats);
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER — Generate Sleeper seats
    // 8 berths per compartment:
    // Lower, Middle, Upper (x2 sets) + Side Lower, Side Upper
    // ─────────────────────────────────────────
    private List<Seat> generateSleeperSeats(TrainSchedule schedule) {
        List<Seat> seats = new ArrayList<>();

        // Number of compartments = capacity / 8 berths per compartment
        int compartments = schedule.getSleeperCapacity() / 8;

        for (int c = 1; c <= compartments; c++) {
            String coach = "S" + c;  // S1, S2, S3...

            BerthType[] berths = {
                    BerthType.LOWER, BerthType.MIDDLE, BerthType.UPPER,
                    BerthType.LOWER, BerthType.MIDDLE, BerthType.UPPER,
                    BerthType.SIDE_LOWER, BerthType.SIDE_UPPER
            };

            for (int b = 0; b < berths.length; b++) {
                seats.add(Seat.builder()
                        .schedule(schedule)
                        .seatNumber(coach + "-" + (b + 1)) // e.g. S1-1, S1-2
                        .coachNumber(coach)
                        .seatType(SeatType.SLEEPER)
                        .berthType(berths[b])
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        return seats;
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER — Generate AC seats
    // AC 3-Tier: Lower, Middle, Upper, Side Lower, Side Upper
    // AC 2-Tier: Lower, Upper, Side Lower, Side Upper
    // ─────────────────────────────────────────
    private List<Seat> generateAcSeats(TrainSchedule schedule,
                                       SeatType type,
                                       int compartments,
                                       String prefix) {
        List<Seat> seats = new ArrayList<>();

        for (int c = 1; c <= compartments; c++) {
            String coach = prefix + c; // B1, B2... or A1, A2...

            BerthType[] berths = (type == SeatType.AC_3TIER)
                    ? new BerthType[]{
                    BerthType.LOWER, BerthType.MIDDLE, BerthType.UPPER,
                    BerthType.SIDE_LOWER, BerthType.SIDE_UPPER}
                    : new BerthType[]{
                    BerthType.LOWER, BerthType.UPPER,
                    BerthType.SIDE_LOWER, BerthType.SIDE_UPPER};

            for (int b = 0; b < berths.length; b++) {
                seats.add(Seat.builder()
                        .schedule(schedule)
                        .seatNumber(coach + "-" + (b + 1))
                        .coachNumber(coach)
                        .seatType(type)
                        .berthType(berths[b])
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        return seats;
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER — Generate General seats
    // Simple numbered seats — no berth concept
    // ─────────────────────────────────────────
    private List<Seat> generateGeneralSeats(TrainSchedule schedule) {
        List<Seat> seats = new ArrayList<>();

        for (int i = 1; i <= schedule.getGeneralCapacity(); i++) {
            seats.add(Seat.builder()
                    .schedule(schedule)
                    .seatNumber("GEN-" + i)   // GEN-1, GEN-2...
                    .coachNumber("G1")
                    .seatType(SeatType.GENERAL)
                    .berthType(BerthType.NONE)
                    .status(SeatStatus.AVAILABLE)
                    .build());
        }
        return seats;
    }
}