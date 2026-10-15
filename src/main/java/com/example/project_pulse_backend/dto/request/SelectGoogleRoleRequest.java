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

    @NotBlank(message = "CODE_REQUIRED")
    private String code;

    @NotBlank(message = "ROLE_SELECTION_REQUIRED")
    private String role;
}
