package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.entity.Allocation;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AllocationHelper {
    private final AllocationRepo allocationRepo;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;


    public List<Allocation> getAllocationByProject(Long projectId, Long phaseId, User user, String keyword) {
        Project project = projectHelper.getProjectById(projectId, user);
        return allocationRepo.findByProjectId(project.getId(), phaseId, keyword,
                RoleHelper.checkIsEmployee(user) ? user.getId() : null);
    }

    public Allocation getAllocationById(Long id, User user) {
        Allocation allocation = allocationRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.ALLOCATION_NOT_FOUND).build());

        projectHelper.checkAccessToProject(allocation.getPhase().getProject().getId(), user);
        return allocation;
    }

    public void checkHasAllocation(Long phaseId) {
        if (!allocationRepo.findByPhase_Id(phaseId).isEmpty()) {
            throw AppException.builder().appError(AppError.PHASE_IN_USE).build();
        }
    }

    public BigDecimal calculateAllocationPoint(Allocation allocation) {
        if (allocation == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal resourceRate = allocation.getResourceRateSnapshot();
        Integer percentage = allocation.getAllocationPercentage();

        if (resourceRate == null || percentage == null) {
            return BigDecimal.ZERO;
        }

        Phase phase = allocation.getPhase();
        int workdays = TimeHelper.workdays(
                phase.getStartAt(),
                phase.getEndAt(),
                phase.getMakeUpDates(),
                phase.getOffDates()
        );

        return BigDecimal.valueOf(workdays)
                .multiply(resourceRate)
                .multiply(BigDecimal.valueOf(percentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void validateTotalAllocationPercentage(Long userId, LocalDateTime startAt, LocalDateTime endAt,
                                                  Integer newPercentage, Long currentAllocationId) {
        List<Allocation> overlappingAllocations = allocationRepo.findOverlappingAllocationsByUserId(userId,
                startAt, endAt);

        int currentTotal = overlappingAllocations.stream()
                .filter(a -> !a.getId().equals(currentAllocationId))
                .mapToInt(Allocation::getAllocationPercentage)
                .sum();

        if (currentTotal + newPercentage > 100) {
            throw AppException.builder().appError(AppError.TOTAL_ALLOCATION_EXCEEDS_100).build();
        }
    }

    public void checkDone(Phase phase) {
        projectHelper.checkProjectNotDone(phase.getProject());
        phaseHelper.checkPhaseNotDone(phase);
    }

}
