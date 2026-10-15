package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectMemberRequest {
    @NotNull(message = "PROJECT_ID_REQUIRED")
    private Long projectId;

    @NotNull(message = "USER_ID_REQUIRED")
    private Long userId;
}
