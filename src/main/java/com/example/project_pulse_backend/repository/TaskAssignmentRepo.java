package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAssignmentRepo extends JpaRepository<TaskAssignment, Long> {
    Optional<TaskAssignment> findByIdAndTask_Project_Pm_Id(Long id, Long pmId);
}
