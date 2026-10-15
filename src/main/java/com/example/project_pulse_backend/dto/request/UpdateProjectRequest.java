package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.ProjectStatus;
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
public class UpdateProjectRequest {
    @NotBlank(message = "PROJECT_NAME_REQUIRED")
    @Size(min = 2, max = 100, message = "PROJECT_NAME_LENGTH_INVALID")
    private String projectName;

    @NotNull(message = "PM_ID_REQUIRED")
    private Long pmId;

    private LocalDate startAt;
    private LocalDate endAt;
    private BigDecimal clientBudget;
    private BigDecimal projectBudget;
    private String description;

    @NotNull(message = "PROJECT_STATUS_REQUIRED")
    private ProjectStatus projectStatus;

}
