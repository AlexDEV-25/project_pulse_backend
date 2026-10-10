package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepo extends JpaRepository<Allocation, Long> {

    boolean existsByPhase_IdAndMember_Id(Long phaseId, Long memberId);

    // Lấy tất cả phân bổ của nhân viên (qua user_id) giao với khoảng thời gian [startAt, endAt]
    @Query("""
                SELECT a FROM Allocation a
                JOIN a.phase p
                JOIN a.member pm
                WHERE pm.user.id = :userId
                  AND p.startAt < :endAt
                  AND p.endAt > :startAt
            """)
    List<Allocation> findOverlappingAllocationsByUserId(
            @Param("userId") Long userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt);

    List<Allocation> findByPhase_Id(Long phaseId);

    @Query("""
            SELECT a
            FROM Allocation a
            WHERE a.phase.project.id = :projectId
              AND (:phaseId IS NULL OR a.phase.id = :phaseId)
              AND (:userId IS NULL OR a.member.user.id = :userId)
              AND (
                  :keyword IS NULL
                  OR LOWER(a.member.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<Allocation> findByProjectId(
            @Param("projectId") Long projectId,
            @Param("phaseId") Long phaseId,
            @Param("keyword") String keyword,
            @Param("userId") Long userId
    );

    Optional<Allocation> findByPhase_IdAndMember_Id(Long phaseId, Long memberId);
    
}
