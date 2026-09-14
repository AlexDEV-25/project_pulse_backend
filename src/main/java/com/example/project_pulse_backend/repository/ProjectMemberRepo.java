package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    boolean existsByProject_IdAndUser_IdAndIdNot(Long projectId, Long userId, Long id);

    Optional<ProjectMember> findByIdAndProject_Pm_Id(Long id, Long pmId);
}
