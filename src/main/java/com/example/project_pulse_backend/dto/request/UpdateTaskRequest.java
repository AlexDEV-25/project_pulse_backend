package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    @NotBlank(message = "TASK_NAME_REQUIRED")
    private String taskName;

    private String description;

    @NotNull(message = "TASK_START_DATE_REQUIRED")
    private LocalDate startAt;

    @NotNull(message = "TASK_END_DATE_REQUIRED")
    private LocalDate endAt;

    @NotNull(message = "TASK_STATUS_REQUIRED")
    private TaskStatus taskStatus;

    @NotNull(message = "TASK_POINT_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "TASK_POINT_MUST_BE_POSITIVE")
    private BigDecimal taskPoint;

    @NotNull(message = "TASK_ASSIGNMENTS_REQUIRED")
    private List<@Valid TaskAssignmentRequest> assignments;
}
