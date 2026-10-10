package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.CreateTaskNoteRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskNoteRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.TaskNoteResponse;
import com.example.project_pulse_backend.service.TaskNoteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class TaskNoteController {
    private TaskNoteService taskNoteService;

    @PostMapping("/api/task/notes")
    public APIResponse<TaskNoteResponse> createTaskNote(@RequestBody @Valid CreateTaskNoteRequest request) {
        return APIResponse.<TaskNoteResponse>builder()
                .result(taskNoteService.createTaskNote(request)).build();
    }

    @PutMapping("/api/task-note/{noteId}")
    public APIResponse<TaskNoteResponse> updateTaskNote(
            @PathVariable Long noteId, @RequestBody @Valid UpdateTaskNoteRequest request) {
        return APIResponse.<TaskNoteResponse>builder()
                .result(taskNoteService.updateTaskNote(noteId, request)).build();
    }

    @DeleteMapping("/api/task-note/{noteId}")
    public APIResponse<Void> deleteTaskNote(@PathVariable Long noteId) {
        taskNoteService.deleteTaskNote(noteId);
        return APIResponse.<Void>builder().build();
    }

    @GetMapping("/api/task/{taskId}/notes")
    public APIResponse<TaskNoteResponse> getTaskNotesByTask(@PathVariable Long taskId) {
        return APIResponse.<TaskNoteResponse>builder()
                .resultList(taskNoteService.getTaskNotesByTask(taskId)).build();
    }

    @PatchMapping("/api/task-note/{noteId}/confirm")
    public APIResponse<TaskNoteResponse> confirmTaskNotes(
            @PathVariable Long noteId) {
        return APIResponse.<TaskNoteResponse>builder()
                .result(taskNoteService.confirmTaskNotes(noteId)).build();
    }
}
