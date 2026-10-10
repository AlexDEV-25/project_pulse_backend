package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.PhaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<LocalDate> makeUpDates;
    private List<LocalDate> offDates;
    private Integer workdays;
    private PhaseStatus status;
    private BigDecimal progress;
}
