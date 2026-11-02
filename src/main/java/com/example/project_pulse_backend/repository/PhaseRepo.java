package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.entity.Phase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PhaseRepo extends JpaRepository<Phase, Long> {
    @Query("""
            SELECT phase FROM Phase phase
            WHERE phase.project.id = :projectId
              AND (:keyword IS NULL
                    OR LOWER(phase.phaseName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Phase> filterPhase(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword);

    List<Phase> findByProject_Id(Long projectId);

    List<Phase> findByProject_IdOrderByStartAtAscIdAsc(Long projectId);

    boolean existsByProject_IdAndStatusNot(Long projectId, PhaseStatus status);

    @EntityGraph(attributePaths = "project")
    @Query("""
            SELECT phase
            FROM Phase phase
            WHERE phase.startAt <= :weekEnd
              AND phase.endAt >= :weekStart
              AND EXISTS (
                  SELECT allocation.id
                  FROM Allocation allocation
                  WHERE allocation.phase = phase
                    AND allocation.member.user.id = :userId
              )
            """)
    List<Phase> findAllocatedPhasesOverlappingWeek(
            @Param("userId") Long userId,
                    @Param("weekStart") LocalDate weekStart,
                    @Param("weekEnd") LocalDate weekEnd);
}
