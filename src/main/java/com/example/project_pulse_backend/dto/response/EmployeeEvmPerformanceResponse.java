package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEvmPerformanceResponse {
    private Long userId;
    private String userName;
    private String position;
    private String avatarUrl;
    private BigDecimal resourceRate;

    private BigDecimal totalWorkdays;
    private BigDecimal totalAc;
    private BigDecimal totalEv;
    private BigDecimal netValue; // totalEv - totalAc
    private BigDecimal roi;      // totalEv / totalAc
    private Long completedTasksCount;
    private String performanceCategory; // "STAR", "HEAVY_LIFTER", "BALANCED", "NEEDS_IMPROVEMENT"
}
