package com.example.project_pulse_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private Long authId;
    private String email;
    private String userName;
    private String position;
    private BigDecimal resourceRate;
    private String avatarUrl;
    private Long departmentId;
    private String departmentName;
    private List<RoleResponse> roles;
}
