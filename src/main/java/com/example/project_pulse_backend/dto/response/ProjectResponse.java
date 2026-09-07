package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String projectName;
    private Long pmId;
    private String pmName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal clientBudget;
    private BigDecimal projectBudget;
    private String description;
    private ProjectStatus projectStatus;
}
