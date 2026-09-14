package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.ProjectMemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectMemberStatusRequest {
    @NotNull(message = "Trang thai project member khong duoc de trong")
    private ProjectMemberStatus status;
}
