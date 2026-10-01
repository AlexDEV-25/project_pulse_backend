package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentRequest {
    @NotNull(message = "EMPLOYEE_ID không được để trống")
    private Long employeeId;

    @NotNull(message = "Tỷ lệ đóng góp không được để trống")
    @Min(value = 0, message = "Tỷ lệ đóng góp phải từ 0 đến 100")
    @Max(value = 100, message = "Tỷ lệ đóng góp phải từ 0 đến 100")
    private Integer contributionPercentage;
}
