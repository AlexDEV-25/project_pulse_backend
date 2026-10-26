package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.TaskCostTransactionResponse;
import com.example.project_pulse_backend.entity.TaskCostTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskCostTransactionMapper {
    @Mapping(target = "projectId", source = "task.phase.project.id")
    @Mapping(target = "phaseId", source = "task.phase.id")
    @Mapping(target = "phaseName", source = "task.phase.phaseName")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskName", source = "task.taskName")
    @Mapping(target = "employeeId", source = "executor.user.id")
    @Mapping(target = "employeeName", source = "executor.user.userName")
    TaskCostTransactionResponse toTaskCostTransactionResponse(TaskCostTransaction transaction);
}
