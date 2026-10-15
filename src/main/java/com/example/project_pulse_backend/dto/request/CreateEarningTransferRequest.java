package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEarningTransferRequest {
    @NotNull(message = "SOURCE_TRANSACTION_ID_REQUIRED")
    private Long sourceTransactionId;

    @NotNull(message = "TARGET_EMPLOYEE_ID_REQUIRED")
    private Long targetEmployeeId;

    @NotNull(message = "EARNING_POINTS_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "EARNING_POINTS_MUST_BE_POSITIVE")
    private BigDecimal points;

    private String reason;
}
