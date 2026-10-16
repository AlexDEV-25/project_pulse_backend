package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {
    @NotBlank(message = "PROJECT_NAME_REQUIRED")
    @Size(min = 2, max = 100, message = "PROJECT_NAME_LENGTH_INVALID")
    private String projectName;

    @NotNull(message = "PM_ID_REQUIRED")
    private Long pmId;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startAt;

    @NotNull(message = "END_DATE_REQUIRED")
    private LocalDate endAt;

    @NotNull(message = "CLIENT_BUDGET_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "BUDGET_MUST_BE_POSITIVE")
    private BigDecimal clientBudget;

    @NotNull(message = "PROJECT_BUDGET_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "BUDGET_MUST_BE_POSITIVE")
    private BigDecimal projectBudget;

    private String description;

}
