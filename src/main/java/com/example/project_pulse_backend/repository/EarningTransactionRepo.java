package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.EarningTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EarningTransactionRepo extends JpaRepository<EarningTransaction, Long> {
}
