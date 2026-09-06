package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentName(String departmentName);
}
