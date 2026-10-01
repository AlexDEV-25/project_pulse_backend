package com.example.project_pulse_backend.dto.response;

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
public class UserResponse {
    private Long id;
    private Long authId;
    private boolean enabled;
    private String email;
    private String userName;
    private String position;
    private BigDecimal resourceRate;
    private String avatarUrl;
    private String departmentName;
    private List<RoleResponse> roles;
}
