package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.service.PhaseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phase")
@AllArgsConstructor
public class PhaseController {
    private PhaseService phaseService;

    @PostMapping
    public APIResponse<PhaseResponse> createPhase(@RequestBody @Valid CreatePhaseRequest request) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.createPhase(request)).build();
    }

    @PutMapping("/{id}")
    public APIResponse<PhaseResponse> updatePhase(@PathVariable Long id, @RequestBody @Valid UpdatePhaseRequest request) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.updatePhase(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deletePhase(@PathVariable Long id) {
        phaseService.deletePhase(id);
        return APIResponse.<Void>builder().build();
    }

    @GetMapping("/project/{projectId}")
    public APIResponse<PhaseResponse> getAllPhasesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword) {
        return APIResponse.<PhaseResponse>builder()
                .resultList(phaseService.getAllPhasesByProject(projectId, keyword)).build();
    }

    @GetMapping("/detail/{phaseId}")
    public APIResponse<PhaseResponse> getPhaseByProject(@PathVariable Long phaseId) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.getPhaseById(phaseId)).build();
    }


}
