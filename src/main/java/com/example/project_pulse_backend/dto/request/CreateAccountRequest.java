package com.example.project_pulse_backend.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Email phải đúng định dạng ví dụ: example@gmail.com")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt")
    private String password;

    @NotEmpty(message = "role không được để trống")
    private List<String> roles;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự")
    private String userName;

    @NotBlank(message = "Chức vụ không được để trống")
    @Size(max = 100, message = "Chức vụ không được vượt quá 100 ký tự")
    private String position;

    @NotNull(message = "Resource rate không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Resource rate không được nhỏ hơn 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Resource rate không được lớn hơn 100")
    private Float resourceRate;

    @NotNull(message = "DepartmentId không được để trống")
    @Positive(message = "DepartmentId phải là số dương")
    private Long departmentId;

}
