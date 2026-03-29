package com.trainreservation.repository;

import com.trainreservation.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    // Find train by its train number (e.g. "12345")
    // SELECT * FROM trains WHERE train_number = ?
    Optional<Train> findByTrainNumber(String trainNumber);

    // Find all active trains from a source to destination
    // SELECT * FROM trains WHERE source_station = ?
    //   AND dest_station = ? AND active = true
    List<Train> findBySourceStationAndDestStationAndActiveTrue(
            String sourceStation,
            String destStation
    );
}