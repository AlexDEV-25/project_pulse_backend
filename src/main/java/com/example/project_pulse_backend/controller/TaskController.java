package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskStatusRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.TaskResponse;
import com.example.project_pulse_backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
@AllArgsConstructor
public class TaskController {
    private TaskService taskService;

    @PostMapping
    public APIResponse<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest request) {
        return APIResponse.<TaskResponse>builder()
                .result(taskService.createTask(request)).build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return APIResponse.<Void>builder().build();
    }

    @GetMapping("/project/{projectId}")
    public APIResponse<TaskResponse> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long phaseId) {
        return APIResponse.<TaskResponse>builder()
                .resultList(taskService.getTasksByProject(projectId, keyword, phaseId)).build();
    }

    @GetMapping("/pm/detail/{id}")
    public APIResponse<TaskResponse> getTaskById(@PathVariable Long id) {
        return APIResponse.<TaskResponse>builder()
                .result(taskService.getTaskById(id)).build();
    }


    @PutMapping("/{id}")
    public APIResponse<TaskResponse> updateTask(
            @PathVariable Long id, @RequestBody @Valid UpdateTaskRequest request) {
        return APIResponse.<TaskResponse>builder()
                .result(taskService.updateTask(id, request)).build();
    }

    @PatchMapping("/{id}/status")
    public APIResponse<TaskResponse> updateTaskStatus(
            @PathVariable Long id, @RequestBody @Valid UpdateTaskStatusRequest request) {
        return APIResponse.<TaskResponse>builder()
                .result(taskService.updateTaskStatus(id, request)).build();
    }
}
