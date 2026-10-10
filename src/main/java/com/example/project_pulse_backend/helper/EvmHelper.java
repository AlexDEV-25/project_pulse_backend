package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.PhaseEvmSnapshot;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import com.example.project_pulse_backend.repository.PhaseEvmSnapshotRepo;
import com.example.project_pulse_backend.repository.TaskCostTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EvmHelper {
    private static final int DIVISION_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private final PhaseEvmSnapshotRepo snapshotRepo;
    private final AllocationRepo allocationRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final AllocationHelper allocationHelper;

    @Transactional
    public void createSnapshot(Phase phase) {
        if (snapshotRepo.findByPhase_Id(phase.getId()).isPresent()) {
            throw AppException.builder().appError(AppError.PHASE_EVM_SNAPSHOT_ALREADY_EXISTS).build();
        }

        LocalDateTime capturedAt = LocalDateTime.now();
        BigDecimal bac = bacForPhase(phase);
        BigDecimal pv = pvForPhase(phase, capturedAt);
        BigDecimal ev = evForPhase(phase.getId());
        BigDecimal ac = acForPhase(phase.getId());

        snapshotRepo.save(PhaseEvmSnapshot.builder()
                .phase(phase)
                .capturedAt(capturedAt)
                .bac(bac).pv(pv).ev(ev).ac(ac).build());
    }

    public BigDecimal cv(BigDecimal ev, BigDecimal ac) {
        return ev.subtract(ac);
    }

    public BigDecimal sv(BigDecimal ev, BigDecimal pv) {
        return ev.subtract(pv);
    }

    public BigDecimal cpi(BigDecimal ev, BigDecimal ac) {
        return divideOrNull(ev, ac);
    }

    public BigDecimal spi(BigDecimal ev, BigDecimal pv) {
        return divideOrNull(ev, pv);
    }

    public BigDecimal eac(BigDecimal bac, BigDecimal cpi) {
        return cpi == null ? null : divideOrNull(bac, cpi);
    }

    public BigDecimal etc(BigDecimal eac, BigDecimal ac) {
        return eac == null ? null : eac.subtract(ac);
    }

    public BigDecimal vac(BigDecimal bac, BigDecimal eac) {
        return eac == null ? null : bac.subtract(eac);
    }

    public BigDecimal tcpi(BigDecimal bac, BigDecimal ev, BigDecimal ac) {
        return divideOrNull(bac.subtract(ev), bac.subtract(ac));
    }

    public BigDecimal plannedValueAt(
            BigDecimal allocationPoint,
            LocalDateTime phaseStartAt,
            LocalDateTime phaseEndAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates,
            LocalDateTime at) {
        Objects.requireNonNull(allocationPoint, "allocationPoint must not be null");
        Objects.requireNonNull(phaseStartAt, "phaseStartAt must not be null");
        Objects.requireNonNull(phaseEndAt, "phaseEndAt must not be null");
        if (at.isBefore(phaseStartAt)) {
            return BigDecimal.ZERO;
        }
        if (!at.isBefore(phaseEndAt)) {
            return allocationPoint;
        }

        int phaseWorkdays = TimeHelper.workdays(
                phaseStartAt, phaseEndAt, makeUpDates, offDates);
        if (phaseWorkdays <= 0) {
            return BigDecimal.ZERO;
        }
        int elapsedWorkdays = Math.clamp(
                TimeHelper.elapsedWorkdays(
                        phaseStartAt, phaseEndAt, at, makeUpDates, offDates), 0, phaseWorkdays);

        return allocationPoint.multiply(BigDecimal.valueOf(elapsedWorkdays))
                .divide(BigDecimal.valueOf(phaseWorkdays), DIVISION_SCALE, ROUNDING_MODE);
    }

    public PhaseEvmSnapshot getPhaseEvmSnapshotByPhaseId(Long phaseId) {
        return snapshotRepo.findByPhase_Id(phaseId).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_EVM_SNAPSHOT_NOT_FOUND).build()
        );
    }

    public PhaseEvmSnapshot snapshotAt(Phase phase, LocalDateTime capturedAt) {
        return new PhaseEvmSnapshot(
                null,
                phase,
                capturedAt,
                bacForPhase(phase),
                pvForPhase(phase, capturedAt),
                evForPhase(phase.getId()),
                acForPhase(phase.getId())
        );
    }

    public BigDecimal pv(Long projectId, LocalDateTime at) {
        BigDecimal total = BigDecimal.ZERO;
        for (var allocation : allocationRepo.findByProjectId(projectId, null, null, null)) {
            Phase phase = allocation.getPhase();
            total = total.add(plannedValueAt(
                    allocationHelper.calculateAllocationPoint(allocation),
                    phase.getStartAt(),
                    phase.getEndAt(),
                    phase.getMakeUpDates(),
                    phase.getOffDates(),
                    at));
        }
        return total;
    }

    private BigDecimal bacForPhase(Phase phase) {
        return allocationRepo.findByPhase_Id(phase.getId()).stream()
                .map(allocationHelper::calculateAllocationPoint)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal pvForPhase(Phase phase, LocalDateTime at) {
        return allocationRepo.findByPhase_Id(phase.getId()).stream()
                .map(allocation -> plannedValueAt(
                        allocationHelper.calculateAllocationPoint(allocation),
                        phase.getStartAt(),
                        phase.getEndAt(),
                        phase.getMakeUpDates(),
                        phase.getOffDates(),
                        at))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal acForPhase(Long phaseId) {
        return taskCostTransactionRepo.sumCostPointsByPhaseId(phaseId);
    }

    public BigDecimal evForPhase(Long phaseId) {
        return earningTransactionRepo.sumNetEarnedPointsByPhaseId(
                phaseId, EarningTransactionType.EARN);

    }


    private BigDecimal divideOrNull(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() == 0) {
            return null;
        }
        return numerator.divide(denominator, DIVISION_SCALE, ROUNDING_MODE);
    }
}
