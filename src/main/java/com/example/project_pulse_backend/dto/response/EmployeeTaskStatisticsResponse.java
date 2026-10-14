package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTaskStatisticsResponse {
    private String projectName;
    private String taskName;
    private LocalDate endAt;
    private BigDecimal taskPoint;
    private int progress;
}
