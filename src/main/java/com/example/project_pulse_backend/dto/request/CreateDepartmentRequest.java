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
    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(min = 2, max = 50, message = "Tên phòng ban phải từ 2 đến 50 ký tự")
    private String departmentName;
}
