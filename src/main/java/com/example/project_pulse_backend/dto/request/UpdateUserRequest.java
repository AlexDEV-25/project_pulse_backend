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
public class UpdateUserRequest {

    @NotNull(message = "USER_ID_MUST_NOT_BE_NULL")
    @Positive(message = "USER_ID_MUST_BE_POSITIVE")
    private Long id;

    @NotBlank(message = "USERNAME_MUST_NOT_BE_BLANK")
    @Size(max = 50, message = "USERNAME_MAX_LENGTH_EXCEEDED")
    private String userName;

    @NotBlank(message = "POSITION_MUST_NOT_BE_BLANK")
    @Size(max = 100, message = "POSITION_MAX_LENGTH_EN")
    private String position;

    @NotNull(message = "RESOURCE_RATE_MUST_NOT_BE_NULL")
    @DecimalMin(value = "0.0", inclusive = false, message = "RESOURCE_RATE_MUST_BE_POSITIVE")
    private BigDecimal resourceRate;

    @NotNull(message = "DEPARTMENT_ID_MUST_NOT_BE_NULL")
    @Positive(message = "DEPARTMENT_ID_MUST_BE_POSITIVE_EN")
    private Long departmentId;

    @NotEmpty(message = "ROLES_REQUIRED")
    private List<@NotBlank(message = "ROLE_REQUIRED") String> roles;

}
