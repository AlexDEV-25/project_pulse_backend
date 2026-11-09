package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskHelperTest {
    @Test
    void retrievesTasksAndChecksRulesAndDeletion() {
        TaskRepo taskRepo = mock(TaskRepo.class);
        TaskNoteRepo noteRepo = mock(TaskNoteRepo.class);
        TaskCostTransactionRepo costRepo = mock(TaskCostTransactionRepo.class);
        EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        TaskHelper helper = new TaskHelper(taskRepo, noteRepo, costRepo, earningRepo, projectHelper,
                phaseHelper, mock(NotificationHelper.class));
        User user = User.builder().id(7L).build();
        Project project = Project.builder().id(4L).build();
        Phase phase = Phase.builder().id(3L).project(project)
                .startAt(LocalDate.of(2026, 9, 7)).endAt(LocalDate.of(2026, 9, 20)).build();
        Task task = Task.builder().id(9L).phase(phase).build();
        when(projectHelper.getProjectById(4L, user)).thenReturn(project);
        when(taskRepo.filterTask(4L, 3L, "api", 7L)).thenReturn(List.of(task));
        assertEquals(List.of(task), helper.getProjectById(4L, user, 3L, "api", 7L));
        when(taskRepo.findById(9L)).thenReturn(Optional.of(task));
        assertSame(task, helper.getById(9L, user));
        verify(projectHelper).checkAccessToProject(4L, user);
        when(taskRepo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getById(10L, user));

        helper.checkDone(phase);
        verify(projectHelper).checkProjectNotDone(project);
        verify(phaseHelper).checkPhaseNotDone(phase);
        helper.checkTime(phase, phase.getStartAt(), phase.getEndAt());
        assertThrows(AppException.class, () ->
                helper.checkTime(phase, phase.getStartAt().minusDays(1), phase.getEndAt()));
        helper.checkEmployeeStatusTransition(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

        when(noteRepo.findByTask_Id(9L)).thenReturn(List.of());
        when(costRepo.findByTask_Id(9L)).thenReturn(List.of());
        when(earningRepo.findByTask_Id(9L)).thenReturn(List.of());
        helper.checkBeforeDelete(9L);
        when(noteRepo.findByTask_Id(9L)).thenReturn(List.of(TaskNote.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDelete(9L));
        when(noteRepo.findByTask_Id(9L)).thenReturn(List.of());
        when(costRepo.findByTask_Id(9L)).thenReturn(List.of(TaskCostTransaction.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDelete(9L));
        when(costRepo.findByTask_Id(9L)).thenReturn(List.of());
        when(earningRepo.findByTask_Id(9L)).thenReturn(List.of(EarningTransaction.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDelete(9L));

        when(noteRepo.existsByTask_IdAndTaskNoteStatus(9L, TaskNoteStatus.PENDING_CONFIRMATION))
                .thenReturn(false);
        helper.checkUnconfirmedTaskNotes(task);
        when(noteRepo.existsByTask_IdAndTaskNoteStatus(9L, TaskNoteStatus.PENDING_CONFIRMATION))
                .thenReturn(true);
        assertThrows(AppException.class, () -> helper.checkUnconfirmedTaskNotes(task));
    }

    @Test
    void assignsAndRemovesTaskNotificationsThroughBothAssignmentOverloads() {
        NotificationHelper notification = mock(NotificationHelper.class);
        TaskHelper helper = new TaskHelper(mock(TaskRepo.class), mock(TaskNoteRepo.class),
                mock(TaskCostTransactionRepo.class), mock(EarningTransactionRepo.class),
                mock(ProjectHelper.class), mock(PhaseHelper.class), notification);
        User pm = User.builder().id(1L).build();
        User firstUser = User.builder().id(2L).build();
        User secondUser = User.builder().id(3L).build();
        Project project = Project.builder().id(4L).pm(pm).build();
        Task task = Task.builder().id(9L).taskName("Build")
                .phase(Phase.builder().project(project).build()).build();
        ProjectMember firstMember = ProjectMember.builder().id(1L).user(firstUser).build();
        ProjectMember secondMember = ProjectMember.builder().id(2L).user(secondUser).build();
        TaskAssignment oldAssignment = TaskAssignment.builder().task(task).employee(firstMember).build();
        TaskAssignment newAssignment = TaskAssignment.builder().task(task).employee(secondMember).build();
        Notification note = Notification.builder().build();
        when(notification.createNotification(any())).thenReturn(note);

        helper.assignTaskNotification(task, null);
        helper.assignTaskNotification(Task.builder().assignments(List.of()).build(), pm);
        Task assignedTask = Task.builder().id(9L).taskName("Build")
                .phase(Phase.builder().project(project).build())
                .assignments(List.of(newAssignment)).build();
        helper.assignTaskNotification(assignedTask, pm);
        verify(notification).createUserNotification(argThat(request ->
                request.getReceiver() == secondUser && request.getSender() == pm));

        helper.checkDifferentTaskAssignment(task, pm, List.of(oldAssignment), List.of(newAssignment));
        assertEquals(3L, mockingDetails(notification).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("createNotification")).count());
        assertEquals(3L, mockingDetails(notification).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("createUserNotification")).count());

        helper.checkDifferentTaskAssignment(List.of(oldAssignment), List.of(newAssignment));
        verify(notification, times(5)).createNotification(any());
        helper.checkDifferentTaskAssignment(null, List.of());
    }
}
