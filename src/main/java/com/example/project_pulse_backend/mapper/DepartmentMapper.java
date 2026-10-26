package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateDepartmentRequest;
import com.example.project_pulse_backend.dto.request.UpdateDepartmentRequest;
import com.example.project_pulse_backend.dto.response.DepartmentResponse;
import com.example.project_pulse_backend.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hidden", constant = "false")
    Department toDepartment(CreateDepartmentRequest request);

    @Mapping(target = "id", ignore = true)
    void updateDepartment(UpdateDepartmentRequest request, @MappingTarget Department department);

    @Mapping(target = "employeeCount", source = "employeeCount")
    DepartmentResponse toDepartmentResponse(Department department, long employeeCount);
}
