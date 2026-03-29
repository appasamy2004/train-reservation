package com.trainreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainReservationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainReservationSystemApplication.class, args);
    }

}
