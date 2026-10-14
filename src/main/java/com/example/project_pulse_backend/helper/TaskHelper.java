package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskHelper {
    private final TaskRepo taskRepo;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;

    public List<Task> getProjectById(Long projectId, User user, Long phaseId, String keyword, Long userId) {
        Project project = projectHelper.getProjectById(projectId, user);
        return taskRepo.findByProjectIdAndFilters(project.getId(), phaseId, keyword, userId);
    }

    public Task getById(Long Id, User user) {
        Task task = taskRepo.findById(Id).orElseThrow(
                () -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        projectHelper.checkAccessToProject(task.getPhase().getProject().getId(), user);
        return task;
    }

    public void checkDone(Project project, Phase phase) {
        projectHelper.checkProjectNotDone(project);
        phaseHelper.checkPhaseNotDone(phase);
    }

    public void checkSameProject(Project project, Phase phase) {
        if (!project.getId().equals(phase.getProject().getId())) {
            throw AppException.builder().appError(AppError.PROJECT_MISMATCH).build();
        }
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
}
