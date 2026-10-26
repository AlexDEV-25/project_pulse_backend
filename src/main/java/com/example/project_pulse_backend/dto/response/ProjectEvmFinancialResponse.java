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
public class ProjectEvmFinancialResponse {
    private Long projectId;
    private String projectName;
    private LocalDateTime capturedAt;

    private BigDecimal projectBudget; // BAC
    private BigDecimal clientBudget;  // null if PM not authorized

    // EVM core
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

    // Dual profitability & Margin
    private BigDecimal evToBacRatio;          // (EV / BAC) * 100
    private BigDecimal evToClientBudgetRatio; // (EV / ClientBudget) * 100 (if clientBudget != null)
    private BigDecimal currentMargin;         // ClientBudget - AC (if clientBudget != null)
    private BigDecimal forecastedMargin;      // ClientBudget - EAC (if clientBudget != null)
    private BigDecimal marginPercentage;      // (CurrentMargin / ClientBudget) * 100 (if clientBudget != null)
}
