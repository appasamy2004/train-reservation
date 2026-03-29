package com.trainreservation.controller;

import com.trainreservation.dto.SeatAvailabilityDTO;
import com.trainreservation.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController = @Controller + automatically converts
// return value to JSON (instead of looking for an HTML file)
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // ─────────────────────────────────────────
    // GET /api/seats/availability?scheduleId=1
    // Returns JSON with available seat counts
    // e.g. {"scheduleId":1,"available":{"SLEEPER":45}...}
    // ─────────────────────────────────────────
    @GetMapping("/availability")
    public ResponseEntity<SeatAvailabilityDTO> getAvailability(
            @RequestParam Long scheduleId) {

        return ResponseEntity.ok(
                seatService.getAvailability(scheduleId));
    }

    // ─────────────────────────────────────────
    // GET /api/seats/layout?scheduleId=1
    // Returns JSON with full seat layout grouped by coach
    // ─────────────────────────────────────────
    @GetMapping("/layout")
    public ResponseEntity<?> getLayout(
            @RequestParam Long scheduleId) {

        return ResponseEntity.ok(
                seatService.getSeatLayout(scheduleId));
    }
}
//```
//
//        ---
//
//        ### 📌 Build Check
//
//        Press **Ctrl + F9**:
//        ```
//        Build completed successfully ✅
//        ```
//
//        Now **run the app** and open browser:
//        ```
//        http://localhost:8080
//        ```
//        You'll see a **White Label Error page** — that's fine! We haven't created HTML pages yet. But it means the app is running correctly! ✅
//
//        ---
//
//        ### 🧠 How A Request Flows Through The App
//```
//        Browser: GET /search?from=Chennai&to=Mumbai&date=2024-12-01
//        ↓
//        TrainController.searchTrains()
//            ↓
//                    TrainService.searchTrains()
//            ↓
//                    TrainScheduleRepository.searchTrains()  ← hits MySQL
//            ↓
//        Returns List<TrainSchedule>
//            ↓
//                    model.addAttribute("schedules", result)
//            ↓
//        returns "search"  →  templates/search.html
//            ↓
//        Browser sees the search results page