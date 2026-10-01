package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmTaskNotesRequest {
    @NotNull(message = "EMPLOYEE_ID không được để trống")
    private Long employeeId;

    @NotNull(message = "Ngày xác nhận không được để trống")
    private LocalDate date;
}
