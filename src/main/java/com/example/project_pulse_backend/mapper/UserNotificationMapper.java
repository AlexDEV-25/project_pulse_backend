package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.UserNotificationResponse;
import com.example.project_pulse_backend.entity.UserNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserNotificationMapper {
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.userName")
    @Mapping(target = "receiverId", source = "receiver.id")
    @Mapping(target = "receiverName", source = "receiver.userName")
    @Mapping(target = "notificationId", source = "notification.id")
    @Mapping(target = "notificationContent", source = "notification.content")
    @Mapping(target = "notificationLink", source = "notification.link")
    @Mapping(target = "notificationType", source = "notification.type")
    UserNotificationResponse toUserNotificationResponse(UserNotification notification);
}
