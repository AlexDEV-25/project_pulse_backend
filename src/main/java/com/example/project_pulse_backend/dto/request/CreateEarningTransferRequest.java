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
    @NotNull(message = "ID giao dịch earn gốc không được để trống")
    private Long sourceTransactionId;

    @NotNull(message = "ID nhân viên nhận không được để trống")
    private Long targetEmployeeId;

    @NotNull(message = "Số earn chuyển không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số earn chuyển phải lớn hơn 0")
    private BigDecimal points;

    private String reason;
}
