package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseStatusRequest;
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
    public APIResponse<PhaseResponse> getAllPhasesByProjectForAdmin(@PathVariable Long projectId) {
        return APIResponse.<PhaseResponse>builder()
                .resultList(phaseService.getAllPhasesByProjectForAdmin(projectId)).build();
    }

    @GetMapping("/project/{projectId}/detail/{phaseId}")
    public APIResponse<PhaseResponse> getPhaseByProjectForAdmin(@PathVariable Long projectId, @PathVariable Long phaseId) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.getPhaseByProjectForAdmin(projectId, phaseId)).build();
    }

    @GetMapping("/pm/project/{projectId}")
    public APIResponse<PhaseResponse> getAllPhasesByProjectForPM(@PathVariable Long projectId) {
        return APIResponse.<PhaseResponse>builder()
                .resultList(phaseService.getAllPhasesByProjectForPM(projectId)).build();
    }

    @GetMapping("/pm/project/{projectId}/detail/{phaseId}")
    public APIResponse<PhaseResponse> getPhaseByProjectForPM(@PathVariable Long projectId, @PathVariable Long phaseId) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.getPhaseByProjectForPM(projectId, phaseId)).build();
    }

    @PatchMapping("/{id}/status")
    public APIResponse<PhaseResponse> updatePhaseStatus(@PathVariable Long id, @RequestBody @Valid UpdatePhaseStatusRequest request) {
        return APIResponse.<PhaseResponse>builder()
                .result(phaseService.updatePhaseStatus(id, request)).build();
    }
}
