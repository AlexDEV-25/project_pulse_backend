package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.*;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceBusinessFlowTest {

    @Test
    void departmentServiceReturnsEmptyDepartmentList() {
        DepartmentRepo repository = mock(DepartmentRepo.class);
        when(repository.findAll()).thenReturn(List.of());
        DepartmentService service = new DepartmentService(
                repository, mock(UserHelper.class), mock(DepartmentMapper.class));

        assertTrue(service.getAllDepartments().isEmpty());
    }

    @Test
    void notificationServiceMarksCurrentUsersUnreadNotificationsAsRead() {
        UserNotificationRepository repository = mock(UserNotificationRepository.class);
        GetUserByToken currentUser = mock(GetUserByToken.class);
        when(currentUser.get()).thenReturn(User.builder().id(7L).build());
        when(repository.findByReceiver_IdAndReadFalse(7L)).thenReturn(List.of());
        UserNotificationService service = new UserNotificationService(
                repository, currentUser, mock(UserNotificationMapper.class));

        service.readAll();

        verify(repository).saveAll(List.of());
    }

    @Test
    void employeeScheduleReturnsAllDaysInRequestedWeek() {
        GetUserByToken currentUser = mock(GetUserByToken.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        AllocationHelper allocationHelper = mock(AllocationHelper.class);
        when(currentUser.get()).thenReturn(User.builder().id(11L).build());
        when(phaseHelper.getEmployeePhasesForWeek(
                eq(currentUser.get()), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(allocationHelper.getEmployeeAllocations(any(), anyList())).thenReturn(List.of());
        EmployeeWorkScheduleService service = new EmployeeWorkScheduleService(
                currentUser, phaseHelper, allocationHelper);

        WeeklyWorkScheduleResponse response = service.getWeeklyWorkSchedule(LocalDate.of(2026, 11, 11));

        assertEquals(LocalDate.of(2026, 11, 9), response.getWeekStart());
        assertEquals(LocalDate.of(2026, 11, 15), response.getWeekEnd());
        assertEquals(7, response.getDays().size());
        assertTrue(response.getDays().stream().noneMatch(WorkScheduleDayResponse::isWorkingDay));
    }

    @Test
    void taskCostServiceNormalizesBlankKeywordAndReturnsEmptyList() {
        GetUserByToken currentUser = mock(GetUserByToken.class);
        TaskCostTransactionHelper helper = mock(TaskCostTransactionHelper.class);
        User user = User.builder().id(5L).build();
        when(currentUser.get()).thenReturn(user);
        when(helper.getByProjectId(8L, user, null, null)).thenReturn(List.of());
        TaskCostTransactionService service = new TaskCostTransactionService(
                currentUser, helper, mock(TaskCostTransactionMapper.class));

        assertTrue(service.getByProject(8L, "   ", null).isEmpty());
        verify(helper).getByProjectId(8L, user, null, null);
    }

    @Test
    void statisticsServiceBuildsEmployeeSummaryFromCurrentUsersData() {
        UserRepo userRepo = mock(UserRepo.class);
        ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
        TaskRepo taskRepo = mock(TaskRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        TaskCostTransactionRepo costRepo = mock(TaskCostTransactionRepo.class);
        GetUserByToken currentUser = mock(GetUserByToken.class);
        User employee = User.builder().id(3L).build();
        when(currentUser.get()).thenReturn(employee);
        when(memberRepo.findByUser_Id(3L)).thenReturn(List.of());
        when(taskRepo.findAssignedTasksWithDetails(3L)).thenReturn(List.of());
        when(earningRepo.sumNetEarnedPointsByUserId(anyLong(), any(), any())).thenReturn(BigDecimal.ZERO);
        StatisticsService service = new StatisticsService(
                userRepo, memberRepo, taskRepo, earningRepo, costRepo, mock(EvmHelper.class),
                currentUser, mock(ProjectHelper.class));

        EmployeeStatisticsSummaryResponse response = service.getEmployeeSummary();

        assertEquals(0, response.getTotalProject());
        assertEquals(0, response.getTotalAssignedTask());
        assertEquals(0, response.getTotalCompletedTask());
        assertEquals(BigDecimal.ZERO, response.getTotalEarnedPoint());
    }
}
