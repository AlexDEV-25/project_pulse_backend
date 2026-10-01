package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.entity.Allocation;
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
import java.time.Duration;
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

    @Transactional
    public PhaseEvmSnapshot createSnapshot(Phase phase) {
        if (snapshotRepo.findByPhase_Id(phase.getId()).isPresent()) {
            throw AppException.builder().appError(AppError.PHASE_EVM_SNAPSHOT_ALREADY_EXISTS).build();
        }

        BigDecimal bac = phase.getProject().getProjectBudget();
        if (bac == null) {
            throw AppException.builder().appError(AppError.PHASE_EVM_BAC_NOT_SET).build();
        }

        LocalDateTime capturedAt = LocalDateTime.now();
        BigDecimal pv = plannedValueForAllocations(
                allocationRepo.findByPhase_Id(phase.getId()), capturedAt);
        BigDecimal ev = earningTransactionRepo.sumNetEarnedPointsByPhaseId(
                phase.getId(), EarningTransactionType.EARN, EarningTransactionType.REVERSAL);
        BigDecimal ac = taskCostTransactionRepo.sumCostPointsByPhaseId(phase.getId());

        return snapshotRepo.save(PhaseEvmSnapshot.builder()
                .phase(phase)
                .capturedAt(capturedAt)
                .bac(bac)
                .pv(pv)
                .ev(ev)
                .ac(ac)
                .build());
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

        long phaseDuration = Duration.between(phaseStartAt, phaseEndAt).toSeconds();
        if (phaseDuration <= 0) {
            return allocationPoint;
        }
        long elapsed = Duration.between(phaseStartAt, at).toSeconds();
        // Spread a phase's planned allocation points linearly across its scheduled duration.
        return allocationPoint.multiply(BigDecimal.valueOf(elapsed))
                .divide(BigDecimal.valueOf(phaseDuration), DIVISION_SCALE, ROUNDING_MODE);
    }

    private BigDecimal plannedValueForAllocations(
            List<Allocation> allocations, LocalDateTime at) {
        return allocations.stream()
                .map(allocation -> plannedValueAt(
                        allocation.getAllocationPoint(),
                        allocation.getPhase().getStartAt(),
                        allocation.getPhase().getEndAt(),
                        at))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal divideOrNull(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() == 0) {
            return null;
        }
        return numerator.divide(denominator, DIVISION_SCALE, ROUNDING_MODE);
    }
}
