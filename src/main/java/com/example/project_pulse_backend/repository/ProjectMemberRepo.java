package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.ProjectMember;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    // Lock toàn bộ ProjectMember có user_id tương ứng để tránh race condition giữa 2 request
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<ProjectMember> findAllByUserIdWithLock(@Param("userId") Long userId);

    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);

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
    List<ProjectMember> findByProjectId(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword
    );
}
