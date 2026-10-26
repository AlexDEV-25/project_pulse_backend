package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.mapper.UserMapper;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHelper {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final DepartmentRepo departmentRepo;
    private final UserMapper userMapper;

    public User createUser(CreateAccountRequest request) {

        List<Role> roles = findRoles(request.getRoles());
        Department department = departmentRepo.findById(request.getDepartmentId())
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());

        User newUser = userMapper.toUser(request);
        newUser.setRoles(roles);
        newUser.setDepartment(department);
        return userRepo.save(newUser);
    }

    public List<Role> findRoles(List<String> roleNames) {
        List<String> requestedRoleNames = roleNames.stream().distinct().toList();
        List<Role> roles = roleRepo.findAllById(requestedRoleNames);
        if (roles.size() != requestedRoleNames.size()) {
            throw AppException.builder().appError(AppError.ROLE_NOT_FOUND).build();
        }
        return roles;
    }

    public void checkDepartmentEmpty(Long departmentId) {
        if (userRepo.existsByDepartment_Id(departmentId)) {
            throw AppException.builder().appError(AppError.DEPARTMENT_NOT_EMPTY).build();
        }
    }

    public long getUserCountByDepartment(Long departmentId) {
        return userRepo.countByDepartment_Id(departmentId);
    }
}
