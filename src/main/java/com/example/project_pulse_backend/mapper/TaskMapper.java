package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateTaskRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskRequest;
import com.example.project_pulse_backend.dto.response.TaskResponse;
import com.example.project_pulse_backend.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = TaskAssignmentMapper.class)
public interface TaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phase", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    Task toTask(CreateTaskRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phase", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    void updateTask(UpdateTaskRequest request, @MappingTarget Task task);

    @Mapping(target = "projectId", source = "phase.project.id")
    @Mapping(target = "projectName", source = "phase.project.projectName")
    @Mapping(target = "phaseId", source = "phase.id")
    @Mapping(target = "phaseName", source = "phase.phaseName")
    @Mapping(target = "progress", ignore = true)
    TaskResponse toTaskResponse(Task task);
}
