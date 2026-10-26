package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.CreateAccountResponse;
import com.example.project_pulse_backend.dto.request.CreateAccountRequest;
import com.example.project_pulse_backend.entity.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "forgotPasswordCode", ignore = true)
    @Mapping(target = "forgotPasswordCodeExpiresAt", ignore = true)
    Auth toAuth(CreateAccountRequest request);

    @Mapping(target = "userName", source = "user.userName")
    @Mapping(target = "position", source = "user.position")
    @Mapping(target = "resourceRate", source = "user.resourceRate")
    CreateAccountResponse toCreateAccountResponse(Auth auth);
}
