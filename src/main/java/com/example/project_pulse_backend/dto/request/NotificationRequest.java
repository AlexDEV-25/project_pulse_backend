package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "CONTENT_REQUIRED")
    private String content;
	
    private String link;

    @NotNull(message = "NOTIFICATION_TYPE_REQUIRED")
    private NotificationType type;
}
