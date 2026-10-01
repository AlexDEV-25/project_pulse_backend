package com.example.project_pulse_backend.dto.response;

import com.example.project_pulse_backend.constant.TaskNoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskNoteResponse {
    private Long id;
    private Long taskId;
    private Long employeeId;
    private String employeeName;
    private String content;
    private Integer workingHours;
    private LocalDateTime workingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TaskNoteStatus taskNoteStatus;
}
