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

    @NotNull(message = "User ID must not be null")
    @Positive(message = "User ID must be positive")
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String userName;

    @NotBlank(message = "Position must not be blank")
    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;

    @NotNull(message = "Resource rate must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Resource rate must be greater than 0")
    private BigDecimal resourceRate;

    @NotNull(message = "Department ID must not be null")
    @Positive(message = "Department ID must be positive")
    private Long departmentId;

    @NotEmpty(message = "Roles must not be empty")
    private List<@NotBlank(message = "Role must not be blank") String> roles;

}
