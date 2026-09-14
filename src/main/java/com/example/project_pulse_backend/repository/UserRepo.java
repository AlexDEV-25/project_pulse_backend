package com.example.project_pulse_backend.repository;

import com.example.project_pulse_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {
    List<User> findByDepartment_Id(Long departmentId);
}
