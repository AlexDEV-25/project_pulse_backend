package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Phase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhaseRepo extends JpaRepository<Phase, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT phase FROM Phase phase WHERE phase.id = :id")
    Optional<Phase> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT phase FROM Phase phase
            WHERE phase.project.id = :projectId
              AND (:keyword IS NULL
                    OR LOWER(phase.phaseName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Phase> searchByProjectIdAndKeyword(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword);

}
