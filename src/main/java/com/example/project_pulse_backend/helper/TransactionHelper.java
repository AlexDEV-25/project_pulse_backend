package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.TaskAssignment;
import com.example.project_pulse_backend.entity.TaskNote;
import com.example.project_pulse_backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHelper {
    private final TaskNoteHelper taskNoteHelper;
    private final EarningTransactionHelper earningTransactionHelper;
    private final TaskCostTransactionHelper taskCostTransactionHelper;

    public void createCompletionTransactions(Task task) {
        if (task.getAssignments() == null || task.getAssignments().isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_HAS_NO_ASSIGNMENTS).build();
        }

        for (TaskAssignment assignment : task.getAssignments()) {
            ProjectMember employee = assignment.getEmployee();
            List<TaskNote> confirmedNotes = taskNoteHelper.checkTaskNotesExist(task, employee);

            earningTransactionHelper.createEarningTransaction(task, assignment, employee);
            taskCostTransactionHelper.createTaskCostTransaction(task, employee, confirmedNotes);
        }
    }


}
