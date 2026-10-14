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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AllocationHelper {
    private final AllocationRepo allocationRepo;
    private final ProjectHelper projectHelper;
    private final PhaseHelper phaseHelper;

    // lấy phân bổ theo project
    public List<Allocation> getAllocationByProject(Long projectId, Long phaseId, User user, String keyword) {
        Project project = projectHelper.getProjectById(projectId, user);
        return allocationRepo.findByProjectId(project.getId(), phaseId, keyword,
                RoleHelper.checkIsEmployee(user) ? user.getId() : null);
    }

    // lấy phân bổ theo id
    public Allocation getAllocationById(Long id, User user) {
        Allocation allocation = allocationRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.ALLOCATION_NOT_FOUND).build());

        projectHelper.checkAccessToProject(allocation.getPhase().getProject().getId(), user);
        return allocation;
    }

    // tính point phân bổ (point = % phân bổ x ngày làm việc(trong phase) x costSnapshot)
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

    public void recalculateForUser(Long userId) {
        List<Allocation> allocations = allocationRepo.findAllByMember_User_Id(userId);
        Map<Allocation, Set<LocalDate>> workingDatesByAllocation = new HashMap<>();
        Map<LocalDate, Integer> dailyTotals = new HashMap<>();

        for (Allocation allocation : allocations) {
            Phase phase = allocation.getPhase();
            Set<LocalDate> workingDates = TimeHelper.workingDates(
                    phase.getStartAt(),
                    phase.getEndAt(),
                    phase.getMakeUpDates(),
                    phase.getOffDates());
            workingDatesByAllocation.put(allocation, workingDates);
            for (LocalDate date : workingDates) {
                dailyTotals.merge(date, allocation.getAllocationPercentage(), Integer::sum);
            }
        }

        for (Allocation allocation : allocations) {
            List<LocalDate> overloadedDates = workingDatesByAllocation.get(allocation).stream()
                    .filter(date -> dailyTotals.getOrDefault(date, 0) > 100)
                    .toList();
            allocation.setOverloadedDates(overloadedDates);
        }
    }

    public void checkDone(Phase phase) {
        projectHelper.checkProjectNotDone(phase.getProject());
        phaseHelper.checkPhaseNotDone(phase);
    }

}
