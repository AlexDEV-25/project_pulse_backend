package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import com.example.project_pulse_backend.repository.TaskRepo;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.response.PhaseWorkingDays;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

class PhaseHelperCalendarTest {
    @Test
    void rejectsSpecialDatesThatAreBothMakeUpAndOffDates() {
        PhaseHelper helper = new PhaseHelper(
                mock(PhaseRepo.class),
                mock(TaskRepo.class),
                mock(AllocationRepo.class),
                mock(EarningTransactionRepo.class),
                mock(ProjectHelper.class));
        LocalDate specialDate = LocalDate.of(2026, 10, 10);

        assertThrows(AppException.class, () -> helper.checkSpecialDates(
                specialDate,
                specialDate,
                List.of(specialDate),
                List.of(specialDate)));
    }

    @Test
    void rejectsMakeUpDatesOutsidePhaseRange() {
        PhaseHelper helper = new PhaseHelper(
                mock(PhaseRepo.class),
                mock(TaskRepo.class),
                mock(AllocationRepo.class),
                mock(EarningTransactionRepo.class),
                mock(ProjectHelper.class));
        LocalDate start = LocalDate.of(2026, 10, 10);

        assertThrows(AppException.class, () -> helper.checkSpecialDates(
                start,
                start,
                List.of(start.plusDays(1)),
                List.of()));
    }

    @Test
    void retrievesAndCalculatesEmployeePhaseDays() {
        PhaseRepo phaseRepo = mock(PhaseRepo.class);
        TaskRepo taskRepo = mock(TaskRepo.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        PhaseHelper helper = new PhaseHelper(phaseRepo, taskRepo, allocationRepo, earningRepo, projectHelper);
        User user = User.builder().id(7L).build();
        LocalDate weekStart = LocalDate.of(2026, 9, 7);
        LocalDate weekEnd = weekStart.plusDays(6);
        Phase phase = Phase.builder().id(3L).project(Project.builder().id(4L).build())
                .startAt(weekStart.minusDays(2)).endAt(weekEnd.plusDays(2))
                .makeUpDates(List.of()).offDates(List.of()).build();
        when(phaseRepo.filterPhase(4L, "api")).thenReturn(List.of(phase));
        when(phaseRepo.findAllocatedPhasesOverlappingWeek(7L, weekStart, weekEnd)).thenReturn(List.of(phase));
        when(phaseRepo.findById(3L)).thenReturn(Optional.of(phase));

        assertEquals(List.of(phase), helper.getPhasesByProject(4L, user, "api"));
        List<PhaseWorkingDays> employeePhases = helper.getEmployeePhasesForWeek(user, weekStart, weekEnd);
        assertEquals(Set.of(weekStart, weekStart.plusDays(1), weekStart.plusDays(2),
                weekStart.plusDays(3), weekStart.plusDays(4)), employeePhases.getFirst().workingDates());
        assertSame(phase, helper.getPhaseById(3L, user));
        org.mockito.Mockito.verify(projectHelper, org.mockito.Mockito.times(2))
                .checkAccessToProject(4L, user);
        when(phaseRepo.findById(3L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getPhaseById(3L, user));

        assertEquals(List.of(), helper.normalizeDates(null));
        List<LocalDate> dates = List.of(weekStart);
        assertEquals(dates, helper.normalizeDates(dates));
        assertNotSame(dates, helper.normalizeDates(dates));
        helper.checkPhaseNotDone(phase);
        assertThrows(AppException.class, () -> helper.checkPhaseNotDone(
                Phase.builder().status(PhaseStatus.DONE).build()));
    }

    @Test
    void validatesPhaseTimesProgressAndDeletion() {
        PhaseRepo phaseRepo = mock(PhaseRepo.class);
        TaskRepo taskRepo = mock(TaskRepo.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        PhaseHelper helper = new PhaseHelper(phaseRepo, taskRepo, allocationRepo, earningRepo,
                mock(ProjectHelper.class));
        LocalDate start = LocalDate.of(2026, 9, 7);
        LocalDate end = start.plusDays(4);
        Project project = Project.builder().startAt(start.minusDays(1)).endAt(end.plusDays(1)).build();
        Phase phase = Phase.builder().id(2L).startAt(start).endAt(end).build();
        when(taskRepo.findByPhase_Id(2L)).thenReturn(List.of(
                Task.builder().startAt(start).endAt(start.plusDays(2)).build()));
        helper.checkTime(phase, project, start, end);
        when(taskRepo.findByPhase_Id(2L)).thenReturn(List.of(
                Task.builder().startAt(start.minusDays(1)).endAt(start.plusDays(2)).build()));
        assertThrows(AppException.class, () -> helper.checkTime(phase, project, start, end));
        assertThrows(AppException.class, () -> helper.checkTime(null, project, end, start));

        when(taskRepo.sumTaskPointsByPhaseId(2L)).thenReturn(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, helper.calculateProgress(2L));
        when(taskRepo.sumTaskPointsByPhaseId(2L)).thenReturn(new BigDecimal("20"));
        when(earningRepo.sumNetEarnedPointsByPhaseId(2L, EarningTransactionType.EARN))
                .thenReturn(new BigDecimal("5"));
        assertEquals(new BigDecimal("25.00"), helper.calculateProgress(2L));

        when(allocationRepo.findByPhase_Id(2L)).thenReturn(List.of());
        when(taskRepo.findByPhase_Id(2L)).thenReturn(List.of());
        helper.checkBeforeDelete(2L);
        when(allocationRepo.findByPhase_Id(2L)).thenReturn(List.of(
                com.example.project_pulse_backend.entity.Allocation.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDelete(2L));

        when(taskRepo.existsByPhase_IdAndTaskStatusNotIn(2L, List.of(TaskStatus.DONE, TaskStatus.CANCELED)))
                .thenReturn(false);
        helper.checkInProgressTasks(phase);
        when(taskRepo.existsByPhase_IdAndTaskStatusNotIn(2L, List.of(TaskStatus.DONE, TaskStatus.CANCELED)))
                .thenReturn(true);
        assertThrows(AppException.class, () -> helper.checkInProgressTasks(phase));
    }
}
