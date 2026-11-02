package com.example.project_pulse_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleDayResponse {
    private LocalDate date;
    private DayOfWeek dayOfWeek;

    @JsonProperty("isWorkingDay")
    private boolean isWorkingDay;

    private List<AllocationResponse> allocations;
}
