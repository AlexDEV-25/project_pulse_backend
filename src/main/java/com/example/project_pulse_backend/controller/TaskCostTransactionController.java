package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.TaskCostTransactionResponse;
import com.example.project_pulse_backend.service.TaskCostTransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-cost-transactions")
@AllArgsConstructor
public class TaskCostTransactionController {
    private TaskCostTransactionService taskCostTransactionService;

    @GetMapping("/project/{projectId}")
    public APIResponse<TaskCostTransactionResponse> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long phaseId) {
        return APIResponse.<TaskCostTransactionResponse>builder()
                .resultList(taskCostTransactionService.getByProject(projectId, keyword, phaseId))
                .build();
    }

    @GetMapping("/project/{id}")
    public APIResponse<TaskCostTransactionResponse> getById(@PathVariable Long id) {
        return APIResponse.<TaskCostTransactionResponse>builder()
                .result(taskCostTransactionService.getById(id))
                .build();
    }

}
