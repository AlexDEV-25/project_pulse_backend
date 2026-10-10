package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.TaskCostTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TaskCostTransactionRepo extends JpaRepository<TaskCostTransaction, Long> {
    @Query("""
            SELECT COALESCE(SUM(cost.points), 0)
            FROM TaskCostTransaction cost
            WHERE cost.task.phase.id = :phaseId
            """)
    BigDecimal sumCostPointsByPhaseId(@Param("phaseId") Long phaseId);

    @Query("""
            SELECT COALESCE(SUM(cost.points), 0)
            FROM TaskCostTransaction cost
            WHERE cost.task.phase.project.id = :projectId
            """)
    BigDecimal sumCostPointsByProjectId(@Param("projectId") Long projectId);

    @Query("""
            SELECT costTransaction
            FROM TaskCostTransaction costTransaction
            WHERE costTransaction.task.phase.project.id = :projectId
              AND (:phaseId IS NULL OR costTransaction.task.phase.id = :phaseId)
              AND (
                    :keyword IS NULL
                    OR LOWER(costTransaction.task.taskName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(costTransaction.executor.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY costTransaction.id ASC
            """)
    List<TaskCostTransaction> findByProjectIdAndFilters(
            @Param("projectId") Long projectId,
            @Param("phaseId") Long phaseId,
            @Param("keyword") String keyword);
}
