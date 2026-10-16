package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Long notificationId;
    private String notificationContent;
    private String notificationLink;
    private NotificationType notificationType;
    private boolean read;
    private LocalDateTime createdAt;
}
