package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.dto.request.CreateEarningTransferRequest;
import com.example.project_pulse_backend.dto.response.EarningTransactionResponse;
import com.example.project_pulse_backend.entity.EarningTransaction;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.EarningTransactionHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.ProjectMemberHelper;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EarningTransactionService {
    private final EarningTransactionRepo earningTransactionRepo;
    private final GetUserByToken getUserByToken;
    private final ProjectMemberHelper projectMemberHelper;
    private final EarningTransactionHelper earningTransactionHelper;
    private final ProjectHelper projectHelper;

    @PreAuthorize("hasAuthority('GET_EARNINGS_BY_PROJECT')")
    public List<EarningTransactionResponse> getByProject(
            Long projectId, String keyword, Long phaseId, EarningTransactionType earningTransactionType) {
        User user = getUserByToken.get();
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return earningTransactionHelper.getByProjectId(
                        projectId, user, normalizedKeyword, phaseId, earningTransactionType).stream()
                .map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_EARNINGS_BY_ID')")
    public EarningTransactionResponse getById(Long id, Long projectId) {
        User user = getUserByToken.get();
        return toResponse(earningTransactionHelper.getById(id, projectId, user));
    }

    @PreAuthorize("hasAuthority('TRANSFER_EARN')")
    @Transactional
    public EarningTransactionResponse transfer(Long projectId, CreateEarningTransferRequest request) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);

        EarningTransaction source = earningTransactionRepo.findByIdForUpdate(request.getSourceTransactionId())
                .orElseThrow(() -> AppException.builder().appError(AppError.EARNING_TRANSACTION_NOT_FOUND).build());


        if (!source.getTask().getPhase().getProject().getId().equals(project.getId())
                || source.getEarningTransactionType() != EarningTransactionType.EARN
                || source.getExecutor().getUser().getId().equals(request.getTargetEmployeeId())) {
            throw AppException.builder().appError(AppError.INVALID_EARNING_TRANSFER).build();
        }

        ProjectMember target = projectMemberHelper.findByProjectAndUser(project, request.getTargetEmployeeId(), user);

        earningTransactionHelper.checkAvailablePoint(source, request.getPoints());

        EarningTransaction transfer = EarningTransaction.builder()
                .task(source.getTask())
                .executor(target)
                .referenceTransaction(source)
                .points(request.getPoints())
                .earningTransactionType(EarningTransactionType.TRANSFER)
                .reason(request.getReason())
                .build();
        return toResponse(earningTransactionRepo.save(transfer));
    }

    private EarningTransactionResponse toResponse(EarningTransaction transaction) {
        var task = transaction.getTask();
        var employee = transaction.getExecutor().getUser();
        return EarningTransactionResponse.builder()
                .id(transaction.getId())
                .projectId(task.getPhase().getProject().getId())
                .phaseId(task.getPhase().getId())
                .phaseName(task.getPhase().getPhaseName())
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .employeeId(employee.getId())
                .employeeName(employee.getUserName())
                .referenceTransactionId(transaction.getReferenceTransaction() != null
                        ? transaction.getReferenceTransaction().getId() : null)
                .points(transaction.getPoints())
                .earningTransactionType(transaction.getEarningTransactionType())
                .reason(transaction.getReason())
                .build();
    }
}
