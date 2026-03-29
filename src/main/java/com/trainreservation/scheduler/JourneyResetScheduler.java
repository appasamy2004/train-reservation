package com.trainreservation.scheduler;

import com.trainreservation.entity.*;
import com.trainreservation.entity.Booking.BookingStatus;
import com.trainreservation.entity.TrainSchedule.ScheduleStatus;
import com.trainreservation.enums.SeatStatus;
import com.trainreservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component          // Tells Spring → manage this class
@RequiredArgsConstructor
@Slf4j              // Lombok → gives us log.info(), log.error() etc.
public class JourneyResetScheduler {

    private final TrainScheduleRepository scheduleRepo;
    private final BookingRepository       bookingRepo;
    private final SeatRepository          seatRepo;

    // ═══════════════════════════════════════════
    // Runs every day at midnight automatically
    // Cron format: second minute hour day month weekday
    // "0 0 0 * * *" = at 00:00:00 every day
    // ═══════════════════════════════════════════
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetCompletedJourneys() {

        LocalDate today = LocalDate.now();

        log.info("⏰ Scheduler running — checking for " +
                "completed journeys before {}", today);

        // Find all schedules whose journey date has passed
        // but are still marked SCHEDULED
        List<TrainSchedule> completedJourneys =
                scheduleRepo.findCompletedJourneys(today);

        if (completedJourneys.isEmpty()) {
            log.info("✅ No completed journeys to reset today.");
            return;
        }

        for (TrainSchedule schedule : completedJourneys) {

            log.info("🔄 Resetting schedule ID: {} | Train: {} | Date: {}",
                    schedule.getId(),
                    schedule.getTrain().getTrainName(),
                    schedule.getJourneyDate());

            // ── Step 1: Mark schedule as COMPLETED ──
            schedule.setStatus(ScheduleStatus.COMPLETED);
            scheduleRepo.save(schedule);

            // ── Step 2: Cancel all active bookings ──
            // (bookings that are NOT already cancelled)
            List<Booking> activeBookings = bookingRepo
                    .findByScheduleIdAndBookingStatusNot(
                            schedule.getId(),
                            BookingStatus.CANCELLED);

            activeBookings.forEach(booking ->
                    booking.setBookingStatus(BookingStatus.CANCELLED));

            bookingRepo.saveAll(activeBookings);

            log.info("   ❌ Cancelled {} bookings",
                    activeBookings.size());

            // ── Step 3: Reset all seats back to AVAILABLE ──
            List<Seat> seats = schedule.getSeats();

            if (seats != null && !seats.isEmpty()) {
                seats.forEach(seat ->
                        seat.setStatus(SeatStatus.AVAILABLE));
                seatRepo.saveAll(seats);

                log.info("   💺 Reset {} seats to AVAILABLE",
                        seats.size());
            }
        }

        log.info("✅ Scheduler done — reset {} journeys",
                completedJourneys.size());
    }


    // ═══════════════════════════════════════════
    // BONUS: Runs every hour — logs a health check
    // Helps confirm the scheduler is alive
    // ═══════════════════════════════════════════
    @Scheduled(cron = "0 0 * * * *")
    public void healthCheck() {
        log.info("💓 Scheduler health check — {}",
                LocalDate.now());
    }
}