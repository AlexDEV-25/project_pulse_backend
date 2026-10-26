package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.PhaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseEvmDetailResponse {
    private Long phaseId;
    private String phaseName;
    private LocalDate startAt;
    private LocalDate endAt;
    private PhaseStatus status;
    private BigDecimal progress;

    // Chỉ số riêng của Phase
    private BigDecimal bac;
    private BigDecimal pv;
    private BigDecimal ev;
    private BigDecimal ac;
    private BigDecimal cv;
    private BigDecimal sv;
    private BigDecimal cpi;
    private BigDecimal spi;

    // Chỉ số lũy kế (Cumulative cho S-Curve)
    private BigDecimal cumulativePv;
    private BigDecimal cumulativeEv;
    private BigDecimal cumulativeAc;
}
