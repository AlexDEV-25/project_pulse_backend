package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {
    List<User> findByDepartment_Id(Long departmentId);

    long countByDepartment_Id(Long departmentId);

    @Query("""
            SELECT COUNT(DISTINCT u)
            FROM User u
            JOIN u.roles role
            WHERE role.name = :roleName
            """)
    long countUsersByRoleName(@Param("roleName") String roleName);

    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN u.roles role
            WHERE (:keyword IS NULL
                   OR LOWER(u.userName) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(u.position) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(u.department.departmentName) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR EXISTS (
                       SELECT auth.id
                       FROM Auth auth
                       WHERE auth.user = u
                         AND LOWER(auth.email) LIKE CONCAT('%', LOWER(:keyword), '%')
                   ))
              AND (:filterByRoles = false OR role.name IN :roleNames)
              AND (:filterByDepartments = false OR u.department.id IN :departmentIds)
            ORDER BY u.id
            """)
    List<User> findUsers(
            @Param("keyword") String keyword,
            @Param("filterByRoles") boolean filterByRoles,
            @Param("roleNames") List<String> roleNames,
            @Param("filterByDepartments") boolean filterByDepartments,
            @Param("departmentIds") List<Long> departmentIds);
}
