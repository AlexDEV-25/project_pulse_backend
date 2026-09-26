package com.example.project_pulse_backend.repository;

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

    boolean existsByProject_IdAndUser_IdAndIdNot(Long projectId, Long userId, Long id);

    Optional<ProjectMember> findByIdAndProject_Pm_Id(Long id, Long pmId);

    // Lock toàn bộ ProjectMember có user_id tương ứng để tránh race condition giữa 2 request
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<ProjectMember> findAllByUserIdWithLock(@Param("userId") Long userId);

    Optional<ProjectMember> findByIdAndProject_Id(Long id, Long projectId);
}
