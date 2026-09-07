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
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {
    @NotBlank(message = "Tên project không được để trống")
    @Size(min = 2, max = 100, message = "Tên project phải từ 2 đến 100 ký tự")
    private String projectName;

    @NotNull(message = "PM_ID không được để trống")
    private Long pmId;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal clientBudget;
    private BigDecimal projectBudget;
    private String description;

    @NotNull(message = "Trạng thái project không được để trống")
    private ProjectStatus projectStatus;
}
