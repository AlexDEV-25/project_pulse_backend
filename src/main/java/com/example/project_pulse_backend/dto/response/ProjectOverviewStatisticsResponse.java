package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOverviewStatisticsResponse {
    private long totalProject;
    private Map<ProjectStatus, Long> projectStatusCounts;
}
