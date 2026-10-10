package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskStatusRequest {
    @NotNull(message = "Trạng thái task không được để trống")
    private TaskStatus taskStatus;
}
