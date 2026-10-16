package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskHelper {
    private final TaskRepo taskRepo;
    private final TaskNoteRepo taskNoteRepo;
    private final TaskAssignmentRepo taskAssignmentRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;

    public List<Task> getProjectById(Long projectId, User user, Long phaseId, String keyword, Long userId) {
        Project project = projectHelper.getProjectById(projectId, user);
        return taskRepo.filterTask(project.getId(), phaseId, keyword, userId);
    }

    public Task getById(Long Id, User user) {
        Task task = taskRepo.findById(Id).orElseThrow(
                () -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        projectHelper.checkAccessToProject(task.getPhase().getProject().getId(), user);
        return task;
    }

    public void checkDone(Phase phase) {
        projectHelper.checkProjectNotDone(phase.getProject());
        phaseHelper.checkPhaseNotDone(phase);
    }

    public void checkTime(Phase phase, LocalDate startAt, LocalDate endAt) {
        TimeHelper.checkStartBeforeEnd(startAt, endAt);
        TimeHelper.checkTaskInPhase(startAt, endAt, phase.getStartAt(), phase.getEndAt());
    }

    public void checkEmployeeStatusTransition(TaskStatus currentStatus, TaskStatus requestedStatus) {
        if (currentStatus != TaskStatus.TODO || requestedStatus != TaskStatus.IN_PROGRESS) {
            throw AppException.builder().appError(AppError.INVALID_TASK_STATUS_TRANSITION).build();
        }
    }

    public void checkBeforeDelete(Long taskId) {
        if (!taskNoteRepo.findByTask_Id(taskId).isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_IN_USE).build();
        }
        if (!taskCostTransactionRepo.findByTask_Id(taskId).isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_IN_USE).build();
        }
        if (!earningTransactionRepo.findByTask_Id(taskId).isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_IN_USE).build();
        }
    }

    public void checkUnconfirmedTaskNotes(Task task) {
        if (taskNoteRepo.existsByTask_IdAndTaskNoteStatus(task.getId(), TaskNoteStatus.PENDING_CONFIRMATION)) {
            throw new AppException(AppError.HAS_TASK_NOTE_UNCONFIRMED);
        }
    }
}
