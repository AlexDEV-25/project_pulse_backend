package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.TaskAssignmentResponse;
import com.example.project_pulse_backend.dto.request.TaskAssignmentRequest;
import com.example.project_pulse_backend.entity.TaskAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskAssignmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "employee", ignore = true)
    TaskAssignment toTaskAssignment(TaskAssignmentRequest request);

    @Mapping(target = "employeeId", source = "employee.user.id")
    @Mapping(target = "employeeName", source = "employee.user.userName")
    TaskAssignmentResponse toTaskAssignmentResponse(TaskAssignment assignment);
}
