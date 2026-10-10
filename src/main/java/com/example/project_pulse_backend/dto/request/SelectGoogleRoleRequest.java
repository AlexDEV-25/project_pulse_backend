package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectGoogleRoleRequest {

    @NotBlank(message = "code không được để trống")
    private String code;

    @NotBlank(message = "Role không được để trống")
    private String role;
}
