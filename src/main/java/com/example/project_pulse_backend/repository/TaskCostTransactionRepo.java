package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.TaskCostTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.math.BigDecimal;

public interface TaskCostTransactionRepo extends JpaRepository<TaskCostTransaction, Long> {
    List<TaskCostTransaction> findByTask_IdAndTask_Project_IdOrderByIdAsc(Long taskId, Long projectId);

    List<TaskCostTransaction> findByTask_Project_IdAndExecutor_User_IdOrderByIdAsc(
            Long projectId, Long employeeId);

    @Query("""
            SELECT COALESCE(SUM(cost.points), 0)
            FROM TaskCostTransaction cost
            WHERE cost.task.phase.id = :phaseId
            """)
    BigDecimal sumCostPointsByPhaseId(@Param("phaseId") Long phaseId);

    @Query("""
            SELECT COALESCE(SUM(cost.points), 0)
            FROM TaskCostTransaction cost
            WHERE cost.task.project.id = :projectId
            """)
    BigDecimal sumCostPointsByProjectId(@Param("projectId") Long projectId);
}
