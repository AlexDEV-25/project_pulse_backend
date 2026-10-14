package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.dto.request.CreatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.AllocationHelper;
import com.example.project_pulse_backend.helper.EvmHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.PhaseHelper;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.ProjectMemberHelper;
import com.example.project_pulse_backend.helper.TimeHelper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhaseService {
    private final PhaseRepo phaseRepo;

    private final GetUserByToken getUserByToken;
    private final PhaseHelper phaseHelper;
    private final AllocationHelper allocationHelper;
    private final ProjectHelper projectHelper;
    private final EvmHelper evmHelper;
    private final AllocationRepo allocationRepo;
    private final ProjectMemberHelper projectMemberHelper;

    @PreAuthorize("hasAuthority('CREATE_PHASE')")
    @Transactional
    public PhaseResponse createPhase(CreatePhaseRequest request) {
        Project project = projectHelper.getProjectById(request.getProjectId(), getUserByToken.get());
        projectHelper.checkProjectNotDone(project);

        phaseHelper.checkTime(project, request.getStartAt(), request.getEndAt());
        List<LocalDate> makeUpDates = phaseHelper.normalizeDates(request.getMakeUpDates());
        List<LocalDate> offDates = phaseHelper.normalizeDates(request.getOffDates());
        phaseHelper.checkSpecialDates(request.getStartAt(), request.getEndAt(), makeUpDates, offDates);

        Phase newPhase = phaseRepo.save(Phase.builder()
                .project(project)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .phaseName(request.getPhaseName())
                .makeUpDates(makeUpDates)
                .offDates(offDates)
                .status(PhaseStatus.PLANNED)
                .build());

        return toResponse(newPhase);
    }

    @PreAuthorize("hasAuthority('UPDATE_PHASE')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PhaseResponse updatePhase(Long id, UpdatePhaseRequest request) {
        var user = getUserByToken.get();
        Phase entity = phaseRepo.findByIdForUpdate(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build());

        projectHelper.checkAccessToProject(entity.getProject().getId(), user);

        projectHelper.checkProjectNotDone(entity.getProject());
        phaseHelper.checkPhaseNotDone(entity);

        phaseHelper.checkTime(entity.getProject(), request.getStartAt(), request.getEndAt());
        List<LocalDate> makeUpDates = phaseHelper.normalizeDates(request.getMakeUpDates());
        List<LocalDate> offDates = phaseHelper.normalizeDates(request.getOffDates());
        phaseHelper.checkSpecialDates(request.getStartAt(), request.getEndAt(), makeUpDates, offDates);

        List<Long> affectedUserIds = allocationRepo.findByPhase_Id(id).stream()
                .map(allocation -> allocation.getMember().getUser().getId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        affectedUserIds.forEach(projectMemberHelper::lockAllProjectMembersByUserId);

        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setPhaseName(request.getPhaseName());
        entity.setMakeUpDates(makeUpDates);
        entity.setOffDates(offDates);

        if (request.getPhaseStatus() == PhaseStatus.DONE && entity.getStatus() != PhaseStatus.DONE) {
            evmHelper.createSnapshot(entity);
        }
        entity.setStatus(request.getPhaseStatus());

        Phase saved = phaseRepo.save(entity);
        affectedUserIds.forEach(allocationHelper::recalculateForUser);
        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('DELETE_PHASE')")
    @Transactional
    public void deletePhase(Long id) {
        Phase phase = phaseHelper.getPhaseById(id, getUserByToken.get());

        projectHelper.checkProjectNotDone(phase.getProject());
        phaseHelper.checkPhaseNotDone(phase);

        phaseHelper.checkHasTask(id);
        phaseHelper.checkHasAllocation(id);

        phaseRepo.delete(phase);
    }

    @PreAuthorize("hasAuthority('GET_PHASES_BY_PROJECT')")
    public List<PhaseResponse> getAllPhasesByProject(Long projectId, String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        List<Phase> phases = phaseHelper.getPhasesByProject(projectId, getUserByToken.get(), normalizedKeyword);
        return phases.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_PHASE_BY_ID')")
    public PhaseResponse getPhaseById(Long phaseId) {
        Phase phase = phaseHelper.getPhaseById(phaseId, getUserByToken.get());
        return toResponse(phase);
    }

    private PhaseResponse toResponse(Phase phase) {
        return PhaseResponse.builder()
                .id(phase.getId())
                .projectId(phase.getProject() != null ? phase.getProject().getId() : null)
                .projectName(phase.getProject() != null ? phase.getProject().getProjectName() : null)
                .startAt(phase.getStartAt())
                .endAt(phase.getEndAt())
                .phaseName(phase.getPhaseName())
                .makeUpDates(phase.getMakeUpDates() == null ? List.of() : phase.getMakeUpDates())
                .offDates(phase.getOffDates() == null ? List.of() : phase.getOffDates())
                .workdays(TimeHelper.workdays(
                        phase.getStartAt(), phase.getEndAt(), phase.getMakeUpDates(), phase.getOffDates()))
                .status(phase.getStatus())
                .progress(phaseHelper.calculateProgress(phase.getId()))
                .build();
    }


}
