package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Allocation;
import com.example.project_pulse_backend.entity.Authentication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepo extends JpaRepository<Authentication, Long> {
    boolean existsByEmail(String email);

    Optional<Authentication> findByEmail(String email);
}
