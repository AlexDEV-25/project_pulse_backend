package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.PhaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePhaseRequest {

    @NotNull(message = "Start date cannot be null")
    private LocalDate startAt;

    @NotNull(message = "End date cannot be null")
    private LocalDate endAt;

    @NotBlank(message = "Ten phase khong duoc de trong")
    @Size(min = 2, max = 100, message = "Ten phase phai tu 2 den 100 ky tu")
    private String phaseName;

    @NotNull(message = "Phase status cannot be null")
    private PhaseStatus phaseStatus;

    private List<LocalDate> makeUpDates;
    private List<LocalDate> offDates;
}
