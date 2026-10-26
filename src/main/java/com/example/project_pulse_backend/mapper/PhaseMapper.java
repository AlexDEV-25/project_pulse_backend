package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.entity.Phase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhaseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "makeUpDates", ignore = true)
    @Mapping(target = "offDates", ignore = true)
    Phase toPhase(CreatePhaseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "makeUpDates", ignore = true)
    @Mapping(target = "offDates", ignore = true)
    void updatePhase(UpdatePhaseRequest request, @MappingTarget Phase phase);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.projectName")
    @Mapping(target = "makeUpDates", defaultExpression = "java(java.util.List.of())")
    @Mapping(target = "offDates", defaultExpression = "java(java.util.List.of())")
    @Mapping(target = "workdays", ignore = true)
    @Mapping(target = "progress", ignore = true)
    PhaseResponse toPhaseResponse(Phase phase);
}
