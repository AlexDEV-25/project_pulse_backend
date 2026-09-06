package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.dto.response.RoleResponse;
import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.dto.response.UserWithoutCostResponse;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.FileManager;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.RoleRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final DepartmentRepo departmentRepo;
    private final GetUserByToken getUserByToken;
    private final FileManager fileStorage;


    @PreAuthorize("hasAuthority('GET_ALL_USERS')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepo.findAll();
        return users.stream().map(this::mapToUserResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_ALL_USERS_FOR_PM')")
    public List<UserWithoutCostResponse> getAllUsersForPM() {
        List<User> users = userRepo.findAll();
        return users.stream().map(this::mapToUserWithoutCostResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_MY_INFO')")
    public UserResponse getMyInfo() {
        User info = getUserByToken.get();
        return mapToUserResponse(info);
    }


    @PreAuthorize("hasAuthority('UPDATE_AVATAR')")
    public UserResponse updateAvatar(MultipartFile avt) {
        User entity = getUserByToken.get();
        if (avt != null) {
            try {
                Map<?, ?> handleAvt = fileStorage.uploadImage(avt);
                String avatarUrl = (String) handleAvt.get("secure_url");

                entity.setAvatarUrl(avatarUrl);
            } catch (Exception e) {
                throw AppException.builder().appError(AppError.UPDATE_PROFILE_FAILED).build();
            }

        }
        User saved = userRepo.save(entity);
        return this.mapToUserResponse(saved);
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public UserResponse updateUser(UpdateUserRequest request) {
        User entity = userRepo.findById(request.getId()).orElseThrow(() -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        List<Role> roles = roleRepo.findAllById(request.getRoles());
        Department department = departmentRepo.findById(request.getDepartmentId())
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());
        entity.setUserName(request.getUserName());
        entity.setPosition(request.getPosition());
        entity.setResourceRate(request.getResourceRate());
        entity.setRoles(roles);
        entity.setDepartment(department);

        User saved = userRepo.save(entity);
        return this.mapToUserResponse(saved);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .position(user.getPosition())
                .resourceRate(user.getResourceRate())
                .roles(user.getRoles().stream().map(role -> RoleResponse.builder().name(role.getName()).build()).toList())
                .departmentName(user.getDepartment().getDepartmentName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private UserWithoutCostResponse mapToUserWithoutCostResponse(User user) {
        return UserWithoutCostResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .position(user.getPosition())
                .departmentName(user.getDepartment().getDepartmentName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

}
