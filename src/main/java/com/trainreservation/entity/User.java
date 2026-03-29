package com.trainreservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity                          // Tells Hibernate → create a table for this
@Table(name = "users")           // Table name in MySQL will be "users"
@Data                            // Lombok → auto generates getters & setters
@NoArgsConstructor               // Lombok → generates empty constructor
@AllArgsConstructor              // Lombok → generates constructor with all fields
@Builder                         // Lombok → lets us do User.builder().name("x").build()
public class User {

    @Id                                           // This is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto increment (1,2,3...)
    private Long id;

    @Column(nullable = false, length = 100)       // NOT NULL in MySQL
    private String name;

    @Column(nullable = false, unique = true, length = 150) // UNIQUE email
    private String email;

    @Column(nullable = false)
    private String password;                      // Will be stored encrypted

    private String phone;

    @Enumerated(EnumType.STRING)                  // Store as "USER" or "ADMIN" text
    @Column(length = 10)
    private Role role = Role.USER;                // Default role is USER

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Inner Enum — Role belongs to User so we define it here
    public enum Role {
        USER, ADMIN
    }
}