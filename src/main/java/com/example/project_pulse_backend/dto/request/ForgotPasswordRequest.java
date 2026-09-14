package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Email phải đúng định dạng ví dụ: example@gmail.com")
    private String email;

    @NotBlank(message = "forgotPasswordCode không được để trống")
    private String forgotPasswordCode;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt")
    private String password;
}
