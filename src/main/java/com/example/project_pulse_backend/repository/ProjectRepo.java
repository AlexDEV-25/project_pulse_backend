package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {
    List<Project> findByPm_Id(Long pmId);

    Optional<Project> findByIdAndPm_Id(Long id, Long pmId);
}
