package com.example.project_pulse_backend.helper;


import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.EmailRequest;
import com.example.project_pulse_backend.dto.request.SendEmailRequest;
import com.example.project_pulse_backend.dto.request.Sender;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.httpClient.EmailClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailHelper {
    private final EmailClient emailClient;

    @Value("${app.email.api-key}")
    @NonFinal
    String apiKey;

    @Value("${app.email.sender-email}")
    @NonFinal
    String senderEmail;

    @Value("${app.email.sender-name}")
    @NonFinal
    String senderName;

    public void sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name(senderName)
                        .email(senderEmail)
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e) {
            throw new AppException(AppError.CANNOT_SEND_EMAIL);
        }
    }
}