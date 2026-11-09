package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.TaskAssignmentRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.TaskAssignmentHelper;
import com.example.project_pulse_backend.mapper.TaskAssignmentMapper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.TaskAssignmentRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskAssignmentServiceTest {
    @Test
    void createsAssignmentsForUniqueProjectMembers() {
        ProjectMemberRepo members = mock(ProjectMemberRepo.class);
        TaskAssignmentRepo assignments = mock(TaskAssignmentRepo.class);
        TaskAssignmentMapper mapper = mock(TaskAssignmentMapper.class);
        TaskAssignmentHelper helper = new TaskAssignmentHelper(members, assignments, mapper);
        Project project = Project.builder().id(1L).build();
        Task task = Task.builder().id(2L).build();
        ProjectMember member = ProjectMember.builder().id(3L).build();
        TaskAssignment mapped = TaskAssignment.builder().contributionPercentage(100).build();
        TaskAssignmentRequest request = TaskAssignmentRequest.builder()
                .employeeId(4L).contributionPercentage(100).build();
        when(members.findByProject_IdAndUser_Id(1L, 4L)).thenReturn(Optional.of(member));
        when(mapper.toTaskAssignment(request)).thenReturn(mapped);

        List<TaskAssignment> created = helper.createAssignments(task, project, List.of(request));

        assertEquals(1, created.size());
        assertSame(task, created.getFirst().getTask());
        assertSame(member, created.getFirst().getEmployee());
        verify(mapper).toTaskAssignment(request);
    }

    @Test
    void rejectsDuplicateAssignmentsAndMissingMembers() {
        ProjectMemberRepo members = mock(ProjectMemberRepo.class);
        TaskAssignmentMapper mapper = mock(TaskAssignmentMapper.class);
        TaskAssignmentHelper helper = new TaskAssignmentHelper(members, mock(TaskAssignmentRepo.class), mapper);
        Project project = Project.builder().id(1L).build();
        TaskAssignmentRequest request = TaskAssignmentRequest.builder().employeeId(4L).build();
        when(members.findByProject_IdAndUser_Id(1L, 4L))
                .thenReturn(Optional.of(ProjectMember.builder().id(5L).build()));
        when(mapper.toTaskAssignment(request)).thenReturn(new TaskAssignment());
        AppException duplicate = assertThrows(AppException.class, () -> helper.createAssignments(
                new Task(), project, List.of(request, request)));
        assertEquals(AppError.TASK_ASSIGNMENT_ALREADY_EXISTS, duplicate.getAppError());

        when(members.findByProject_IdAndUser_Id(1L, 4L)).thenReturn(Optional.empty());
        AppException missingMember = assertThrows(AppException.class, () -> helper.createAssignments(
                new Task(), project, List.of(request)));
        assertEquals(AppError.MEMBER_NOT_FOUND, missingMember.getAppError());
    }

    @Test
    void checksAssignmentMembership() {
        TaskAssignmentRepo assignments = mock(TaskAssignmentRepo.class);
        TaskAssignmentHelper helper = new TaskAssignmentHelper(mock(ProjectMemberRepo.class), assignments,
                mock(TaskAssignmentMapper.class));
        ProjectMember member = ProjectMember.builder().id(3L).build();
        when(assignments.findByTask_Id(2L)).thenReturn(List.of(
                TaskAssignment.builder().employee(member).build()));

        assertDoesNotThrow(() -> helper.checkBelongToTask(2L, member));
        when(assignments.findByTask_Id(2L)).thenReturn(List.of());
        AppException missingAssignment = assertThrows(AppException.class,
                () -> helper.checkBelongToTask(2L, member));
        assertEquals(AppError.USER_NOT_BELONG_TO_TASK, missingAssignment.getAppError());
    }
}
