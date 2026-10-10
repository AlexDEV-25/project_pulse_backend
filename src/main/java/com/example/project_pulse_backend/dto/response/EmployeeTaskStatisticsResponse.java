package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTaskStatisticsResponse {
    private String projectName;
    private String taskName;
    private LocalDateTime endAt;
    private BigDecimal taskPoint;
    private int progress;
}
