package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatisticsSummaryResponse {
    private long totalProject;
    private BigDecimal totalEarnedPoint;
    private long totalAssignedTask;
    private long totalCompletedTask;
}
