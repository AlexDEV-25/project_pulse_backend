package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.dto.request.CreateEarningTransferRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.EarningTransactionResponse;
import com.example.project_pulse_backend.service.EarningTransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/earning-transactions")
@AllArgsConstructor
public class EarningTransactionController {
    private EarningTransactionService earningTransactionService;

    @GetMapping("/project/{projectId}")
    public APIResponse<EarningTransactionResponse> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long phaseId,
            @RequestParam(required = false) EarningTransactionType earningTransactionType) {
        return APIResponse.<EarningTransactionResponse>builder()
                .resultList(earningTransactionService.getByProject(
                        projectId, keyword, phaseId, earningTransactionType))
                .build();
    }

    @GetMapping("/project/{projectId}/{id}")
    public APIResponse<EarningTransactionResponse> getById(
            @PathVariable Long projectId, @PathVariable Long id) {
        return APIResponse.<EarningTransactionResponse>builder()
                .result(earningTransactionService.getById(id, projectId))
                .build();
    }


    @PostMapping("/project/{projectId}/transfer")
    public APIResponse<EarningTransactionResponse> transfer(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateEarningTransferRequest request) {
        return APIResponse.<EarningTransactionResponse>builder()
                .result(earningTransactionService.transfer(projectId, request))
                .build();
    }
}
