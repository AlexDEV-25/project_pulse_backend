package com.example.project_pulse_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OverloadedDateResponse {
    private LocalDate date;
    private Integer totalAllocationPercentage;
}
