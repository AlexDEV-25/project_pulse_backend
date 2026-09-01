package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.TaskCostTransaction;
import com.example.project_pulse_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
}
