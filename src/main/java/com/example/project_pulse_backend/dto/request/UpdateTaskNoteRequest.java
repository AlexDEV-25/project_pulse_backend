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

    @NotNull(message = "WORKING_DATE_REQUIRED")
    private LocalDate workingDate;

    @NotBlank(message = "TASK_NOTE_CONTENT_REQUIRED")
    private String content;

    @NotNull(message = "WORKING_HOURS_REQUIRED")
    @Min(value = 0, message = "WORKING_HOURS_MIN_INVALID")
    @Max(value = 8, message = "WORKING_HOURS_MAX_INVALID")
    private Integer workingHours;
}
