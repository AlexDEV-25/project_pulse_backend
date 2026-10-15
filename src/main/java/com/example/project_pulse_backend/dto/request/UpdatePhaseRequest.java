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

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startAt;

    @NotNull(message = "END_DATE_REQUIRED")
    private LocalDate endAt;

    @NotBlank(message = "PHASE_NAME_REQUIRED")
    @Size(min = 2, max = 100, message = "PHASE_NAME_LENGTH_INVALID")
    private String phaseName;

    @NotNull(message = "PHASE_STATUS_REQUIRED")
    private PhaseStatus phaseStatus;

    private List<LocalDate> makeUpDates;
    private List<LocalDate> offDates;
}
