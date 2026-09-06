package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithoutCostResponse {
    private Long id;
    private String userName;
    private String position;
    private String avatarUrl;
    private String departmentName;
}
