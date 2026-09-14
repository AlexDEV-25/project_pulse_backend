package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhaseRepo extends JpaRepository<Phase, Long> {
    List<Phase> findByProject_Id(Long projectId);

    Optional<Phase> findByIdAndProject_Id(Long id, Long projectId);

    List<Phase> findByProject_IdAndProject_Pm_Id(Long projectId, Long pmId);

    Optional<Phase> findByIdAndProject_Pm_Id(Long id, Long pmId);

    Optional<Phase> findByIdAndProject_IdAndProject_Pm_Id(Long id, Long projectId, Long pmId);
}
