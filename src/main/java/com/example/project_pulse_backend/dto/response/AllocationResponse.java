package com.example.project_pulse_backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AllocationResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long phaseId;
    private String phaseName;
    private Long memberId;
    private Long userId;
    private String userName;
    private Integer allocationPercentage;
    private BigDecimal resourceRateSnapshot;
    private BigDecimal allocationPoint;
}