package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateAllocationRequest;
import com.example.project_pulse_backend.dto.request.UpdateAllocationRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.AllocationResponse;
import com.example.project_pulse_backend.service.AllocationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/allocation")
@AllArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    public APIResponse<AllocationResponse> createAllocation(@RequestBody @Valid CreateAllocationRequest request) {
        return APIResponse.<AllocationResponse>builder()
                .result(allocationService.createAllocation(request))
                .build();
    }

    @PutMapping("/{id}")
    public APIResponse<AllocationResponse> updateAllocation(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAllocationRequest request
    ) {
        return APIResponse.<AllocationResponse>builder()
                .result(allocationService.updateAllocation(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteAllocation(@PathVariable Long id) {
        allocationService.deleteAllocation(id);
        return APIResponse.<Void>builder().build();
    }
}