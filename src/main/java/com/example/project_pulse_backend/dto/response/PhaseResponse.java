package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.AllocationPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String phaseName;
    private Integer workdays;
    private AllocationPlanStatus status;
}
