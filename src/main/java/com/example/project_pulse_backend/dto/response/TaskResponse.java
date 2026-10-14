package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long phaseId;
    private String phaseName;
    private String taskName;
    private String description;
    private LocalDate startAt;
    private LocalDate endAt;
    private TaskStatus taskStatus;
    private BigDecimal taskPoint;
    private Integer progress;
    private List<TaskAssignmentResponse> assignments;
}
