package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateProjectMemberRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectMemberRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectMemberStatusRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.ProjectMemberResponse;
import com.example.project_pulse_backend.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project-member")
@AllArgsConstructor
public class ProjectMemberController {
    private ProjectMemberService projectMemberService;

    @PostMapping
    public APIResponse<ProjectMemberResponse> createProjectMember(@RequestBody @Valid CreateProjectMemberRequest request) {
        return APIResponse.<ProjectMemberResponse>builder()
                .result(projectMemberService.createProjectMember(request)).build();
    }

    @PutMapping("/{id}")
    public APIResponse<ProjectMemberResponse> updateProjectMember(@PathVariable Long id,
                                                                  @RequestBody @Valid UpdateProjectMemberRequest request) {
        return APIResponse.<ProjectMemberResponse>builder()
                .result(projectMemberService.updateProjectMember(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteProjectMember(@PathVariable Long id) {
        projectMemberService.deleteProjectMember(id);
        return APIResponse.<Void>builder().build();
    }

    @PatchMapping("/{id}/status")
    public APIResponse<ProjectMemberResponse> updateProjectMemberStatus(@PathVariable Long id,
                                                                        @RequestBody @Valid UpdateProjectMemberStatusRequest request) {
        return APIResponse.<ProjectMemberResponse>builder()
                .result(projectMemberService.updateProjectMemberStatus(id, request)).build();
    }
}
