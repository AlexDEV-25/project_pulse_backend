package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.NotificationType;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.CreateUserNotificationRequest;
import com.example.project_pulse_backend.dto.request.NotificationRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import com.example.project_pulse_backend.repository.TaskCostTransactionRepo;
import com.example.project_pulse_backend.repository.TaskNoteRepo;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskHelper {
    private final TaskRepo taskRepo;
    private final TaskNoteRepo taskNoteRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;
    private final NotificationHelper notificationHelper;

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

    public void assignTaskNotification(Task newTask, User pm) {
        if (pm == null || newTask.getAssignments() == null || newTask.getAssignments().isEmpty()) {
            return;
        }
        Notification notification = createAssignTaskNotification(newTask);

        for (TaskAssignment taskAssignment : newTask.getAssignments()) {
            if (taskAssignment.getEmployee() != null && taskAssignment.getEmployee().getUser() != null) {
                notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                        .sender(pm)
                        .receiver(taskAssignment.getEmployee().getUser())
                        .notification(notification)
                        .build());
            }
        }
    }

    private Notification createAssignTaskNotification(Task newTask) {
        Long projectId = newTask.getPhase() != null && newTask.getPhase().getProject() != null
                ? newTask.getPhase().getProject().getId() : null;
        return notificationHelper.createNotification(NotificationRequest.builder()
                .content(notificationHelper.assignTaskNotification(newTask.getTaskName()))
                .link(notificationHelper.linkToTask(projectId, newTask.getId()))
                .type(NotificationType.INFO)
                .build());
    }

    private Notification createRemoveFromTaskNotification(Task task) {
        Long projectId = task.getPhase() != null && task.getPhase().getProject() != null
                ? task.getPhase().getProject().getId() : null;
        return notificationHelper.createNotification(NotificationRequest.builder()
                .content(notificationHelper.removeFromTaskNotification(task.getTaskName()))
                .link(notificationHelper.linkToTask(projectId, task.getId()))
                .type(NotificationType.INFO)
                .build());
    }

    public void checkDifferentTaskAssignment(Task task, User pm, List<TaskAssignment> oldTaskAssignments, List<TaskAssignment> newTaskAssignments) {
        if (pm == null || task == null) {
            return;
        }
        List<TaskAssignment> oldList = oldTaskAssignments != null ? oldTaskAssignments : Collections.emptyList();
        List<TaskAssignment> newList = newTaskAssignments != null ? newTaskAssignments : Collections.emptyList();

        Set<Long> oldEmployeeIds = oldList.stream()
                .filter(a -> a.getEmployee() != null && a.getEmployee().getId() != null)
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        Set<Long> newEmployeeIds = newList.stream()
                .filter(a -> a.getEmployee() != null && a.getEmployee().getId() != null)
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        List<TaskAssignment> removed = oldList.stream()
                .filter(a -> a.getEmployee() != null && !newEmployeeIds.contains(a.getEmployee().getId()))
                .toList();

        if (!removed.isEmpty()) {
            Notification removeNotification = createRemoveFromTaskNotification(task);
            for (TaskAssignment taskAssignment : removed) {
                if (taskAssignment.getEmployee() != null && taskAssignment.getEmployee().getUser() != null) {
                    notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                            .sender(pm)
                            .receiver(taskAssignment.getEmployee().getUser())
                            .notification(removeNotification)
                            .build());
                }
            }
        }

        List<TaskAssignment> added = newList.stream()
                .filter(a -> a.getEmployee() != null && !oldEmployeeIds.contains(a.getEmployee().getId()))
                .toList();

        if (!added.isEmpty()) {
            Notification assignNotification = createAssignTaskNotification(task);
            for (TaskAssignment taskAssignment : added) {
                if (taskAssignment.getEmployee() != null && taskAssignment.getEmployee().getUser() != null) {
                    notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                            .sender(pm)
                            .receiver(taskAssignment.getEmployee().getUser())
                            .notification(assignNotification)
                            .build());
                }
            }
        }
    }

    public void checkDifferentTaskAssignment(List<TaskAssignment> oldTaskAssignments, List<TaskAssignment> newTaskAssignments) {
        Task task = null;
        User pm = null;
        if (oldTaskAssignments != null && !oldTaskAssignments.isEmpty()) {
            task = oldTaskAssignments.getFirst().getTask();
        } else if (newTaskAssignments != null && !newTaskAssignments.isEmpty()) {
            task = newTaskAssignments.getFirst().getTask();
        }
        if (task != null && task.getPhase() != null && task.getPhase().getProject() != null) {
            pm = task.getPhase().getProject().getPm();
        }
        checkDifferentTaskAssignment(task, pm, oldTaskAssignments, newTaskAssignments);
    }
}
