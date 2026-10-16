package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.dto.request.CreateUserNotificationRequest;
import com.example.project_pulse_backend.dto.request.NotificationRequest;
import com.example.project_pulse_backend.entity.Notification;
import com.example.project_pulse_backend.entity.UserNotification;
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

    public Notification createNotification(NotificationRequest request) {

        Notification notification = Notification.builder()
                .content(request.getContent())
                .link(request.getLink())
                .type(request.getType())
                .build();

        return notificationRepository.save(notification);
    }


    public void createUserNotification(CreateUserNotificationRequest request) {

        UserNotification notification = UserNotification.builder()
                .sender(request.getSender())
                .receiver(request.getReceiver())
                .notification(request.getNotification())
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();
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

    public String linkToTask() {
        return "/projects?tab=tasks";
    }

    public String linkToTask(Long projectId, Long taskId) {
        if (projectId == null) {
            return linkToTask();
        }
        if (taskId == null) {
            return String.format("/projects/%d?tab=tasks", projectId);
        }
        return String.format("/projects/%d?tab=tasks&taskId=%d", projectId, taskId);
    }

    public String linkToEarn() {
        return "/projects?tab=earnings";
    }

    public String linkToEarn(Long projectId) {
        if (projectId == null) {
            return linkToEarn();
        }
        return String.format("/projects/%d?tab=earnings", projectId);
    }

    public String linkToTransfer() {
        return "/projects?tab=earnings&type=TRANSFER";
    }

    public String linkToTransfer(Long projectId) {
        if (projectId == null) {
            return linkToTransfer();
        }
        return String.format("/projects/%d?tab=earnings&type=TRANSFER", projectId);
    }
}
