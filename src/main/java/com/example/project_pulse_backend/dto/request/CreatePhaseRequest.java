package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePhaseRequest {
    @NotNull(message = "Project_ID khong duoc de trong")
    private Long projectId;

    @NotNull(message = "Start date cannot be null")
    private LocalDateTime startAt;

    @NotNull(message = "End date cannot be null")
    private LocalDateTime endAt;

    @NotBlank(message = "Ten phase khong duoc de trong")
    @Size(min = 2, max = 100, message = "Ten phase phai tu 2 den 100 ky tu")
    private String phaseName;

    private Integer workdays;
}
