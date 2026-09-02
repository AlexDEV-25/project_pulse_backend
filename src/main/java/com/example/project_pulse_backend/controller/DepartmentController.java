package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateDepartmentRequest;
import com.example.project_pulse_backend.dto.request.UpdateDepartmentRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.DepartmentResponse;
import com.example.project_pulse_backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department")
@AllArgsConstructor
public class DepartmentController {
    private DepartmentService departmentService;

    @PostMapping
    public APIResponse<DepartmentResponse> create(@RequestBody @Valid CreateDepartmentRequest request) {
        return APIResponse.<DepartmentResponse>builder()
                .result(departmentService.createDepartment(request)).build();
    }

    @PutMapping("/{id}")
    public APIResponse<DepartmentResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateDepartmentRequest request) {
        return APIResponse.<DepartmentResponse>builder()
                .result(departmentService.updateDepartment(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return APIResponse.<Void>builder().build();
    }

    @GetMapping
    public APIResponse<DepartmentResponse> findAll() {
        return APIResponse.<DepartmentResponse>builder()
                .resultList(departmentService.getAllDepartments()).build();
    }
}
