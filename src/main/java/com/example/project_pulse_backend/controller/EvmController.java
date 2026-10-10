package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.EvmHealthResponse;
import com.example.project_pulse_backend.service.EvmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evm")
@RequiredArgsConstructor
public class EvmController {
    private final EvmService evmService;

    @GetMapping("/project/{projectId}")
    public APIResponse<EvmHealthResponse> getProjectEvmHealth(@PathVariable Long projectId) {
        return APIResponse.<EvmHealthResponse>builder()
                .result(evmService.getProjectHealth(projectId))
                .build();
    }

    @GetMapping("/phase/{phaseId}")
    public APIResponse<EvmHealthResponse> getPhaseEvmHealth(@PathVariable Long phaseId) {
        return APIResponse.<EvmHealthResponse>builder()
                .result(evmService.getPhaseHealth(phaseId))
                .build();
    }
}
