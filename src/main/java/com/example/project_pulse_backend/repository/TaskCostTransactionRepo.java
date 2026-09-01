package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.TaskCostTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCostTransactionRepo extends JpaRepository<TaskCostTransaction, Long> {
}
