package com.trainreservation.repository;

import com.trainreservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<User, Long> means:
// → This repository is for the User entity
// → The primary key type is Long (our id field)
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring automatically generates SQL for this method name!
    // SELECT * FROM users WHERE email = ? LIMIT 1
    Optional<User> findByEmail(String email);

    // SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    // FROM users WHERE email = ?
    boolean existsByEmail(String email);
}