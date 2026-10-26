package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.dto.response.UserStatisticsResponse;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.FileManager;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.RoleHelper;
import com.example.project_pulse_backend.helper.UserHelper;
import com.example.project_pulse_backend.mapper.UserMapper;
import com.example.project_pulse_backend.repository.AuthRepo;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final AuthRepo authRepo;
    private final DepartmentRepo departmentRepo;
    private final GetUserByToken getUserByToken;
    private final FileManager fileStorage;
    private final UserHelper userHelper;
    private final UserMapper userMapper;

    @PreAuthorize("hasAuthority('GET_USERS')")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(
            List<String> roleNames, List<Long> departmentIds, String keyword) {
        User currentUser = getUserByToken.get();
        boolean isAdmin = RoleHelper.checkIsAdmin(currentUser);
        if (!isAdmin && !RoleHelper.checkIsPm(currentUser)) {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }

        List<String> selectedRoles = roleNames == null ? List.of() : roleNames;
        List<Long> selectedDepartments = departmentIds == null ? List.of() : departmentIds;
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return userRepo.findUsers(
                        normalizedKeyword,
                        !selectedRoles.isEmpty(),
                        selectedRoles.isEmpty() ? List.of("") : selectedRoles,
                        !selectedDepartments.isEmpty(),
                        selectedDepartments.isEmpty() ? List.of(-1L) : selectedDepartments)
                .stream().map(isAdmin ? this::toResponse : this::toResponseForPm).toList();
    }

    @PreAuthorize("hasAuthority('GET_STATISTICS_USERS')")
    @Transactional(readOnly = true)
    public UserStatisticsResponse getUserStatistics() {
        Map<String, Long> usersByRole = new LinkedHashMap<>();

        usersByRole.put("EMPLOYEE", userRepo.countUsersByRoleName("EMPLOYEE"));
        usersByRole.put("PM", userRepo.countUsersByRoleName("PM"));
        usersByRole.put("ADMIN", userRepo.countUsersByRoleName("ADMIN"));

        return UserStatisticsResponse.builder()
                .totalUsers(userRepo.count())
                .usersByRole(usersByRole)
                .build();
    }

    @PreAuthorize("hasAuthority('GET_USER_BY_ID')")
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User currentUser = getUserByToken.get();
        User target = userRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());
        if (RoleHelper.checkIsAdmin(currentUser)) {
            return toResponse(target);
        } else if (RoleHelper.checkIsPm(currentUser)) {
            return toResponseForPm(target);
        } else {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }
    }

    @PreAuthorize("hasAuthority('GET_MY_INFO')")
    @Transactional(readOnly = true)
    public UserResponse getMyInfo() {
        User info = getUserByToken.get();
        return toResponse(info);
    }

    @PreAuthorize("hasAuthority('UPDATE_AVATAR')")
    @Transactional
    public UserResponse updateAvatar(MultipartFile avt) {
        User entity = getUserByToken.get();
        if (avt == null || avt.isEmpty()) {
            return toResponse(entity);
        }

        String oldAvatarUrl = entity.getAvatarUrl();
        String newAvatarUrl;
        try {
            Map<?, ?> uploadedAvatar = fileStorage.uploadImage(avt);
            Object secureUrl = uploadedAvatar.get("secure_url");
            if (!(secureUrl instanceof String url) || url.isBlank()) {
                throw AppException.builder().appError(AppError.UPDATE_PROFILE_FAILED).build();
            }
            newAvatarUrl = url;
        } catch (Exception e) {
            throw AppException.builder().appError(AppError.UPDATE_PROFILE_FAILED).build();
        }

        entity.setAvatarUrl(newAvatarUrl);
        User saved;
        try {
            saved = userRepo.save(entity);
        } catch (RuntimeException e) {
            try {
                fileStorage.deleteFile(newAvatarUrl);
            } catch (Exception cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }

        if (oldAvatarUrl != null) {
            try {
                fileStorage.deleteFile(oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Could not delete replaced avatar for user id {}", entity.getId(), e);
            }
        }
        return this.toResponse(saved);
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        User entity = userRepo.findById(request.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());
        List<Role> roles = userHelper.findRoles(request.getRoles());
        Department department = departmentRepo.findById(request.getDepartmentId())
                .orElseThrow(() -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());
        userMapper.updateUser(request, entity);
        entity.setRoles(roles);
        entity.setDepartment(department);

        User saved = userRepo.save(entity);
        return this.toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        var auth = authRepo.findByUser_Id(user.getId()).orElse(null);
        return userMapper.toUserResponse(user, auth);
    }

    private UserResponse toResponseForPm(User user) {
        var auth = authRepo.findByUser_Id(user.getId()).orElse(null);
        return userMapper.toUserResponseForPm(user, auth);
    }
}
