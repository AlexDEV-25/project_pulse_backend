package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class UpdateTaskNoteRequest {

    @NotNull(message = "Ngày làm việc không được để trống")
    private LocalDate workingDate;

    @NotBlank(message = "Nội dung task note không được để trống")
    private String content;

    @NotNull(message = "Số giờ làm không được để trống")
    @Min(value = 0, message = "Số giờ làm phải từ 0 đến 8")
    @Max(value = 8, message = "Số giờ làm không được quá 8 tiếng")
    private Integer workingHours;
}
