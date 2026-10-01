package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvmHealthResponse {
    private Long projectId;
    private String projectName;
    private Long phaseId;
    private String phaseName;
    private LocalDateTime capturedAt;
    private BigDecimal bac;
    private BigDecimal pv;
    private BigDecimal ev;
    private BigDecimal ac;
    private BigDecimal cv;
    private BigDecimal sv;
    private BigDecimal cpi;
    private BigDecimal spi;
    private BigDecimal eac;
    private BigDecimal etc;
    private BigDecimal vac;
    private BigDecimal tcpi;
}
