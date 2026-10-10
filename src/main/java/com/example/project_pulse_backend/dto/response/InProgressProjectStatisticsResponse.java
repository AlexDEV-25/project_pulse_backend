package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.ScheduleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InProgressProjectStatisticsResponse {
    private String projectName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pmName;

    private LocalDateTime endAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal clientBudget;

    private BigDecimal bac;
    private BigDecimal cpi;
    private BigDecimal spi;
    private ScheduleStatus scheduleStatus;
}
