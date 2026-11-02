package com.example.project_pulse_backend.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record ScheduledAllocation(
        Long projectId,
        String projectName,
        Long phaseId,
        String phaseName,
        Long allocationId,
        Integer allocationPercentage,
        Set<LocalDate> workingDates) {
}