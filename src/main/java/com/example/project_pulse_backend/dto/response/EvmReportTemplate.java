package com.example.project_pulse_backend.dto.response;

import java.util.List;

public record EvmReportTemplate(
        String summaryTitle,
        List<String> summaryLabels,
        String phasesTitle,
        String phasesColumns,
        String employeesTitle,
        String employeesColumns) {
}