package com.trainreservation.controller;

import com.trainreservation.service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    // ─────────────────────────────────────────
    // GET / — Home page with search form
    // ─────────────────────────────────────────
    @GetMapping("/")
    public String homePage() {
        return "index"; // → templates/index.html
    }

    // ─────────────────────────────────────────
    // GET /search?from=Chennai&to=Mumbai&date=2024-12-01
    // Show search results
    // ─────────────────────────────────────────
    @GetMapping("/search")
    public String searchTrains(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {

        model.addAttribute("schedules",
                trainService.searchTrains(from, to, date));
        model.addAttribute("from", from);
        model.addAttribute("to",   to);
        model.addAttribute("date", date);

        return "search"; // → templates/search.html
    }
}