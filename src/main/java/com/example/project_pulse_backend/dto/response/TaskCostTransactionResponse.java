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
public class TaskCostTransactionResponse {
    private Long id;
    private Long projectId;
    private Long phaseId;
    private String phaseName;
    private Long taskId;
    private String taskName;
    private Long employeeId;
    private String employeeName;
    private BigDecimal points;
    private BigDecimal workdays;
    private String reason;
}
