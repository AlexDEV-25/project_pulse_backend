package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateTaskNoteRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskNoteRequest;
import com.example.project_pulse_backend.dto.response.TaskNoteResponse;
import com.example.project_pulse_backend.entity.TaskNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskNoteMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "taskNoteStatus", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    TaskNote toTaskNote(CreateTaskNoteRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "taskNoteStatus", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    void updateTaskNote(UpdateTaskNoteRequest request, @MappingTarget TaskNote taskNote);

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "employeeId", source = "author.user.id")
    @Mapping(target = "employeeName", source = "author.user.userName")
    @Mapping(target = "employeeAvatarUrl", source = "author.user.avatarUrl")
    TaskNoteResponse toTaskNoteResponse(TaskNote taskNote);
}
