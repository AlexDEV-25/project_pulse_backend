package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateProjectRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectRequest;
import com.example.project_pulse_backend.dto.response.ProjectResponse;
import com.example.project_pulse_backend.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pm", ignore = true)
    @Mapping(target = "projectStatus", ignore = true)
    Project toProject(CreateProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pm", ignore = true)
    @Mapping(target = "projectStatus", ignore = true)
    void updateProject(UpdateProjectRequest request, @MappingTarget Project project);

    @Mapping(target = "pmId", source = "pm.id")
    @Mapping(target = "pmName", source = "pm.userName")
    ProjectResponse toProjectResponse(Project project);

    @Mapping(target = "pmId", source = "pm.id")
    @Mapping(target = "pmName", source = "pm.userName")
    @Mapping(target = "clientBudget", ignore = true)
    ProjectResponse toProjectResponseForPm(Project project);

    @Mapping(target = "pmId", source = "pm.id")
    @Mapping(target = "pmName", source = "pm.userName")
    @Mapping(target = "clientBudget", ignore = true)
    @Mapping(target = "projectBudget", ignore = true)
    ProjectResponse toProjectResponseForEmployee(Project project);
}
