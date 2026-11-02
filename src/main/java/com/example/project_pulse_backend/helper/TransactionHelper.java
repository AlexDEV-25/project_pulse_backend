package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.NotificationType;
import com.example.project_pulse_backend.dto.request.CreateUserNotificationRequest;
import com.example.project_pulse_backend.dto.request.NotificationRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHelper {
    private final TaskNoteHelper taskNoteHelper;
    private final EarningTransactionHelper earningTransactionHelper;
    private final TaskCostTransactionHelper taskCostTransactionHelper;
    private final NotificationHelper notificationHelper;

    public void createCompletionTransactions(Task task) {
        if (task.getAssignments() == null || task.getAssignments().isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_HAS_NO_ASSIGNMENTS).build();
        }

        for (TaskAssignment assignment : task.getAssignments()) {
            ProjectMember employee = assignment.getEmployee();
            List<TaskNote> confirmedNotes = taskNoteHelper.checkTaskNotesExist(task, employee);

            EarningTransaction newEarningTransaction = earningTransactionHelper.createEarningTransaction(task, assignment, employee);
            this.getPointFormTaskNotification(newEarningTransaction);

            taskCostTransactionHelper.createTaskCostTransaction(task, employee, confirmedNotes);
        }
    }

    private void getPointFormTaskNotification(EarningTransaction newEarningTransaction) {
        Long projectId = newEarningTransaction.getTask() != null
                && newEarningTransaction.getTask().getPhase() != null
                && newEarningTransaction.getTask().getPhase().getProject() != null
                ? newEarningTransaction.getTask().getPhase().getProject().getId() : null;
        Double points = newEarningTransaction.getPoints() != null
                ? newEarningTransaction.getPoints().doubleValue() : 0.0;
        User pm = newEarningTransaction.getTask() != null
                && newEarningTransaction.getTask().getPhase() != null
                && newEarningTransaction.getTask().getPhase().getProject() != null
                ? newEarningTransaction.getTask().getPhase().getProject().getPm() : null;

        Notification notification = notificationHelper.createNotification(NotificationRequest.builder()
                .content(notificationHelper.getPointFormTaskNotification(
                        points, newEarningTransaction.getTask().getTaskName()))
                .link(notificationHelper.linkToEarn(projectId, newEarningTransaction.getId()))
                .type(NotificationType.INFO)
                .build());

        if (pm != null && newEarningTransaction.getExecutor() != null && newEarningTransaction.getExecutor().getUser() != null) {
            notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                    .sender(pm)
                    .receiver(newEarningTransaction.getExecutor().getUser())
                    .notification(notification)
                    .build());
        }
    }
}
