package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAllocationRequest {
    @NotNull(message = "PROJECT_ID_REQUIRED")
    private Long projectId;

    @NotNull(message = "PHASE_ID_REQUIRED")
    private Long phaseId;

    @NotNull(message = "MEMBER_ID_REQUIRED")
    private Long memberId;

    @NotNull(message = "ALLOCATION_PERCENTAGE_REQUIRED")
    @Min(value = 1, message = "ALLOCATION_PERCENTAGE_MIN_1")
    @Max(value = 100, message = "ALLOCATION_PERCENTAGE_MAX_100")
    private Integer allocationPercentage;
}