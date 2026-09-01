package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Authentication;
import com.example.project_pulse_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
}
