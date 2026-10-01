package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.PhaseEvmSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhaseEvmSnapshotRepo extends JpaRepository<PhaseEvmSnapshot, Long> {
    Optional<PhaseEvmSnapshot> findByPhase_Id(Long phaseId);
}
