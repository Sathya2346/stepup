package com.stepup.repository;

import com.stepup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.email = :identifier OR u.mobileNumber = :identifier")
    Optional<User> findByIdentifier(@org.springframework.data.repository.query.Param("identifier") String identifier);
}
