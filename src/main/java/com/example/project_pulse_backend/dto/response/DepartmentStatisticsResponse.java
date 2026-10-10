package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatisticsResponse {
    private long totalDepartments;
    private long activeDepartments;
    private long hiddenDepartments;
}
