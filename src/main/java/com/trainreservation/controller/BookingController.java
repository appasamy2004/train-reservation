package com.trainreservation.controller;

import com.trainreservation.config.CustomUserDetails;
import com.trainreservation.dto.*;
import com.trainreservation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributes;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SeatService    seatService;
    private final TrainService   trainService;

    // ─────────────────────────────────────────
    // GET /booking/select?scheduleId=1
    // Show seat selection page
    // ─────────────────────────────────────────
    @GetMapping("/select")
    public String seatSelectionPage(
            @RequestParam Long scheduleId,
            Model model) {

        model.addAttribute("schedule",
                trainService.getScheduleById(scheduleId));
        model.addAttribute("availability",
                seatService.getAvailability(scheduleId));
        model.addAttribute("seatLayout",
                seatService.getSeatLayout(scheduleId));
        model.addAttribute("bookingRequest",
                new BookingRequest());

        return "seat-selection"; // → templates/seat-selection.html
    }

    // ─────────────────────────────────────────
    // POST /booking/book — Process booking form
    // ─────────────────────────────────────────
    @PostMapping("/book")
    public String bookTicket(
            @ModelAttribute BookingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttrs) {

        try {
            BookingResponse response = bookingService.bookTicket(
                    request, userDetails.getId());

            // Store booking result to show on confirm page
            redirectAttrs.addFlashAttribute("booking", response);
            return "redirect:/booking/confirm";

        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/select?scheduleId="
                    + request.getScheduleId();
        }
    }

    // ─────────────────────────────────────────
    // GET /booking/confirm — Show confirmation
    // ─────────────────────────────────────────
    @GetMapping("/confirm")
    public String confirmPage() {
        return "booking-confirm"; // → templates/booking-confirm.html
    }

    // ─────────────────────────────────────────
    // POST /booking/cancel — Cancel a booking
    // ─────────────────────────────────────────
    @PostMapping("/cancel")
    public String cancelBooking(
            @RequestParam String pnr,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttrs) {

        try {
            bookingService.cancelBooking(pnr, userDetails.getId());
            redirectAttrs.addFlashAttribute("message",
                    "Booking cancelled successfully! Queue updated.");

        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/bookings";
    }

    // ─────────────────────────────────────────
    // GET /booking/status?pnr=PNR123
    // Check PNR status (works without login)
    // ─────────────────────────────────────────
    @GetMapping("/status")
    public String pnrStatus(
            @RequestParam String pnr,
            Model model) {

        try {
            model.addAttribute("booking",
                    bookingService.getBookingByPnr(pnr));
        } catch (RuntimeException e) {
            model.addAttribute("error",
                    "PNR not found: " + pnr);
        }

        return "pnr-status"; // → templates/pnr-status.html
    }
}