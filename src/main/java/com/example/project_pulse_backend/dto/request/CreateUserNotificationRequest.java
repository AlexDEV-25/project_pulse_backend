package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.entity.Notification;
import com.example.project_pulse_backend.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserNotificationRequest {

    @NotNull(message = "SENDER_REQUIRED")
    private User sender;

    @NotNull(message = "RECEIVER_REQUIRED")
    private User receiver;

    @NotNull(message = "NOTIFICATION_REQUIRED")
    private Notification notification;


}
