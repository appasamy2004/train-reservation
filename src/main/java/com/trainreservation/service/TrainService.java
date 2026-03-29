package com.trainreservation.service;

import com.trainreservation.entity.Train;
import com.trainreservation.entity.TrainSchedule;
import com.trainreservation.repository.TrainRepository;
import com.trainreservation.repository.TrainScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service                    // Tells Spring → this is a Service class, manage it
@RequiredArgsConstructor    // Lombok → auto generates constructor for final fields
public class TrainService {

    // Spring automatically injects these repositories
    // (because of @RequiredArgsConstructor + final)
    private final TrainRepository trainRepo;
    private final TrainScheduleRepository scheduleRepo;

    // ─────────────────────────────────────────
    // Search trains by source, destination, date
    // Called when user searches "Chennai → Mumbai on Dec 1"
    // ─────────────────────────────────────────
    public List<TrainSchedule> searchTrains(
            String source,
            String destination,
            LocalDate date) {

        // Normalize input — "chennai" and "Chennai" should both work
        return scheduleRepo.searchTrains(
                source.trim(),
                destination.trim(),
                date
        );
    }

    // ─────────────────────────────────────────
    // Get a specific schedule by its ID
    // Called when user clicks "Book" on a search result
    // ─────────────────────────────────────────
    public TrainSchedule getScheduleById(Long scheduleId) {
        return scheduleRepo.findById(scheduleId)
                .orElseThrow(() ->
                        new RuntimeException("Schedule not found with id: "
                                + scheduleId));
    }

    // ─────────────────────────────────────────
    // Get all active trains (for admin panel)
    // ─────────────────────────────────────────
    public List<Train> getAllTrains() {
        return trainRepo.findAll();
    }
}