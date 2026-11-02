package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.CreateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskStatusRequest;
import com.example.project_pulse_backend.dto.response.TaskResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.TaskMapper;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepo taskRepo;
    private final PhaseHelper phaseHelper;
    private final ProjectHelper projectHelper;
    private final TransactionHelper transactionHelper;
    private final TaskHelper taskHelper;
    private final TaskAssignmentHelper taskAssignmentHelper;
    private final GetUserByToken getUserByToken;
    private final ProjectMemberHelper projectMemberHelper;
    private final TaskMapper taskMapper;

    @PreAuthorize("hasAuthority('CREATE_TASK')")
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        User pm = getUserByToken.get();
        Phase phase = phaseHelper.getPhaseById(request.getPhaseId(), pm);

        taskHelper.checkDone(phase);
        taskHelper.checkTime(phase, request.getStartAt(), request.getEndAt());

        Task task = taskMapper.toTask(request);
        task.setPhase(phase);
        task.setTaskStatus(TaskStatus.TODO);
        task.setAssignments(new ArrayList<>());
        task.getAssignments().addAll(taskAssignmentHelper.createAssignments(task, phase.getProject(), request.getAssignments()));

        Task newTask = taskRepo.save(task);
        taskHelper.assignTaskNotification(newTask, pm);
        return toResponse(newTask);
    }

    @PreAuthorize("hasAuthority('UPDATE_TASK')")
    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        User pm = getUserByToken.get();

        Task task = taskRepo.findForUpdateById(id).orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        projectHelper.checkAccessToProject(task.getPhase().getProject().getId(), pm);

        if (task.getTaskStatus() == TaskStatus.DONE) {
            throw AppException.builder().appError(AppError.TASK_ALREADY_DONE).build();
        }

        taskHelper.checkDone(task.getPhase());
        taskHelper.checkTime(task.getPhase(), request.getStartAt(), request.getEndAt());

        if (request.getTaskStatus() == TaskStatus.DONE && task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw AppException.builder().appError(AppError.TASK_NOT_IN_PROGRESS).build();
        }
        List<TaskAssignment> oldTaskAssignments = new ArrayList<>(task.getAssignments());

        taskMapper.updateTask(request, task);
        task.getAssignments().clear();
        List<TaskAssignment> newTaskAssignments = taskAssignmentHelper.createAssignments(
                task, task.getPhase().getProject(), request.getAssignments());
        task.getAssignments().addAll(newTaskAssignments);

        if (request.getTaskStatus() == TaskStatus.DONE && task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
            taskHelper.checkUnconfirmedTaskNotes(task);
            transactionHelper.createCompletionTransactions(task);
        }
        task.setTaskStatus(request.getTaskStatus());
        taskHelper.checkDifferentTaskAssignment(task, pm, oldTaskAssignments, newTaskAssignments);
        return toResponse(taskRepo.save(task));
    }

    @PreAuthorize("hasAuthority('DELETE_TASK')")
    @Transactional
    public void deleteTask(Long id) {
        User pm = getUserByToken.get();
        Task task = taskHelper.getById(id, pm);
        taskHelper.checkDone(task.getPhase());
        taskHelper.checkBeforeDelete(task.getId());
        taskRepo.delete(task);
    }

    @PreAuthorize("hasAuthority('GET_TASKS_BY_PROJECT')")
    public List<TaskResponse> getTasksByProject(Long projectId, String keyword, Long phaseId, Long userId) {
        User user = getUserByToken.get();

        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return taskHelper.getProjectById(projectId, user, phaseId, normalizedKeyword, userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_EMPLOYEE_WORK_SCHEDULE_TASKS')")
    public List<TaskResponse> getMyTasksByDate(LocalDate selectedDate, List<Long> phaseIds) {
        User employee = getUserByToken.get();
        if (phaseIds == null || phaseIds.isEmpty()) {
            return List.of();
        }

        List<Long> requestedPhaseIds = phaseIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
        if (requestedPhaseIds.isEmpty()) {
            return List.of();
        }

        TimeHelper.WeekRange week = TimeHelper.weekRange(selectedDate);
        List<Long> employeePhaseIds = phaseHelper.getEmployeePhasesForWeek(
                        employee, week.weekStart(), week.weekEnd()).stream()
                .map(phase -> phase.phase().getId())
                .filter(requestedPhaseIds::contains)
                .toList();
        if (employeePhaseIds.isEmpty()) {
            return List.of();
        }

        return taskRepo.findAssignedTasksByPhaseIdsAndDate(
                        employeePhaseIds, employee.getId(), selectedDate).stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_TASK_BY_ID')")
    public TaskResponse getTaskById(Long id) {
        User user = getUserByToken.get();
        Task task = taskHelper.getById(id, user);
        return toResponse(task);
    }

    @PreAuthorize("hasAuthority('UPDATE_MY_TASK_STATUS_BY_ID')")
    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        User user = getUserByToken.get();
        Task task = taskRepo.findForUpdateById(id)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        ProjectMember author = projectMemberHelper.findByProjectAndUser(task.getPhase().getProject(), user.getId(), user);

        taskHelper.checkDone(task.getPhase());
        taskAssignmentHelper.checkBelongToTask(task.getId(), author);
        taskHelper.checkEmployeeStatusTransition(task.getTaskStatus(), request.getTaskStatus());
        task.setTaskStatus(request.getTaskStatus());
        return toResponse(taskRepo.save(task));
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = taskMapper.toTaskResponse(task);
        response.setProgress(task.getAssignments().stream()
                .mapToInt(TaskAssignment::getContributionPercentage)
                .sum());
        return response;
    }

}
