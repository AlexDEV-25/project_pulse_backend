package com.example.project_pulse_backend.dto.request;

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
public class CreateTaskRequest {
    @NotNull(message = "PROJECT_ID không được để trống")
    private Long projectId;

    @NotNull(message = "PHASE_ID không được để trống")
    private Long phaseId;

    @NotBlank(message = "Tên task không được để trống")
    private String taskName;

    private String description;

    @NotNull(message = "Ngày bắt đầu task không được để trống")
    private LocalDate startAt;

    @NotNull(message = "Ngày kết thúc task không được để trống")
    private LocalDate endAt;

    @NotNull(message = "Điểm task không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Điểm task phải lớn hơn 0")
    private BigDecimal taskPoint;

    @NotNull(message = "Danh sách phân công không được để trống")
    private List<@Valid TaskAssignmentRequest> assignments;
}
