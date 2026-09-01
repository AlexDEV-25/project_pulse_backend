package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.Permission;
import com.example.project_pulse_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
