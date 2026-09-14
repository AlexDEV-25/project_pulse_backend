package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AllocationPlanStatus;
import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.request.UpdatePhaseStatusRequest;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.TimeHelper;
import com.example.project_pulse_backend.repository.PhaseRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseService {
    private final PhaseRepo phaseRepo;
    private final ProjectRepo projectRepo;
    private final GetUserByToken getUserByToken;
    private final TimeHelper timeHelper;

    @PreAuthorize("hasAuthority('CREATE_PHASE')")
    public PhaseResponse createPhase(CreatePhaseRequest request) {
        Project project = projectRepo.findById(request.getProjectId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        int workdays = request.getWorkdays() ==
                null ? timeHelper.workdays(request.getStartAt(), request.getEndAt()) : request.getWorkdays();

        timeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());
        timeHelper.checkPhaseInProject(request.getStartAt(), request.getEndAt(), project.getStartAt(), project.getEndAt());

        Phase newPhase = phaseRepo.save(Phase.builder()
                .project(project)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .phaseName(request.getPhaseName())
                .workdays(workdays)
                .status(AllocationPlanStatus.PENDING)
                .build());

        return toResponse(newPhase);
    }

    @PreAuthorize("hasAuthority('UPDATE_PHASE')")
    public PhaseResponse updatePhase(Long id, UpdatePhaseRequest request) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        Phase entity = phaseRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );

        Project project = projectRepo.findByIdAndPm_Id(request.getProjectId(), pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );


        int workdays = request.getWorkdays() ==
                null ? timeHelper.workdays(request.getStartAt(), request.getEndAt()) : request.getWorkdays();

        timeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());
        timeHelper.checkPhaseInProject(request.getStartAt(), request.getEndAt(), project.getStartAt(), project.getEndAt());

        entity.setProject(project);
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setPhaseName(request.getPhaseName());
        entity.setWorkdays(workdays);

        return toResponse(phaseRepo.save(entity));
    }

    @PreAuthorize("hasAuthority('DELETE_PHASE')")
    public void deletePhase(Long id) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        Phase phase = phaseRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );
        phaseRepo.delete(phase);
    }

    @PreAuthorize("hasAuthority('GET_ALL_PHASES')")
    public List<PhaseResponse> getAllPhasesByProjectForAdmin(Long projectId) {
        projectRepo.findById(projectId).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        return phaseRepo.findByProject_Id(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_PHASE_BY_ID')")
    public PhaseResponse getPhaseByProjectForAdmin(Long projectId, Long phaseId) {
        Phase phase = phaseRepo.findByIdAndProject_Id(phaseId, projectId).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );
        return toResponse(phase);
    }

    @PreAuthorize("hasAuthority('GET_ALL_PHASES_FOR_PM')")
    public List<PhaseResponse> getAllPhasesByProjectForPM(Long projectId) {
        User pm = getUserByToken.get();
        checkIsPm(pm);
        projectRepo.findByIdAndPm_Id(projectId, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        return phaseRepo.findByProject_IdAndProject_Pm_Id(projectId, pm.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_PHASE_BY_ID_FOR_PM')")
    public PhaseResponse getPhaseByProjectForPM(Long projectId, Long phaseId) {
        User pm = getUserByToken.get();
        checkIsPm(pm);
        Phase phase = phaseRepo.findByIdAndProject_IdAndProject_Pm_Id(phaseId, projectId, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );
        return toResponse(phase);
    }

    @PreAuthorize("hasAuthority('UPDATE_PHASE_STATUS')")
    public PhaseResponse updatePhaseStatus(Long id, UpdatePhaseStatusRequest request) {
        Phase phase = phaseRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );
        phase.setStatus(request.getStatus());
        return toResponse(phaseRepo.save(phase));
    }

    private PhaseResponse toResponse(Phase phase) {
        return PhaseResponse.builder()
                .id(phase.getId())
                .projectId(phase.getProject() != null ? phase.getProject().getId() : null)
                .projectName(phase.getProject() != null ? phase.getProject().getProjectName() : null)
                .startAt(phase.getStartAt())
                .endAt(phase.getEndAt())
                .phaseName(phase.getPhaseName())
                .workdays(phase.getWorkdays())
                .status(phase.getStatus())
                .build();
    }

    private void checkIsPm(User user) {
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("PM"))) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
    }


}
