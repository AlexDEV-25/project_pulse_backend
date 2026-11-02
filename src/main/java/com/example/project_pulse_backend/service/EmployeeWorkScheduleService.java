package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.AllocationHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.PhaseHelper;
import com.example.project_pulse_backend.helper.TimeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeWorkScheduleService {
    private final GetUserByToken getUserByToken;
    private final PhaseHelper phaseHelper;
    private final AllocationHelper allocationHelper;

    @PreAuthorize("hasAuthority('GET_EMPLOYEE_WORK_SCHEDULE')")
    public WeeklyWorkScheduleResponse getWeeklyWorkSchedule(LocalDate date) {
        User employee = getUserByToken.get();
        LocalDate requestedDate = date == null ? LocalDate.now() : date;
        TimeHelper.WeekRange week = TimeHelper.weekRange(requestedDate);

        List<PhaseWorkingDays> phases = phaseHelper.getEmployeePhasesForWeek(
                employee, week.weekStart(), week.weekEnd());
        List<ScheduledAllocation> allocations = allocationHelper.getEmployeeAllocations(
                employee, phases);

        Map<LocalDate, List<AllocationResponse>> allocationsByDate = new LinkedHashMap<>();
        Set<LocalDate> workingDates = allocations.stream()
                .flatMap(allocation -> allocation.workingDates().stream())
                .collect(Collectors.toSet());
        for (LocalDate currentDate : week.weekStart().datesUntil(week.weekEnd().plusDays(1)).toList()) {
            allocationsByDate.put(currentDate, new ArrayList<>());
        }

        for (ScheduledAllocation allocation : allocations) {
            AllocationResponse dto = AllocationResponse.builder()
                    .projectId(allocation.projectId())
                    .projectName(allocation.projectName())
                    .phaseId(allocation.phaseId())
                    .phaseName(allocation.phaseName())
                    .id(allocation.allocationId())
                    .allocationPercentage(allocation.allocationPercentage())
                    .build();
            for (LocalDate workingDate : allocation.workingDates()) {
                allocationsByDate.get(workingDate).add(dto);
            }
        }

        List<WorkScheduleDayResponse> days = allocationsByDate.entrySet().stream()
                .map(entry -> WorkScheduleDayResponse.builder()
                        .date(entry.getKey())
                        .dayOfWeek(DayOfWeek.from(entry.getKey()))
                        .isWorkingDay(workingDates.contains(entry.getKey()))
                        .allocations(entry.getValue())
                        .build())
                .toList();

        return WeeklyWorkScheduleResponse.builder()
                .weekStart(week.weekStart())
                .weekEnd(week.weekEnd())
                .days(days)
                .build();
    }
}
