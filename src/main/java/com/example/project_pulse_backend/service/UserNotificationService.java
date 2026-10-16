package com.example.project_pulse_backend.service;


import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.response.UserNotificationResponse;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.entity.UserNotification;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.repository.UserNotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final GetUserByToken getUserByToken;

    @PreAuthorize("hasAuthority('GET_ALL_USER_NOTIFICATION')")
    public List<UserNotificationResponse> getByReceiver() {
        User receiver = getUserByToken.get();
        List<UserNotification> userNotifications = userNotificationRepository
                .findByReceiver_IdOrderByCreatedAtDescIdDesc(receiver.getId());
        return userNotifications.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_UNREAD_USER_NOTIFICATION')")
    public List<UserNotificationResponse> getByReceiverIdAndReadFalse() {
        User receiver = getUserByToken.get();
        List<UserNotification> userNotifications = userNotificationRepository
                .findByReceiver_IdAndReadFalseOrderByCreatedAtDescIdDesc(receiver.getId());
        return userNotifications.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('READ_NOTIFICATION')")
    @Transactional
    public UserNotificationResponse read(Long id) {
        User receiver = getUserByToken.get();
        UserNotification entity = userNotificationRepository.findByIdAndReceiver_Id(id, receiver.getId())
                .orElseThrow(() -> new AppException(AppError.NOTIFICATION_NOT_FOUND));
        if (!entity.isRead()) {
            entity.setRead(true);
            entity = userNotificationRepository.save(entity);
        }
        return toResponse(entity);
    }

    @PreAuthorize("hasAuthority('READ_ALL_NOTIFICATION')")
    @Transactional
    public void readAll() {
        User receiver = getUserByToken.get();
        List<UserNotification> entities = userNotificationRepository.findByReceiver_IdAndReadFalse(receiver.getId());
        for (UserNotification userNotification : entities) {
            userNotification.setRead(true);
        }
        userNotificationRepository.saveAll(entities);
    }

    @PreAuthorize("hasAuthority('READ_ALL_NOTIFICATION')")
    @Transactional
    public void readAll(Long id) {
        readAll();
    }

    private UserNotificationResponse toResponse(UserNotification userNotification) {
        return UserNotificationResponse.builder()
                .id(userNotification.getId())
                .senderId(userNotification.getSender().getId())
                .senderName(userNotification.getSender().getUserName())
                .receiverId(userNotification.getReceiver().getId())
                .receiverName(userNotification.getReceiver().getUserName())
                .notificationId(userNotification.getNotification().getId())
                .notificationContent(userNotification.getNotification().getContent())
                .notificationLink(userNotification.getNotification().getLink())
                .notificationType(userNotification.getNotification().getType())
                .read(userNotification.isRead())
                .createdAt(userNotification.getCreatedAt())
                .build();
    }
}
