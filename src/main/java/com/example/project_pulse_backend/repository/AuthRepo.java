package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepo extends JpaRepository<Auth, Long> {
    boolean existsByEmail(String email);

    Optional<Auth> findByEmail(String email);
}
