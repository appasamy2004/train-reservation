package com.trainreservation.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// Holds data from the registration form
// We never expose the User entity directly to the form
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")  // validates email format
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;
}