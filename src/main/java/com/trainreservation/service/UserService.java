package com.trainreservation.service;

import com.trainreservation.dto.UserRegistrationDTO;
import com.trainreservation.entity.Booking;
import com.trainreservation.entity.User;
import com.trainreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder; // encrypts passwords

    // ─────────────────────────────────────────
    // Register a new user
    // ─────────────────────────────────────────
    public User registerUser(UserRegistrationDTO dto) {

        // Check if email already exists
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException(
                    "Email already registered: " + dto.getEmail());
        }

        // Build and save new User
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                // NEVER store plain text password!
                // BCrypt encrypts it: "mypassword" → "$2a$10$xyz..."
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(User.Role.USER)
                .build();

        return userRepo.save(user);
    }

    // ─────────────────────────────────────────
    // Get user by email (used by Spring Security)
    // ─────────────────────────────────────────
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + email));
    }
}
