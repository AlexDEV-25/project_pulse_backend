package com.example.project_pulse_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String userName;
    private BigDecimal userResourceRate;
    private String userPosition;
}
