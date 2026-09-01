package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhaseRepo extends JpaRepository<Phase, Long> {
}
