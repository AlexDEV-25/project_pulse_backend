package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.entity.Phase;

import java.time.LocalDate;
import java.util.Set;

public record PhaseWorkingDays(Phase phase, Set<LocalDate> workingDates) {
}
