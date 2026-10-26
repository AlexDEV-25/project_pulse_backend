package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.entity.Auth;
import com.example.project_pulse_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    User toUser(CreateAccountRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    void updateUser(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(target = "authId", source = "auth.id")
    @Mapping(target = "email", source = "auth.email")
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "position", source = "user.position")
    @Mapping(target = "resourceRate", source = "user.resourceRate")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "departmentId", source = "user.department.id")
    @Mapping(target = "departmentName", source = "user.department.departmentName")
    UserResponse toUserResponse(User user, Auth auth);

    @Mapping(target = "authId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "email", source = "auth.email")
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "position", source = "user.position")
    @Mapping(target = "resourceRate", source = "user.resourceRate")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "departmentId", source = "user.department.id")
    @Mapping(target = "departmentName", source = "user.department.departmentName")
    UserResponse toUserResponseForPm(User user, Auth auth);
}
