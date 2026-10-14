package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Task;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TaskRepo extends JpaRepository<Task, Long> {
    @Query("""
            SELECT task
            FROM Task task
            WHERE task.phase.project.id = :projectId
              AND (:phaseId IS NULL OR task.phase.id = :phaseId)
              AND (
                    :keyword IS NULL
                    OR LOWER(task.taskName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (
                    :userId IS NULL
                    OR EXISTS (
                        SELECT assignment.id
                        FROM TaskAssignment assignment
                        WHERE assignment.task = task
                          AND assignment.employee.user.id = :userId
                    )
              )
            """)
    List<Task> findByProjectIdAndFilters(
            @Param("projectId") Long projectId,
            @Param("phaseId") Long phaseId,
            @Param("keyword") String keyword,
            @Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(task.taskPoint), 0) FROM Task task WHERE task.phase.id = :phaseId")
    BigDecimal sumTaskPointsByPhaseId(@Param("phaseId") Long phaseId);

    List<Task> findByPhase_Id(Long phaseId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Task> findForUpdateById(Long id);

    @Query("""
            SELECT DISTINCT task
            FROM Task task
            JOIN FETCH task.assignments assignment
            JOIN FETCH task.phase phase
            JOIN FETCH phase.project
            WHERE assignment.employee.user.id = :employeeId
            """)
    List<Task> findAssignedTasksWithDetails(@Param("employeeId") Long employeeId);

}
