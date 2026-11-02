package com.example.project_pulse_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeeklyWorkScheduleResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<WorkScheduleDayResponse> days;
}
