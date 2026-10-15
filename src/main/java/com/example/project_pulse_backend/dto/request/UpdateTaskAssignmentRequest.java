package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskAssignmentRequest {
    @NotNull(message = "CONTRIBUTION_PERCENTAGE_REQUIRED")
    @Min(value = 0, message = "CONTRIBUTION_PERCENTAGE_INVALID")
    @Max(value = 100, message = "CONTRIBUTION_PERCENTAGE_INVALID")
    private Integer contributionPercentage;
}
