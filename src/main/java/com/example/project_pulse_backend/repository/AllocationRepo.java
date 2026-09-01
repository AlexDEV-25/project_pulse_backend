package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllocationRepo extends JpaRepository<Allocation, Long> {
}
