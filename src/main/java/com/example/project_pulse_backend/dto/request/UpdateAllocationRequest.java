package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAllocationRequest {
    @NotNull(message = "ALLOCATION_PERCENTAGE_REQUIRED")
    @Min(value = 1, message = "ALLOCATION_PERCENTAGE_MIN_1")
    @Max(value = 100, message = "ALLOCATION_PERCENTAGE_MAX_100")
    private Integer allocationPercentage;
}