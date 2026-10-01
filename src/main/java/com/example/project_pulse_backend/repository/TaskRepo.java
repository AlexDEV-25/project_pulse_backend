package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Task;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TaskRepo extends JpaRepository<Task, Long> {
    List<Task> findByProject_Id(Long projectId);

    List<Task> findByPhase_Id(Long phaseId);

    List<Task> findDistinctByProject_IdAndAssignments_Employee_Id(Long projectId, Long projectMemberId);

    Optional<Task> findByIdAndProject_Pm_Id(Long id, Long pmId);

    Optional<Task> findByIdAndProject_Id(Long id, Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Task> findForUpdateByIdAndProject_Pm_Id(Long id, Long pmId);

    List<Task> findDistinctByAssignments_Employee_User_Id(Long employeeId);

    Optional<Task> findByIdAndAssignments_Employee_User_Id(Long id, Long employeeId);
}
