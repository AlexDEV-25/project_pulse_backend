package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.TaskResponse;
import com.example.project_pulse_backend.dto.response.WeeklyWorkScheduleResponse;
import com.example.project_pulse_backend.service.EmployeeWorkScheduleService;
import com.example.project_pulse_backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeWorkScheduleController {
    private final EmployeeWorkScheduleService employeeWorkScheduleService;
    private final TaskService taskService;

    @GetMapping("/work-schedule")
    public APIResponse<WeeklyWorkScheduleResponse> getWeeklyWorkSchedule(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return APIResponse.<WeeklyWorkScheduleResponse>builder()
                .result(employeeWorkScheduleService.getWeeklyWorkSchedule(date))
                .build();
    }

    @GetMapping("/work-schedule/tasks")
    public APIResponse<TaskResponse> getMyTasksByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> phaseIds) {
        return APIResponse.<TaskResponse>builder()
                .resultList(taskService.getMyTasksByDate(date, phaseIds))
                .build();
    }
}
