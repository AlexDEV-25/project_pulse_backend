package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.request.CreateEarningTransferRequest;
import com.example.project_pulse_backend.dto.response.EarningTransactionResponse;
import com.example.project_pulse_backend.entity.EarningTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EarningTransactionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "executor", ignore = true)
    @Mapping(target = "referenceTransaction", ignore = true)
    @Mapping(target = "earningTransactionType", ignore = true)
    EarningTransaction toTransfer(CreateEarningTransferRequest request);

    @Mapping(target = "projectId", source = "task.phase.project.id")
    @Mapping(target = "phaseId", source = "task.phase.id")
    @Mapping(target = "phaseName", source = "task.phase.phaseName")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskName", source = "task.taskName")
    @Mapping(target = "employeeId", source = "executor.user.id")
    @Mapping(target = "employeeName", source = "executor.user.userName")
    @Mapping(target = "referenceTransactionId", source = "referenceTransaction.id")
    EarningTransactionResponse toEarningTransactionResponse(EarningTransaction transaction);
}
