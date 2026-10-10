package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.TaskCostTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskCostTransactionHelper {
    private static final BigDecimal HOURS_PER_WORKDAY = BigDecimal.valueOf(8);
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final AllocationRepo allocationRepo;
    private final ProjectHelper projectHelper;

    public List<TaskCostTransaction> getByProjectId(Long projectId, User user, String keyword, Long phaseId) {

        Project project = projectHelper.getProjectById(projectId, user);
        return taskCostTransactionRepo.findByProjectIdAndFilters(project.getId(), phaseId, keyword);
    }

    public TaskCostTransaction getById(Long id, User user) {

        TaskCostTransaction taskCostTransaction = taskCostTransactionRepo.findById(id)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_COST_TRANSACTION_NOT_FOUND).build());
        projectHelper.checkAccessToProject(taskCostTransaction.getTask().getPhase().getProject().getId(), user);
        return taskCostTransaction;
    }


    public void createTaskCostTransaction(Task task, ProjectMember employee, List<TaskNote> confirmedNotes) {
        BigDecimal workdays = BigDecimal.ZERO;
        BigDecimal costPoints = BigDecimal.ZERO;

        for (TaskNote note : confirmedNotes) {
            Allocation allocation = allocationRepo.findByPhase_IdAndMember_Id(
                    task.getPhase().getId(), employee.getId()).orElseThrow(() -> AppException.builder()
                    .appError(AppError.ALLOCATION_NOT_FOUND).build());
            if (allocation.getResourceRateSnapshot() == null) {
                throw AppException.builder().appError(AppError.ALLOCATION_RATE_NOT_FOUND).build();
            }

            BigDecimal dayFraction = BigDecimal.valueOf(note.getWorkingHours())
                    .divide(HOURS_PER_WORKDAY, 2, RoundingMode.HALF_UP);

            workdays = workdays.add(dayFraction);
            costPoints = costPoints.add(dayFraction.multiply(allocation.getResourceRateSnapshot()));
        }

        taskCostTransactionRepo.save(TaskCostTransaction.builder()
                .task(task)
                .executor(employee)
                .workdays(workdays)
                .points(costPoints)
                .reason("Chi phí hoàn thành task: " + task.getTaskName())
                .build());
    }

}
