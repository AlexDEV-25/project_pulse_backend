package com.example.project_pulse_backend.controller;


import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.UserNotificationResponse;
import com.example.project_pulse_backend.service.UserNotificationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-notifications")
@AllArgsConstructor
public class UserNotificationController {
    private final UserNotificationService userNotificationService;

    @GetMapping("/receiver")
    public APIResponse<UserNotificationResponse> getByReceiver() {
        return APIResponse.<UserNotificationResponse>builder()
                .resultList(userNotificationService.getByReceiver()).build();
    }

    @GetMapping("/receiver/unread")
    public APIResponse<UserNotificationResponse> getByReceiverIdAndReadFalse() {
        return APIResponse.<UserNotificationResponse>builder()
                .resultList(userNotificationService.getByReceiverIdAndReadFalse()).build();
    }

    @PutMapping("/read/{id}")
    public APIResponse<UserNotificationResponse> read(@PathVariable Long id) {
        return APIResponse.<UserNotificationResponse>builder()
                .result(userNotificationService.read(id)).build();
    }

    @PutMapping("/read-all")
    public APIResponse<Void> readAll() {
        userNotificationService.readAll();
        return APIResponse.<Void>builder().build();
    }

    @PutMapping("/read-all/{id}")
    public APIResponse<Void> readAllById(@PathVariable Long id) {
        userNotificationService.readAll();
        return APIResponse.<Void>builder().build();
    }
}
