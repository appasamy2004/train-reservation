package com.trainreservation.controller;

import com.trainreservation.config.CustomUserDetails;
import com.trainreservation.dto.UserRegistrationDTO;
import com.trainreservation.service.BookingService;
import com.trainreservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation
        .AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support
        .RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService    userService;
    private final BookingService bookingService;

    // ─────────────────────────────────────────
    // GET /login — Show login page
    // ─────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // → templates/login.html
    }

    // ─────────────────────────────────────────
    // GET /register — Show registration form
    // ─────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        // Pass empty DTO to the form
        model.addAttribute("user", new UserRegistrationDTO());
        return "register"; // → templates/register.html
    }

    // ─────────────────────────────────────────
    // POST /register — Process registration form
    // ─────────────────────────────────────────
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") UserRegistrationDTO dto,
            RedirectAttributes redirectAttrs) {

        try {
            userService.registerUser(dto);
            // Success → redirect to login with success message
            redirectAttrs.addFlashAttribute("success",
                    "Registration successful! Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            // Failure → back to register with error message
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // ─────────────────────────────────────────
    // GET /user/bookings — Show my bookings
    // @AuthenticationPrincipal injects logged-in user
    // ─────────────────────────────────────────
    @GetMapping("/user/bookings")
    public String myBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute("bookings",
                bookingService.getUserBookings(userDetails.getId()));
        return "my-bookings"; // → templates/my-bookings.html
    }
}