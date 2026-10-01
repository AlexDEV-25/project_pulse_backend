package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.PhaseStatus;
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
        Optional<Allocation> findByIdAndProject_Pm_Id(Long id, Long pmId);

        @Query("""
                                SELECT a FROM Allocation a
                                JOIN FETCH a.project
                                JOIN FETCH a.phase p
                                JOIN FETCH a.member m
                                JOIN FETCH m.user
                                WHERE m.user.id = :userId
                                ORDER BY p.startAt DESC
                        """)
        List<Allocation> findAllForEmployee(@Param("userId") Long userId);

        boolean existsByPhase_IdAndMember_Id(Long phaseId, Long memberId);

        // Lấy tất cả phân bổ của nhân viên (qua user_id) giao với khoảng thời gian
        // [startAt, endAt]
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

        List<Allocation> findByProject_Id(Long projectId);

        @Query("SELECT a FROM Allocation a JOIN FETCH a.phase WHERE a.project.id = :projectId")
        List<Allocation> findWithPhaseByProjectId(@Param("projectId") Long projectId);

        List<Allocation> findByPhase_Id(Long phaseId);

        @Query("""
                            SELECT a FROM Allocation a
                            JOIN a.phase p
                            WHERE a.project.id = :projectId
                              AND a.member.id = :memberId
                              AND p.status IN :phaseStatuses
                              AND p.startAt < :nextDayStart
                              AND p.endAt >= :dayStart
                        """)
        List<Allocation> findForMemberAndDateInActivePhase(
                        @Param("projectId") Long projectId,
                        @Param("memberId") Long memberId,
                        @Param("phaseStatuses") List<PhaseStatus> phaseStatuses,
                        @Param("dayStart") LocalDateTime dayStart,
                        @Param("nextDayStart") LocalDateTime nextDayStart);
}
