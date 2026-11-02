package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.dto.request.CreateUserNotificationRequest;
import com.example.project_pulse_backend.dto.request.NotificationRequest;
import com.example.project_pulse_backend.entity.Notification;
import com.example.project_pulse_backend.entity.UserNotification;
import com.example.project_pulse_backend.mapper.NotificationMapper;
import com.example.project_pulse_backend.repository.NotificationRepository;
import com.example.project_pulse_backend.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationHelper {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationMapper notificationMapper;

    public Notification createNotification(NotificationRequest request) {

        Notification notification = notificationMapper.toNotification(request);

        return notificationRepository.save(notification);
    }

    public void createUserNotification(CreateUserNotificationRequest request) {

        UserNotification notification = notificationMapper.toUserNotification(request);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        userNotificationRepository.save(notification);
    }

    private String formatPoints(Double point) {
        if (point == null) return "0";
        if (point == Math.floor(point)) {
            return String.format(java.util.Locale.US, "%.0f", point);
        }
        return String.format(java.util.Locale.US, "%.2f", point).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public String createTaskNoteNotification(String employeeName, String taskName) {
        return String.format("%s vừa tạo 1 task note trong task %s ", employeeName, taskName);
    }

    public String getPointFormTaskNotification(Double point, String taskName) {
        return String.format("Bạn vừa nhận được %s point từ task %s ", formatPoints(point), taskName);
    }

    public String assignTaskNotification(String taskName) {
        return String.format("Bạn vừa được giao task %s ", taskName);
    }

    public String transferPointFormTaskNotification(Double point, String taskName, String employeeName) {
        return String.format("Bạn vừa bị chuyển %s point từ task %s sang cho %s ", formatPoints(point), taskName, employeeName);
    }

    public String getTransferFormTaskNotification(Double point, String taskName, String employeeName) {
        return String.format("Bạn vừa được chuyển cho %s point của %s từ task %s ", formatPoints(point), employeeName, taskName);
    }

    public String removeFromTaskNotification(String taskName) {
        return String.format("Bạn đã được loại bỏ khỏi task %s ", taskName);
    }

    public String linkToTask(Long projectId, Long taskId) {
        if (projectId == null) {
            return "/projects?tab=tasks";
        }
        if (taskId == null) {
            return String.format("/projects/%d?tab=tasks", projectId);
        }
        return String.format("/projects/%d?tab=tasks&taskId=%d", projectId, taskId);
    }

    public String linkToEarn(Long projectId, Long transactionId) {
        if (projectId == null) {
            return "/projects?tab=earnings";
        }
        if (transactionId == null) {
            return String.format("/projects/%d?tab=earnings", projectId);
        }
        return String.format("/projects/%d?tab=earnings&transactionId=%d", projectId, transactionId);
    }

    public String linkToTransfer(Long projectId, Long transactionId) {
        if (projectId == null) {
            return "/projects?tab=earnings&type=TRANSFER";
        }
        if (transactionId == null) {
            return String.format("/projects/%d?tab=earnings&type=TRANSFER", projectId);
        }
        return String.format("/projects/%d?tab=earnings&type=TRANSFER&transactionId=%d", projectId, transactionId);
    }
}
