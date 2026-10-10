package com.example.project_pulse_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsSummaryResponse {
    private long totalProject;
    private Long totalUser;
    private long totalOverdueProject;
    private long totalNearingDeadlineProject;
}
