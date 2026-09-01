package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.dto.response.CreateAccountResponse;
import com.example.project_pulse_backend.entity.Authentication;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AuthenticationRepo;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationRepo authenticationRepo;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final PasswordEncoder passwordEncoder;
//    private final GetUserByToken getUserByToken;
    
    public CreateAccountResponse createAccount(CreateAccountRequest request) {

        if (authenticationRepo.existsByEmail(request.getEmail())) {
            throw AppException.builder().appError(AppError.EMAIL_ALREADY_EXISTS).build();
        }

        List<Role> roles = roleRepo.findAllById(request.getRoles());
        Department department = departmentRepo.findById(request.getDepartmentId()).orElseThrow(() -> new RuntimeException("Department not found"));

        User user = User.builder()
                .userName(request.getUserName())
                .roles(roles)
                .position(request.getPosition())
                .resourceRate(request.getResourceRate())
                .department(department)
                .hidden(false)
                .build();
        User newUser = userRepo.save(user);

        Authentication authentication = Authentication.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .user(newUser)
                .hidden(false)
                .build();
        Authentication newAuth = authenticationRepo.save(authentication);

        return CreateAccountResponse.builder()
                .email(newAuth.getEmail())
                .userName(newAuth.getUser().getUserName())
                .position(newAuth.getUser().getPosition())
                .resourceRate(newAuth.getUser().getResourceRate())
                .build();
    }

}

