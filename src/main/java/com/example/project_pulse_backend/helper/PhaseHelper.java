package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.EarningTransactionRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PhaseHelper {
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final AllocationRepo allocationRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final ProjectHelper projectHelper;

    public List<Phase> getPhasesByProject(Long projectId, User user, String keyword) {
        projectHelper.checkAccessToProject(projectId, user);
        return phaseRepo.filterPhase(projectId, keyword);
    }

    public Phase getPhaseById(Long id, User user) {
        Phase phase = phaseRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build());
        projectHelper.checkAccessToProject(phase.getProject().getId(), user);
        return phase;
    }

    public void checkPhaseNotDone(Phase phase) {
        if (phase.getStatus() == PhaseStatus.DONE) {
            throw AppException.builder().appError(AppError.PHASE_ALREADY_DONE).build();
        }
    }

    public List<LocalDate> normalizeDates(List<LocalDate> dates) {
        return dates == null ? List.of() : new ArrayList<>(dates);
    }

    public void checkSpecialDates(
            LocalDate startAt,
            LocalDate endAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {

        Set<LocalDate> specialDates = new HashSet<>();
        for (LocalDate date : makeUpDates) {
            if (date == null || date.isBefore(startAt) || date.isAfter(endAt)
                    || !specialDates.add(date) || !TimeHelper.isWeekend(date)) {
                throw AppException.builder().appError(AppError.PHASE_INVALID_SPECIAL_DATE).build();
            }
        }
        for (LocalDate date : offDates) {
            if (date == null || date.isBefore(startAt) || date.isAfter(endAt)
                    || !specialDates.add(date) || TimeHelper.isWeekend(date)) {
                throw AppException.builder().appError(AppError.PHASE_INVALID_SPECIAL_DATE).build();
            }
        }
    }

    public void checkTime(Phase phase, Project project, LocalDate startAt, LocalDate endAt) {
        TimeHelper.checkStartBeforeEnd(startAt, endAt);
        TimeHelper.checkPhaseInProject(startAt, endAt, project.getStartAt(), project.getEndAt());
        if (phase != null) {
            List<Task> tasks = taskRepo.findByPhase_Id(phase.getId());
            for (Task task : tasks) {
                try {
                    TimeHelper.checkTaskInPhase(
                            task.getStartAt(), task.getEndAt(), startAt, endAt);
                } catch (AppException e) {
                    throw new AppException(AppError.HAS_TASK_OUT_OF_PHASE);
                }
            }
        }
    }

    // tiến độ phase = tổng earn trong phase / tổng task point trong phase
    public BigDecimal calculateProgress(Long phaseId) {
        BigDecimal totalTaskPoints = taskRepo.sumTaskPointsByPhaseId(phaseId);
        if (totalTaskPoints.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal earnedPoints = earningTransactionRepo.sumNetEarnedPointsByPhaseId(
                phaseId, EarningTransactionType.EARN);
        return earnedPoints.multiply(BigDecimal.valueOf(100))
                .divide(totalTaskPoints, 2, RoundingMode.HALF_UP);
    }

    public void checkBeforeDelete(Long phaseId) {
        if (!allocationRepo.findByPhase_Id(phaseId).isEmpty()) {
            throw AppException.builder().appError(AppError.PHASE_IN_USE).build();
        }
        if (!taskRepo.findByPhase_Id(phaseId).isEmpty()) {
            throw AppException.builder().appError(AppError.PHASE_IN_USE).build();
        }
    }

    public void checkInProgressTasks(Phase phase) {
        if (taskRepo.existsByPhase_IdAndTaskStatusNotIn(phase.getId(), List.of(TaskStatus.DONE, TaskStatus.CANCELED))) {
            throw new AppException(AppError.HAS_TASK_NOT_DONE);
        }
    }
}
