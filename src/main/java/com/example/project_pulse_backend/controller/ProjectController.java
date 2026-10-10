package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.dto.request.CreateProjectRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.ProjectResponse;
import com.example.project_pulse_backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
@AllArgsConstructor
public class ProjectController {
    private ProjectService projectService;

    @PostMapping
    public APIResponse<ProjectResponse> createProject(@RequestBody @Valid CreateProjectRequest request) {
        return APIResponse.<ProjectResponse>builder()
                .result(projectService.createProject(request)).build();
    }

    @PutMapping("/{id}")
    public APIResponse<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody @Valid UpdateProjectRequest request) {
        return APIResponse.<ProjectResponse>builder()
                .result(projectService.updateProject(id, request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return APIResponse.<Void>builder().build();
    }

    @GetMapping
    public APIResponse<ProjectResponse> getAllProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long pmId,
            @RequestParam(required = false) ProjectStatus projectStatus) {
        return APIResponse.<ProjectResponse>builder()
                .resultList(projectService.getAllProjects(pmId, keyword, projectStatus)).build();
    }

    @GetMapping("/{id}")
    public APIResponse<ProjectResponse> getProjectByIdForAdmin(@PathVariable Long id) {
        return APIResponse.<ProjectResponse>builder()
                .result(projectService.getProjectById(id)).build();
    }

}
