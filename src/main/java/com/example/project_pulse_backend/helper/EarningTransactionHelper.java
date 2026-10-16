package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.NotificationType;
import com.example.project_pulse_backend.dto.request.CreateUserNotificationRequest;
import com.example.project_pulse_backend.dto.request.NotificationRequest;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EarningTransactionHelper {
    private final EarningTransactionRepo earningTransactionRepo;
    private final ProjectHelper projectHelper;
    private final NotificationHelper notificationHelper;

    public List<EarningTransaction> getByProjectId(
            Long projectId, User user,
            String keyword, Long phaseId,
            EarningTransactionType earningTransactionType) {
        Project project = projectHelper.getProjectById(projectId, user);
        return earningTransactionRepo.filterEarningTransaction(
                project.getId(), phaseId, earningTransactionType, keyword);
    }

    public EarningTransaction getById(Long id, Long projectId, User user) {
        projectHelper.checkAccessToProject(projectId, user);
        return earningTransactionRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.EARNING_NOT_FOUND).build());
    }

    public EarningTransaction createEarningTransaction(Task task, TaskAssignment assignment, ProjectMember employee) {
        BigDecimal earnedPoints = task.getTaskPoint()
                .multiply(BigDecimal.valueOf(assignment.getContributionPercentage()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return earningTransactionRepo.save(EarningTransaction.builder()
                .task(task)
                .executor(employee)
                .points(earnedPoints)
                .earningTransactionType(EarningTransactionType.EARN)
                .reason("Hoàn thành task: " + task.getTaskName())
                .build());
    }


    public void checkAvailablePoint(EarningTransaction source, BigDecimal points) {
        BigDecimal transferredPoints = totalTransferPoint(source);
        BigDecimal availablePoints = source.getPoints().subtract(transferredPoints);

        if (points.compareTo(availablePoints) > 0) {
            throw AppException.builder().appError(AppError.EARNING_BALANCE_NOT_ENOUGH).build();
        }
    }


    public BigDecimal totalTransferPoint(EarningTransaction source) {
        List<EarningTransaction> previousTransfers =
                earningTransactionRepo.findByReferenceTransaction_IdAndEarningTransactionType(
                        source.getId(), EarningTransactionType.TRANSFER);

        return previousTransfers.stream()
                .map(EarningTransaction::getPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void getTransferFormTaskNotification(EarningTransaction source, EarningTransaction newTransfer) {
        Long projectId = newTransfer.getTask() != null
                && newTransfer.getTask().getPhase() != null
                && newTransfer.getTask().getPhase().getProject() != null
                ? newTransfer.getTask().getPhase().getProject().getId() : null;
        Double points = newTransfer.getPoints() != null ? newTransfer.getPoints().doubleValue() : 0.0;
        User pm = source.getTask() != null
                && source.getTask().getPhase() != null
                && source.getTask().getPhase().getProject() != null
                ? source.getTask().getPhase().getProject().getPm() : null;

        Notification notification = notificationHelper.createNotification(NotificationRequest.builder()
                .content(notificationHelper.getTransferFormTaskNotification(
                        points, newTransfer.getTask().getTaskName(), source.getExecutor().getUser().getUserName()))
                .link(notificationHelper.linkToTransfer(projectId))
                .type(NotificationType.INFO)
                .build());

        if (pm != null && newTransfer.getExecutor() != null && newTransfer.getExecutor().getUser() != null) {
            notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                    .sender(pm)
                    .receiver(newTransfer.getExecutor().getUser())
                    .notification(notification)
                    .build());
        }
    }

    public void transferPointFormTaskNotification(EarningTransaction source, EarningTransaction newTransfer) {
        Long projectId = newTransfer.getTask() != null
                && newTransfer.getTask().getPhase() != null
                && newTransfer.getTask().getPhase().getProject() != null
                ? newTransfer.getTask().getPhase().getProject().getId() : null;
        Double points = newTransfer.getPoints() != null ? newTransfer.getPoints().doubleValue() : 0.0;
        User pm = newTransfer.getTask() != null
                && newTransfer.getTask().getPhase() != null
                && newTransfer.getTask().getPhase().getProject() != null
                ? newTransfer.getTask().getPhase().getProject().getPm() : null;

        Notification notification = notificationHelper.createNotification(NotificationRequest.builder()
                .content(notificationHelper.transferPointFormTaskNotification(
                        points, newTransfer.getTask().getTaskName(), newTransfer.getExecutor().getUser().getUserName()))
                .link(notificationHelper.linkToTransfer(projectId))
                .type(NotificationType.INFO)
                .build());

        if (pm != null && source != null && source.getExecutor() != null && source.getExecutor().getUser() != null) {
            notificationHelper.createUserNotification(CreateUserNotificationRequest.builder()
                    .sender(pm)
                    .receiver(source.getExecutor().getUser())
                    .notification(notification)
                    .build());
        }
    }

    public void transferPointFormTaskNotification(EarningTransaction newTransfer) {
        if (newTransfer != null && newTransfer.getReferenceTransaction() != null) {
            transferPointFormTaskNotification(newTransfer.getReferenceTransaction(), newTransfer);
        }
    }
}
