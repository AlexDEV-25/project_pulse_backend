package com.example.project_pulse_backend.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayRequest {

    @NotNull(message = "enabled không được để trống")
    private boolean enabled;
}
