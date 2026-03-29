package com.trainreservation.repository;

import com.trainreservation.entity.TrainSchedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    // Custom JPQL query — searches trains by source, destination and date
    // JOIN FETCH loads the Train data along with Schedule in one query
    // (avoids the N+1 query problem)
    @Query("SELECT ts FROM TrainSchedule ts JOIN FETCH ts.train t " +
            "WHERE t.sourceStation = :source " +
            "AND t.destStation = :dest " +
            "AND ts.journeyDate = :date " +
            "AND ts.status = 'SCHEDULED'")
    List<TrainSchedule> searchTrains(
            @Param("source") String source,
            @Param("dest")   String dest,
            @Param("date")   LocalDate date
    );

    // Used by the Scheduler in Step 11
    // Finds all journeys that are past today but still marked SCHEDULED
    @Query("SELECT ts FROM TrainSchedule ts " +
            "WHERE ts.journeyDate < :today " +
            "AND ts.status = 'SCHEDULED'")
    List<TrainSchedule> findCompletedJourneys(@Param("today") LocalDate today);

    // Find a specific schedule by train and date
    Optional<TrainSchedule> findByTrainIdAndJourneyDate(
            Long trainId,
            LocalDate journeyDate
    );
}