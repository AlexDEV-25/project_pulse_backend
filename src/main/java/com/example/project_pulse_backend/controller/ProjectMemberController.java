package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateProjectMemberRequest;
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

    @GetMapping("/project/{projectId}")
    public APIResponse<ProjectMemberResponse> getMyProjects(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword) {
        return APIResponse.<ProjectMemberResponse>builder()
                .resultList(projectMemberService.getMyProjectMembers(projectId, keyword)).build();
    }

    @PostMapping
    public APIResponse<ProjectMemberResponse> createProjectMember(
            @RequestBody @Valid CreateProjectMemberRequest request) {
        return APIResponse.<ProjectMemberResponse>builder()
                .result(projectMemberService.createProjectMember(request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteProjectMember(@PathVariable Long id) {
        projectMemberService.deleteProjectMember(id);
        return APIResponse.<Void>builder().build();
    }

}
