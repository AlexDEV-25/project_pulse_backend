package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.EarningTransaction;
import com.example.project_pulse_backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepo extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);

    List<Permission> findByDescriptionContainingIgnoreCase(String keyword);
}
