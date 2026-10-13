package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.CreateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskStatusRequest;
import com.example.project_pulse_backend.dto.response.TaskAssignmentResponse;
import com.example.project_pulse_backend.dto.response.TaskResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepo taskRepo;
    private final PhaseHelper phaseHelper;
    private final ProjectHelper projectHelper;
    private final TransactionHelper transactionHelper;
    private final TaskHelper taskHelper;
    private final TaskAssignmentHelper taskAssignmentHelper;
    private final GetUserByToken getUserByToken;
    private final ProjectMemberHelper projectMemberHelper;

    @PreAuthorize("hasAuthority('CREATE_TASK')")
    public TaskResponse createTask(CreateTaskRequest request) {
        User pm = getUserByToken.get();

        Project project = projectHelper.getProjectById(request.getProjectId(), pm);
        Phase phase = phaseHelper.getPhaseById(request.getPhaseId(), pm);

        taskHelper.checkDone(project, phase);
        taskHelper.checkSameProject(project, phase);
        taskHelper.checkTime(phase, request.getStartAt(), request.getEndAt());

        Task task = Task.builder()
                .phase(phase)
                .taskName(request.getTaskName())
                .description(request.getDescription())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .taskStatus(TaskStatus.TODO)
                .taskPoint(request.getTaskPoint())
                .assignments(new ArrayList<>())
                .build();
        task.getAssignments().addAll(taskAssignmentHelper.createAssignments(task, project, request.getAssignments()));

        return toResponse(taskRepo.save(task));
    }

    @PreAuthorize("hasAuthority('UPDATE_TASK')")
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        User pm = getUserByToken.get();

        Task task = taskRepo.findForUpdateById(id).orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());

        projectHelper.checkAccessToProject(task.getPhase().getProject().getId(), pm);

        if (task.getTaskStatus() == TaskStatus.DONE) {
            throw AppException.builder().appError(AppError.TASK_ALREADY_DONE).build();
        }

        taskHelper.checkDone(task.getPhase().getProject(), task.getPhase());
        taskHelper.checkTime(task.getPhase(), request.getStartAt(), request.getEndAt());

        if (request.getTaskStatus() == TaskStatus.DONE && task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw AppException.builder().appError(AppError.TASK_NOT_IN_PROGRESS).build();
        }

        task.setTaskName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setStartAt(request.getStartAt());
        task.setEndAt(request.getEndAt());
        task.setTaskPoint(request.getTaskPoint());
        task.getAssignments().clear();
        task.getAssignments().addAll(taskAssignmentHelper.createAssignments(
                task, task.getPhase().getProject(), request.getAssignments()));

        if (request.getTaskStatus() == TaskStatus.DONE) {
            transactionHelper.createCompletionTransactions(task);
        }
        task.setTaskStatus(request.getTaskStatus());

        return toResponse(taskRepo.save(task));
    }

    @PreAuthorize("hasAuthority('DELETE_TASK')")
    public void deleteTask(Long id) {
        User pm = getUserByToken.get();
        Task task = taskHelper.getById(id, pm);
        if (task.getPhase().getStatus() == PhaseStatus.DONE) {
            throw AppException.builder().appError(AppError.TASK_PHASE_DONE).build();
        }
        if (task.getTaskStatus() != null && !task.getTaskStatus().name().equals("TODO")) {
            throw AppException.builder().appError(AppError.TASK_IN_PROGRESS_CANNOT_DELETE).build();
        }
        taskRepo.delete(task);
    }

    @PreAuthorize("hasAuthority('GET_TASKS_BY_PROJECT')")
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId, String keyword, Long phaseId, Long userId) {
        User user = getUserByToken.get();

        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return taskHelper.getProjectById(projectId, user, phaseId, normalizedKeyword, userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_TASKS_BY_PROJECT')")
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId, String keyword, Long phaseId) {
        return getTasksByProject(projectId, keyword, phaseId, null);
    }
    
    @PreAuthorize("hasAuthority('GET_TASK_BY_ID')")
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        User user = getUserByToken.get();
        Task task = taskHelper.getById(id, user);
        return toResponse(task);
    }

    @PreAuthorize("hasAuthority('UPDATE_MY_TASK_STATUS_BY_ID')")
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        User user = getUserByToken.get();
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        ProjectMember author = projectMemberHelper.findByProjectAndUser(task.getPhase().getProject(), user.getId(), user);

        taskAssignmentHelper.checkBelongToTask(task.getId(), author);
        taskHelper.checkEmployeeStatusTransition(task.getTaskStatus(), request.getTaskStatus());
        task.setTaskStatus(request.getTaskStatus());
        return toResponse(taskRepo.save(task));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getPhase().getProject().getId())
                .projectName(task.getPhase().getProject().getProjectName())
                .phaseId(task.getPhase().getId())
                .phaseName(task.getPhase().getPhaseName())
                .taskName(task.getTaskName())
                .description(task.getDescription())
                .startAt(task.getStartAt())
                .endAt(task.getEndAt())
                .taskStatus(task.getTaskStatus())
                .taskPoint(task.getTaskPoint())
                .progress(task.getAssignments().stream()
                        .mapToInt(TaskAssignment::getContributionPercentage)
                        .sum())
                .assignments(task.getAssignments().stream()
                        .map(this::toAssignmentResponse)
                        .toList())
                .build();
    }

    private TaskAssignmentResponse toAssignmentResponse(TaskAssignment assignment) {
        ProjectMember member = assignment.getEmployee();
        return TaskAssignmentResponse.builder()
                .id(assignment.getId())
                .employeeId(member.getUser().getId())
                .employeeName(member.getUser().getUserName())
                .contributionPercentage(assignment.getContributionPercentage())
                .build();
    }

}
