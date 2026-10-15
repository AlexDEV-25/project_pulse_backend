package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    @Email(message = "EMAIL_INVALID")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "EMAIL_EXAMPLE_FORMAT")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_FORMAT_INVALID")
    private String password;

    @NotEmpty(message = "ROLE_LIST_REQUIRED")
    private List<String> roles;

    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, max = 50, message = "USERNAME_LENGTH_INVALID")
    private String userName;

    @NotBlank(message = "POSITION_REQUIRED")
    @Size(max = 100, message = "POSITION_MAX_LENGTH_EXCEEDED")
    private String position;

    @NotNull(message = "RESOURCE_RATE_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = true, message = "RESOURCE_RATE_BELOW_ZERO")
    @DecimalMax(value = "100.0", inclusive = true, message = "RESOURCE_RATE_ABOVE_100")
    private BigDecimal resourceRate;

    @NotNull(message = "DEPARTMENT_ID_REQUIRED")
    @Positive(message = "DEPARTMENT_ID_MUST_BE_POSITIVE")
    private Long departmentId;

}
