package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.RoleResponse;
import com.example.project_pulse_backend.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    RoleResponse toRoleResponse(Role role);
}
