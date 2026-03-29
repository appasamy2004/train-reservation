package com.trainreservation.config;

import com.trainreservation.entity.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

// This wraps our User entity so Spring Security can use it
// AND we can access user.getId() in controllers
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long   id;      // Our DB user id
    private final String email;
    private final String password;
    private final String role;

    // Convert our User entity → CustomUserDetails
    public CustomUserDetails(User user) {
        this.id       = user.getId();
        this.email    = user.getEmail();
        this.password = user.getPassword();
        this.role     = user.getRole().name();
    }

    @Override
    public Collection<? extends org.springframework.security
            .core.GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword()             { return password; }
    @Override public String getUsername()             { return email; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}