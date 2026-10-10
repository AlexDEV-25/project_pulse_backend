package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAssignmentRepo extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByTask_Id(Long taskId);
}
