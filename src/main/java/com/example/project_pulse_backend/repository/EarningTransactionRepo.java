package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.entity.EarningTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EarningTransactionRepo extends JpaRepository<EarningTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT earning FROM EarningTransaction earning WHERE earning.id = :id")
    Optional<EarningTransaction> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT earning
            FROM EarningTransaction earning
            WHERE earning.task.phase.project.id = :projectId
              AND (:phaseId IS NULL OR earning.task.phase.id = :phaseId)
              AND (:earningTransactionType IS NULL
                   OR earning.earningTransactionType = :earningTransactionType)
              AND (
                    :keyword IS NULL
                    OR LOWER(earning.task.taskName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(earning.executor.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY earning.id ASC
            """)
    List<EarningTransaction> filterEarningTransaction(
            @Param("projectId") Long projectId,
            @Param("phaseId") Long phaseId,
            @Param("earningTransactionType") EarningTransactionType earningTransactionType,
            @Param("keyword") String keyword);

    List<EarningTransaction> findByReferenceTransaction_IdAndEarningTransactionType(
            Long referenceTransactionId,
            EarningTransactionType earningTransactionType);

    // lấy tổng earn 1 phase
    @Query("""
            SELECT COALESCE(SUM(earning.points), 0)
            FROM EarningTransaction earning
            WHERE earning.task.phase.id = :phaseId
              AND earning.earningTransactionType = :earnType
            """)
    BigDecimal sumNetEarnedPointsByPhaseId(
            @Param("phaseId") Long phaseId,
            @Param("earnType") EarningTransactionType earnType);

    // lấy tổng earn 1 project
    @Query("""
            SELECT COALESCE(SUM(earning.points), 0)
            FROM EarningTransaction earning
            WHERE earning.task.phase.project.id = :projectId
              AND earning.earningTransactionType = :earnType
            """)
    BigDecimal sumNetEarnedPointsByProjectId(
            @Param("projectId") Long projectId,
            @Param("earnType") EarningTransactionType earnType);

    // lấy tổng earn 1 user nhận được
    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN earning.earningTransactionType = :earnType THEN earning.points
                    WHEN earning.earningTransactionType = :transferType
                         AND earning.executor.user.id = :userId THEN earning.points
                    WHEN earning.earningTransactionType = :transferType
                         AND earning.referenceTransaction.executor.user.id = :userId THEN -earning.points
                    ELSE 0
                END
            ), 0)
            FROM EarningTransaction earning
            LEFT JOIN earning.referenceTransaction ref
            LEFT JOIN ref.executor sourceExecutor
            WHERE earning.executor.user.id = :userId
               OR (earning.earningTransactionType = :transferType AND sourceExecutor.user.id = :userId)
            """)
    BigDecimal sumNetEarnedPointsByUserId(
            @Param("userId") Long userId,
            @Param("earnType") EarningTransactionType earnType,
            @Param("transferType") EarningTransactionType transferType);

    // lấy tổng earn 1 user nhận được trong 1 project
    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN earning.earningTransactionType = :earnType THEN earning.points
                    WHEN earning.earningTransactionType = :transferType
                         AND earning.executor.user.id = :userId THEN earning.points
                    WHEN earning.earningTransactionType = :transferType
                         AND earning.referenceTransaction.executor.user.id = :userId THEN -earning.points
                    ELSE 0
                END
            ), 0)
            FROM EarningTransaction earning
            LEFT JOIN earning.referenceTransaction ref
            LEFT JOIN ref.executor sourceExecutor
            WHERE earning.task.phase.project.id = :projectId
              AND (earning.executor.user.id = :userId
                   OR (earning.earningTransactionType = :transferType AND sourceExecutor.user.id = :userId))
            """)
    BigDecimal sumNetEarnedPointsByUserIdAndProjectId(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("earnType") EarningTransactionType earnType,
            @Param("transferType") EarningTransactionType transferType);

    List<EarningTransaction> findByExecutor_Id(Long executorId);

    List<EarningTransaction> findByTask_Id(Long taskId);

    @Query("""
            SELECT earning.executor.user.id, COALESCE(SUM(earning.points), 0)
            FROM EarningTransaction earning
            WHERE earning.task.phase.project.id = :projectId
              AND earning.earningTransactionType = :earnType
            GROUP BY earning.executor.user.id
            """)
    List<Object[]> sumEarnedPointsGroupedByEmployee(
            @Param("projectId") Long projectId,
            @Param("earnType") EarningTransactionType earnType);
}
