package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Phase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhaseRepo extends JpaRepository<Phase, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT phase FROM Phase phase WHERE phase.id = :id")
    Optional<Phase> findByIdForUpdate(@Param("id") Long id);

    List<Phase> findByProject_Id(Long projectId);

    Optional<Phase> findByIdAndProject_Id(Long id, Long projectId);

    List<Phase> findByProject_IdAndProject_Pm_Id(Long projectId, Long pmId);

    Optional<Phase> findByIdAndProject_Pm_Id(Long id, Long pmId);

    Optional<Phase> findByIdAndProject_IdAndProject_Pm_Id(Long id, Long projectId, Long pmId);
}
