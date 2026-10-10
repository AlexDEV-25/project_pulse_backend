package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.TaskCostTransactionResponse;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.TaskCostTransaction;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.TaskCostTransactionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCostTransactionService {
    private final GetUserByToken getUserByToken;
    private final TaskCostTransactionHelper taskCostTransactionHelper;

    @PreAuthorize("hasAuthority('GET_TASK_COSTS_BY_PROJECT')")
    public List<TaskCostTransactionResponse> getByProject(Long projectId, String keyword, Long phaseId) {
        User user = getUserByToken.get();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        return taskCostTransactionHelper.getByProjectId(projectId, user, normalizedKeyword, phaseId)
                .stream().map(this::toResponse).toList();
    }
    
    @PreAuthorize("hasAuthority('GET_TASK_COSTS_BY_ID')")
    public TaskCostTransactionResponse getById(Long id) {
        User user = getUserByToken.get();
        return toResponse(taskCostTransactionHelper.getById(id, user));
    }


    private TaskCostTransactionResponse toResponse(TaskCostTransaction transaction) {
        Task task = transaction.getTask();
        User employee = transaction.getExecutor().getUser();
        return TaskCostTransactionResponse.builder()
                .id(transaction.getId())
                .projectId(task.getPhase().getProject().getId())
                .phaseId(task.getPhase().getId())
                .phaseName(task.getPhase().getPhaseName())
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .employeeId(employee.getId())
                .employeeName(employee.getUserName())
                .points(transaction.getPoints())
                .workdays(transaction.getWorkdays())
                .reason(transaction.getReason())
                .build();
    }
}
