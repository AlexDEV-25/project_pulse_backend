package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.TaskAssignmentRequest;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.TaskAssignment;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.TaskAssignmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TaskAssignmentHelper {

    private final ProjectMemberRepo projectMemberRepo;
    private final TaskAssignmentRepo taskAssignmentRepo;

    public List<TaskAssignment> createAssignments(
            Task task, Project project, List<TaskAssignmentRequest> requests) {
        Set<Long> employeeIds = new HashSet<>();
        List<TaskAssignment> assignments = new ArrayList<>();
        for (TaskAssignmentRequest request : requests) {
            if (!employeeIds.add(request.getEmployeeId())) {
                throw AppException.builder().appError(AppError.TASK_ASSIGNMENT_ALREADY_EXISTS).build();
            }

            ProjectMember member = projectMemberRepo.findByProject_IdAndUser_Id(
                            project.getId(), request.getEmployeeId())
                    .orElseThrow(() -> AppException.builder().appError(AppError.MEMBER_NOT_FOUND).build());

            assignments.add(TaskAssignment.builder()
                    .task(task)
                    .employee(member)
                    .contributionPercentage(request.getContributionPercentage())
                    .build());
        }
        return assignments;
    }

    public void checkBelongToTask(Long taskId, ProjectMember member) {
        boolean isBelong = taskAssignmentRepo.findByTask_Id(taskId).stream().anyMatch(
                taskAssignment -> taskAssignment.getEmployee().getId().equals(member.getId()));
        if (!isBelong) {
            throw AppException.builder().appError(AppError.USER_NOT_BELONG_TO_TASK).build();
        }
    }
}
