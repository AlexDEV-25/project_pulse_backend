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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class EmailHelper {
    private final EmailClient emailClient;

    @Value("${app.domain.frontend}")
    private String frontendDomain;

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


    public String bodyChangePassword(String email, String forgotPassWordCode) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String url = frontendDomain + "/change-password/" + encodedEmail + "/" + forgotPassWordCode;

        return """
                <html><body>
                <h2 style='color:#4A90E2'>Đặt lại mật khẩu</h2>
                <p>Vui lòng sử dụng mã sau:</p>
                <h1 style='color:green'>%s</h1>
                <p>Bấm vào link để kích hoạt:</p>
                <a href='%s'>Nhấn vào đây để đặt lại mật khẩu</a>
                </body></html>
                """.formatted(forgotPassWordCode, url);

    }

    public String bodyLockAccount() {

        return """
                <html><body>
                <h2 style='color:#D0021B'>Thông báo khóa tài khoản</h2>
                <p>Tài khoản của bạn đã bị khóa vì lý do bảo mật.</p>
                <p>Nếu bạn có thắc mắc, vui lòng liên hệ ngay với bộ phận hỗ trợ thông qua số 0987654321.</p>
                </body></html>
                """;


    }

    public String bodyUnLockAccount() {
        return """ 
                <html><body>
                <h2 style='color:#D0021B'>Thông báo mở lại tài khoản</h2>
                <p>Tài khoản của bạn đã được mở lại.</p>
                </body></html>
                """;
    }
}