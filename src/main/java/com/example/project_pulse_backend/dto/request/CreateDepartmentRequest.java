package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepartmentRequest {
    @NotBlank(message = "DEPARTMENT_NAME_REQUIRED")
    @Size(min = 2, max = 50, message = "DEPARTMENT_NAME_LENGTH_INVALID")
    private String departmentName;
}
