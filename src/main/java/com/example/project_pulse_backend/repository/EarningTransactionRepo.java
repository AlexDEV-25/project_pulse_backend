package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.EarningTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface EarningTransactionRepo extends JpaRepository<EarningTransaction, Long> {
    List<EarningTransaction> findByTask_Project_IdOrderByIdAsc(Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT earning FROM EarningTransaction earning WHERE earning.id = :id")
    Optional<EarningTransaction> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EarningTransaction> findByReferenceTransaction_IdAndEarningTransactionType(
            Long referenceTransactionId,
            EarningTransactionType earningTransactionType);

    boolean existsByReferenceTransaction_IdAndEarningTransactionType(
            Long referenceTransactionId,
            EarningTransactionType earningTransactionType);

    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN earning.earningTransactionType = :earnType THEN earning.points
                    WHEN earning.earningTransactionType = :reversalType THEN -earning.points
                    ELSE 0
                END
            ), 0)
            FROM EarningTransaction earning
            WHERE earning.task.phase.id = :phaseId
            """)
    BigDecimal sumNetEarnedPointsByPhaseId(
            @Param("phaseId") Long phaseId,
            @Param("earnType") EarningTransactionType earnType,
            @Param("reversalType") EarningTransactionType reversalType);

    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN earning.earningTransactionType = :earnType THEN earning.points
                    WHEN earning.earningTransactionType = :reversalType THEN -earning.points
                    ELSE 0
                END
            ), 0)
            FROM EarningTransaction earning
            WHERE earning.task.project.id = :projectId
            """)
    BigDecimal sumNetEarnedPointsByProjectId(
            @Param("projectId") Long projectId,
            @Param("earnType") EarningTransactionType earnType,
            @Param("reversalType") EarningTransactionType reversalType);
}
