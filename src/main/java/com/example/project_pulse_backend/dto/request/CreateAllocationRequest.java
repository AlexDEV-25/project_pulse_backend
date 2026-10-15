package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAllocationRequest {

    @NotNull(message = "ALLOCATION_PHASE_ID_REQUIRED")
    private Long phaseId;

    @NotNull(message = "ALLOCATION_MEMBER_ID_REQUIRED")
    private Long memberId;

    @NotNull(message = "ALLOCATION_PERCENTAGE_REQUIRED")
    @Min(value = 1, message = "ALLOCATION_PERCENTAGE_MIN_INVALID")
    @Max(value = 100, message = "ALLOCATION_PERCENTAGE_MAX_INVALID")
    private Integer allocationPercentage;
}