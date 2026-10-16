package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);

    List<ProjectMember> findByProject_Id(Long projectId);

    List<ProjectMember> findByUser_Id(Long userId);

    @Query("""
            SELECT pm
            FROM ProjectMember pm
            WHERE pm.user.id = :userId
              AND (:status IS NULL OR pm.project.projectStatus = :status)
              AND (
                  :keyword IS NULL
                  OR LOWER(pm.project.projectName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<ProjectMember> searchByUserIdAndProjectNameAndProjectStatus(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status
    );

    @Query("""
            SELECT pm
            FROM ProjectMember pm
            WHERE pm.project.id = :projectId
              AND (
                  :keyword IS NULL OR LOWER(pm.user.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<ProjectMember> filterProjectMember(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword
    );
}
