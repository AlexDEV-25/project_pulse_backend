package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateAllocationRequest;
import com.example.project_pulse_backend.dto.request.UpdateAllocationRequest;
import com.example.project_pulse_backend.dto.response.AllocationResponse;
import com.example.project_pulse_backend.entity.Allocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = AllocationOverloadedDateMapper.class)
public interface AllocationMapper {
    @Mapping(target = "phase", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "resourceRateSnapshot", ignore = true)
    @Mapping(target = "overloadedDates", ignore = true)
    Allocation toAllocation(CreateAllocationRequest request);

    @Mapping(target = "allocationPercentage", source = "allocationPercentage")
    void updateAllocation(UpdateAllocationRequest request, @MappingTarget Allocation allocation);

    @Mapping(target = "projectId", source = "phase.project.id")
    @Mapping(target = "projectName", source = "phase.project.projectName")
    @Mapping(target = "phaseId", source = "phase.id")
    @Mapping(target = "phaseName", source = "phase.phaseName")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "userId", source = "member.user.id")
    @Mapping(target = "userName", source = "member.user.userName")
    @Mapping(target = "allocationPoint", ignore = true)
    @Mapping(target = "overloadedDates", defaultExpression = "java(java.util.List.of())")
    AllocationResponse toAllocationResponse(Allocation allocation);
}
