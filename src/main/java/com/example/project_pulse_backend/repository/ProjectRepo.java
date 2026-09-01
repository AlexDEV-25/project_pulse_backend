package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<Project, Long> {
}
