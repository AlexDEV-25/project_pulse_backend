package com.example.project_pulse_backend.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CreateBodyEmailHelper {

    @Value("${app.domain.frontend}")
    private String frontendDomain;


    public String bodyChangePassword(String email, String forgotPassWordCode) {
        String url = frontendDomain + "/change-password/" + email + "/" + forgotPassWordCode;

        return """
                <html><body>
                <h2 style='color:#4A90E2'>Đặt lại mật khẩu</h2>"
                <p>Vui lòng sử dụng mã sau:</p>"
                <h1 style='color:green'>" + forgotPassWordCode + "</h1>"
                <p>Bấm vào link để kích hoạt:</p>"
                <a href='" + url + "'>Nhấn vào đây để đặt lại mật khẩu</a>"
                </body></html>
                """;

    }

    public String bodyLockAccount() {

        return """
                <html><body>
                <h2 style='color:#D0021B'>Thông báo khóa tài khoản</h2>"
                <p>Tài khoản của bạn đã bị khóa vì lý do bảo mật.</p>"
                <p>Nếu bạn có thắc mắc, vui lòng liên hệ ngay với bộ phận hỗ trợ thông qua số 0987654321.</p>"
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
