package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.service.EvmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/project/{projectId}/phases")
    public APIResponse<PhaseEvmDetailResponse> getProjectPhasesEvm(@PathVariable Long projectId) {
        return APIResponse.<PhaseEvmDetailResponse>builder()
                .resultList(evmService.getProjectPhasesEvm(projectId))
                .build();
    }

    @GetMapping("/project/{projectId}/employees")
    public APIResponse<EmployeeEvmPerformanceResponse> getProjectEmployeesEvm(@PathVariable Long projectId) {
        return APIResponse.<EmployeeEvmPerformanceResponse>builder()
                .resultList(evmService.getProjectEmployeesEvm(projectId))
                .build();
    }

    @GetMapping("/project/{projectId}/financial-summary")
    public APIResponse<ProjectEvmFinancialResponse> getProjectFinancialSummary(@PathVariable Long projectId) {
        return APIResponse.<ProjectEvmFinancialResponse>builder()
                .result(evmService.getProjectFinancialSummary(projectId))
                .build();
    }

    @GetMapping(value = "/project/{projectId}/export-csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportEvmCsvReport(@PathVariable Long projectId) {
        byte[] csvData = evmService.exportEvmCsvReport(projectId);
        String filename = "evm_report_project_" + projectId + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
