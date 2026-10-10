package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.dto.response.EvmHealthResponse;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.PhaseEvmSnapshot;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.EvmHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.PhaseHelper;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import com.example.project_pulse_backend.repository.TaskCostTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvmService {
    private final GetUserByToken getUserByToken;
    private final EvmHelper evmHelper;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;
    private final EarningTransactionRepo earningTransactionRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;

    @PreAuthorize("hasAuthority('GET_EVM_BY_PHASE')")
    public EvmHealthResponse getPhaseHealth(Long phaseId) {
        User user = getUserByToken.get();
        Phase phase = phaseHelper.getPhaseById(phaseId, user);

        if (phase.getStatus() == PhaseStatus.DONE) {
            PhaseEvmSnapshot snapshot = evmHelper.getPhaseEvmSnapshotByPhaseId(phase.getId());
            Phase snapshotPhase = snapshot.getPhase();
            return toResponse(snapshot, snapshotPhase, snapshotPhase.getProject());
        }
        return toResponse(evmHelper.snapshotAt(phase, LocalDateTime.now()), phase, phase.getProject());
    }

    @PreAuthorize("hasAuthority('GET_EVM_BY_PROJECT')")
    public EvmHealthResponse getProjectHealth(Long projectId) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);

        BigDecimal bac = project.getProjectBudget();
        LocalDateTime capturedAt = LocalDateTime.now();
        BigDecimal pv = evmHelper.pv(projectId, capturedAt);
        BigDecimal ev = earningTransactionRepo.sumNetEarnedPointsByProjectId(project.getId(), EarningTransactionType.EARN);
        BigDecimal ac = taskCostTransactionRepo.sumCostPointsByProjectId(project.getId());

        PhaseEvmSnapshot snapshot = new PhaseEvmSnapshot(null, null, capturedAt, bac, pv, ev, ac);

        return toResponse(snapshot, null, project);
    }

    private EvmHealthResponse toResponse(PhaseEvmSnapshot snapshot, Phase snapshotPhase, Project project) {
        BigDecimal cpi = evmHelper.cpi(snapshot.getEv(), snapshot.getAc());
        BigDecimal eac = evmHelper.eac(snapshot.getBac(), cpi);
        LocalDateTime captureAt = snapshot.getCapturedAt();
        BigDecimal bac = snapshot.getBac();
        BigDecimal pv = snapshot.getPv();
        BigDecimal ev = snapshot.getEv();
        BigDecimal ac = snapshot.getAc();
        Long phaseId = snapshotPhase != null ? snapshotPhase.getId() : null;
        String phaseName = snapshotPhase != null ? snapshotPhase.getPhaseName() : null;

        Long projectId = project != null ? project.getId() : null;
        String projectName = project != null ? project.getProjectName() : null;


        return EvmHealthResponse.builder()
                .projectId(projectId)
                .projectName(projectName)
                .phaseId(phaseId)
                .phaseName(phaseName)
                .capturedAt(captureAt)
                .bac(bac)
                .pv(pv)
                .ev(ev)
                .ac(ac)
                .cv(evmHelper.cv(ev, ac))
                .sv(evmHelper.sv(ev, pv))
                .cpi(cpi)
                .spi(evmHelper.spi(ev, pv))
                .eac(eac)
                .etc(evmHelper.etc(eac, ac))
                .vac(evmHelper.vac(bac, eac))
                .tcpi(evmHelper.tcpi(bac, ev, ac))
                .build();
    }

}
