package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.PhaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePhaseStatusRequest {
    @NotNull(message = "PHASE_STATUS_REQUIRED_VI")
    private PhaseStatus status;
}
