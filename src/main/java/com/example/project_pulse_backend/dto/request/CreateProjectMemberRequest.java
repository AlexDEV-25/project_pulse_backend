package com.example.project_pulse_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectMemberRequest {
    @NotNull(message = "Project_ID khong duoc de trong")
    private Long projectId;

    @NotNull(message = "User_ID khong duoc de trong")
    private Long userId;
}
