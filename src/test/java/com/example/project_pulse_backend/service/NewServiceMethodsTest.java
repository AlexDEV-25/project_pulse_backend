package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.*;
import com.example.project_pulse_backend.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewServiceMethodsTest {
    @Test
    void scheduleIncludesWorkingAllocationAndUsesCurrentWeek() {
        GetUserByToken token = mock(GetUserByToken.class);
        PhaseHelper phases = mock(PhaseHelper.class);
        AllocationHelper allocations = mock(AllocationHelper.class);
        User employee = User.builder().id(11L).build();
        when(token.get()).thenReturn(employee);
        when(phases.getEmployeePhasesForWeek(eq(employee), any(), any())).thenReturn(List.of());
        when(allocations.getEmployeeAllocations(eq(employee), anyList())).thenReturn(List.of(
                new ScheduledAllocation(2L, "Project", 3L, "Phase", 4L, 50,
                        Set.of(LocalDate.of(2026, 11, 10)))));

        var response = new EmployeeWorkScheduleService(token, phases, allocations)
                .getWeeklyWorkSchedule(LocalDate.of(2026, 11, 11));

        assertEquals(7, response.getDays().size());
        var workingDay = response.getDays().stream()
                .filter(day -> day.getDate().equals(LocalDate.of(2026, 11, 10))).findFirst().orElseThrow();
        assertTrue(workingDay.isWorkingDay());
        assertEquals(1, workingDay.getAllocations().size());
        assertEquals(4L, workingDay.getAllocations().getFirst().getId());
        verify(allocations).getEmployeeAllocations(eq(employee), anyList());
    }

    @Test
    void notificationsListReadReadAllAndMissingReadAreCovered() {
        UserNotificationRepository repository = mock(UserNotificationRepository.class);
        GetUserByToken token = mock(GetUserByToken.class);
        UserNotificationMapper mapper = mock(UserNotificationMapper.class);
        User receiver = User.builder().id(7L).build();
        UserNotification unread = UserNotification.builder().id(9L).receiver(receiver).read(false).build();
        when(token.get()).thenReturn(receiver);
        when(repository.findByReceiver_IdOrderByCreatedAtDescIdDesc(7L)).thenReturn(List.of(unread));
        when(repository.findByReceiver_IdAndReadFalseOrderByCreatedAtDescIdDesc(7L)).thenReturn(List.of(unread));
        when(repository.findByIdAndReceiver_Id(9L, 7L)).thenReturn(Optional.of(unread));
        when(repository.findByReceiver_IdAndReadFalse(7L)).thenReturn(List.of(unread));
        when(repository.save(unread)).thenReturn(unread);
        when(mapper.toUserNotificationResponse(unread)).thenReturn(UserNotificationResponse.builder().build());
        UserNotificationService service = new UserNotificationService(repository, token, mapper);

        assertEquals(1, service.getByReceiver().size());
        assertEquals(1, service.getByReceiverIdAndReadFalse().size());
        assertNotNull(service.read(9L));
        assertTrue(unread.isRead());
        service.readAll();
        assertTrue(unread.isRead());
        service.readAll(9L);
        verify(repository, times(2)).saveAll(List.of(unread));

        when(repository.findByIdAndReceiver_Id(10L, 7L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.read(10L));
    }

    @Test
    void taskCostQueriesNormalizeKeywordAndMapSingleTransaction() {
        GetUserByToken token = mock(GetUserByToken.class);
        TaskCostTransactionHelper helper = mock(TaskCostTransactionHelper.class);
        TaskCostTransactionMapper mapper = mock(TaskCostTransactionMapper.class);
        User user = User.builder().id(5L).build();
        TaskCostTransaction transaction = TaskCostTransaction.builder().id(6L).build();
        TaskCostTransactionResponse expected = TaskCostTransactionResponse.builder().build();
        when(token.get()).thenReturn(user);
        when(helper.getByProjectId(8L, user, "design", 3L)).thenReturn(List.of(transaction));
        when(helper.getById(6L, user)).thenReturn(transaction);
        when(mapper.toTaskCostTransactionResponse(transaction)).thenReturn(expected);
        TaskCostTransactionService service = new TaskCostTransactionService(token, helper, mapper);

        assertEquals(List.of(expected), service.getByProject(8L, "  design  ", 3L));
        assertEquals(expected, service.getById(6L));
        verify(helper).getByProjectId(8L, user, "design", 3L);
    }
}
