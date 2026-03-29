package com.trainreservation.config;

import com.trainreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication
        .configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web
        .builders.HttpSecurity;
import org.springframework.security.config.annotation.web
        .configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration          // Tells Spring → this class has config/settings
@EnableWebSecurity      // Activates Spring Security
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepo;

    // ─────────────────────────────────────────
    // Define which pages are public vs protected
    // ─────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        // ✅ These pages anyone can visit (no login needed)
                        .requestMatchers(
                                "/",            // Home page
                                "/search",      // Search trains
                                "/register",    // Registration page
                                "/login",       // Login page
                                "/css/**",      // CSS files
                                "/js/**",       // JS files
                                "/images/**"    // Images
                        ).permitAll()

                        // 🔐 Everything else needs login
                        .anyRequest().authenticated()
                )

                // Configure the login page
                .formLogin(form -> form
                        .loginPage("/login")            // Our custom login page
                        .loginProcessingUrl("/login")   // Spring handles this POST
                        .defaultSuccessUrl("/", true)   // After login → home
                        .failureUrl("/login?error=true")// Wrong password → back to login
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    // ─────────────────────────────────────────
    // Tell Spring Security HOW to load a user
    // Spring calls this when someone tries to login
    // ─────────────────────────────────────────

    // Replace the userDetailsService() method in SecurityConfig.java with this:
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            var user = userRepo.findByEmail(email)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "User not found: " + email));

            // Now returns CustomUserDetails instead
            return new CustomUserDetails(user);
        };
    }
    // ─────────────────────────────────────────
    // BCrypt password encoder
    // Encrypts passwords before storing in DB
    // This @Bean fixes the UserService error from Step 8!
    // ─────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────
    // Authentication Manager
    // Used internally by Spring Security
    // ─────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}