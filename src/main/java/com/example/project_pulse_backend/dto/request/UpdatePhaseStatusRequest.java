package com.example.project_pulse_backend.dto.request;

import com.example.project_pulse_backend.constant.AllocationPlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePhaseStatusRequest {
    @NotNull(message = "Trang thai phase khong duoc de trong")
    private AllocationPlanStatus status;
}
