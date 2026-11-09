package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.TaskMapper;
import com.example.project_pulse_backend.repository.TaskRepo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {
    @Test
    void createsUpdatesDeletesAndQueriesTasks() {
        TaskRepo repository = mock(TaskRepo.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        TransactionHelper transactionHelper = mock(TransactionHelper.class);
        TaskHelper taskHelper = mock(TaskHelper.class);
        TaskAssignmentHelper assignmentHelper = mock(TaskAssignmentHelper.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectMemberHelper memberHelper = mock(ProjectMemberHelper.class);
        TaskMapper mapper = mock(TaskMapper.class);
        User user = User.builder().id(1L).build();
        Project project = Project.builder().id(2L).build();
        Phase phase = Phase.builder().id(3L).project(project).build();
        Task task = Task.builder().id(4L).phase(phase).assignments(List.of()).build();
        TaskResponse response = TaskResponse.builder().id(4L).build();
        when(token.get()).thenReturn(user);
        when(phaseHelper.getPhaseById(3L, user)).thenReturn(phase);
        when(mapper.toTask(any())).thenReturn(task);
        when(assignmentHelper.createAssignments(eq(task), eq(project), any())).thenReturn(List.of());
        when(repository.save(task)).thenReturn(task);
        when(mapper.toTaskResponse(task)).thenReturn(response);
        when(repository.findForUpdateById(4L)).thenReturn(Optional.of(task));
        when(taskHelper.getProjectById(2L, user, null, null, null)).thenReturn(List.of(task));
        when(taskHelper.getById(4L, user)).thenReturn(task);
        TaskService service = new TaskService(repository, phaseHelper, projectHelper, transactionHelper,
                taskHelper, assignmentHelper, token, memberHelper, mapper);

        assertEquals(response, service.createTask(CreateTaskRequest.builder().phaseId(3L).build()));
        assertEquals(TaskStatus.TODO, task.getTaskStatus());
        assertEquals(response, service.updateTask(4L, UpdateTaskRequest.builder()
                .taskStatus(TaskStatus.TODO).build()));
        assertEquals(List.of(response), service.getTasksByProject(2L, null, null, null));
        assertEquals(response, service.getTaskById(4L));
        service.deleteTask(4L);
        verify(taskHelper).checkBeforeDelete(4L);
        verify(repository).delete(task);
    }

    @Test
    void employeeTaskDateFilterAndStatusUpdateAreCovered() {
        TaskRepo repository = mock(TaskRepo.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        TaskHelper taskHelper = mock(TaskHelper.class);
        TaskAssignmentHelper assignmentHelper = mock(TaskAssignmentHelper.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectMemberHelper memberHelper = mock(ProjectMemberHelper.class);
        TaskMapper mapper = mock(TaskMapper.class);
        User employee = User.builder().id(7L).build();
        Project project = Project.builder().id(8L).build();
        Phase phase = Phase.builder().id(3L).project(project).build();
        Task task = Task.builder().id(4L).phase(phase).taskStatus(TaskStatus.TODO)
                .assignments(List.of()).build();
        TaskResponse response = TaskResponse.builder().id(4L).build();
        ProjectMember member = ProjectMember.builder().id(9L).build();
        when(token.get()).thenReturn(employee);
        when(phaseHelper.getEmployeePhasesForWeek(eq(employee), any(), any()))
                .thenReturn(List.of(new PhaseWorkingDays(phase, Set.of())));
        when(repository.findAssignedTasksByPhaseIdsAndDate(List.of(3L), 7L, LocalDate.of(2026, 11, 11)))
                .thenReturn(List.of(task));
        when(mapper.toTaskResponse(task)).thenReturn(response);
        when(repository.findForUpdateById(4L)).thenReturn(Optional.of(task));
        when(memberHelper.findByProjectAndUser(project, 7L, employee)).thenReturn(member);
        when(repository.save(task)).thenReturn(task);
        TaskService service = new TaskService(repository, phaseHelper, projectHelper, mock(TransactionHelper.class),
                taskHelper, assignmentHelper, token, memberHelper, mapper);

        assertEquals(List.of(), service.getMyTasksByDate(LocalDate.of(2026, 11, 11), List.of()));
        assertEquals(List.of(response), service.getMyTasksByDate(
                LocalDate.of(2026, 11, 11), Arrays.asList(3L, 3L, null, -1L)));
        service.updateTaskStatus(4L,
                UpdateTaskStatusRequest.builder().taskStatus(TaskStatus.IN_PROGRESS).build());
        assertEquals(TaskStatus.IN_PROGRESS, task.getTaskStatus());
        verify(assignmentHelper).checkBelongToTask(4L, member);
    }
}
