package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.EmployeeStatisticsSummaryResponse;
import com.example.project_pulse_backend.dto.response.EmployeeTaskStatisticsResponse;
import com.example.project_pulse_backend.dto.response.InProgressProjectStatisticsResponse;
import com.example.project_pulse_backend.dto.response.ProjectEvAcStatisticResponse;
import com.example.project_pulse_backend.dto.response.ProjectOverviewStatisticsResponse;
import com.example.project_pulse_backend.dto.response.ProjectStatusStatisticResponse;
import com.example.project_pulse_backend.dto.response.StatisticsSummaryResponse;
import com.example.project_pulse_backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public APIResponse<StatisticsSummaryResponse> getSummary() {
        return APIResponse.<StatisticsSummaryResponse>builder()
                .result(statisticsService.getSummary())
                .build();
    }

    @GetMapping("/projects/status-statistics")
    public APIResponse<Map<ProjectStatus, ProjectStatusStatisticResponse>> getProjectStatusStatistics() {
        return APIResponse.<Map<ProjectStatus, ProjectStatusStatisticResponse>>builder()
                .result(statisticsService.getProjectStatusStatistics())
                .build();
    }

    @GetMapping("/projects/overview")
    public APIResponse<ProjectOverviewStatisticsResponse> getProjectOverviewStatistics() {
        return APIResponse.<ProjectOverviewStatisticsResponse>builder()
                .result(statisticsService.getProjectOverviewStatistics())
                .build();
    }

    @GetMapping("/projects/ev-ac-statistics")
    public APIResponse<Map<String, ProjectEvAcStatisticResponse>> getProjectEvAcStatistics() {
        return APIResponse.<Map<String, ProjectEvAcStatisticResponse>>builder()
                .result(statisticsService.getProjectEvAcStatistics())
                .build();
    }

    @GetMapping("/projects/in-progress")
    public APIResponse<InProgressProjectStatisticsResponse> getInProgressProjects() {
        return APIResponse.<InProgressProjectStatisticsResponse>builder()
                .resultList(statisticsService.getInProgressProjects())
                .build();
    }

    @GetMapping("/employee/summary")
    public APIResponse<EmployeeStatisticsSummaryResponse> getEmployeeSummary() {
        return APIResponse.<EmployeeStatisticsSummaryResponse>builder()
                .result(statisticsService.getEmployeeSummary())
                .build();
    }

    @GetMapping("/employee/project-points")
    public APIResponse<Map<String, BigDecimal>> getEmployeeProjectPointStatistics() {
        return APIResponse.<Map<String, BigDecimal>>builder()
                .result(statisticsService.getEmployeeProjectPointStatistics())
                .build();
    }

    @GetMapping("/employee/tasks")
    public APIResponse<EmployeeTaskStatisticsResponse> getEmployeeTasks() {
        return APIResponse.<EmployeeTaskStatisticsResponse>builder()
                .resultList(statisticsService.getEmployeeTasks())
                .build();
    }
}
