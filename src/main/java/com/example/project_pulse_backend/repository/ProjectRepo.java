package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepo extends JpaRepository<Project, Long> {
    List<Project> findByPm_Id(Long pmId);

    boolean existsByIdAndPm_Id(Long id, Long pmId);

    @Query("""
            SELECT project FROM Project project
            WHERE (:pmId IS NULL OR project.pm.id = :pmId)
              AND (:status IS NULL OR project.projectStatus = :status)
              AND (:keyword IS NULL
                    OR LOWER(project.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<Project> searchProjects(
            @Param("pmId") Long pmId,
            @Param("status") ProjectStatus status,
            @Param("keyword") String keyword);
}
