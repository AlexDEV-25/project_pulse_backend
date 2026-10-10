package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.CreateTaskNoteRequest;
import com.example.project_pulse_backend.dto.request.UpdateTaskNoteRequest;
import com.example.project_pulse_backend.dto.response.TaskNoteResponse;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.TaskNote;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.repository.TaskNoteRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskNoteService {
    private final TaskNoteRepo taskNoteRepo;
    private final TaskHelper taskHelper;
    private final GetUserByToken getUserByToken;
    private final TaskNoteHelper taskNoteHelper;
    private final ProjectHelper projectHelper;
    private final ProjectMemberHelper projectMemberHelper;
    private final TaskAssignmentHelper taskAssignmentHelper;

    @PreAuthorize("hasAuthority('CREATE_TASK_NOTE')")
    public TaskNoteResponse createTaskNote(CreateTaskNoteRequest request) {
        User user = getUserByToken.get();
        Task task = taskHelper.getById(request.getTaskId(), user);
        ProjectMember author = projectMemberHelper.findByProjectAndUser(task.getPhase().getProject(), user.getId(), user);

        taskAssignmentHelper.checkBelongToTask(task.getId(), author);

        if (task.getAssignments() == null || task.getAssignments().stream()
                .noneMatch(assignment -> assignment.getEmployee().getUser().getId().equals(user.getId()))) {
            throw AppException.builder().appError(AppError.USER_NOT_BELONG_TO_TASK).build();
        }

        if (task.getTaskStatus() != TaskStatus.IN_PROGRESS) {
            throw AppException.builder().appError(AppError.TASK_NOT_IN_PROGRESS).build();
        }


        TimeHelper.checkWorkingDateInTask(request.getWorkingDate(), task.getStartAt(), task.getEndAt());

        TaskNote taskNote = TaskNote.builder()
                .task(task).author(author)
                .content(request.getContent())
                .workingHours(request.getWorkingHours())
                .workingDate(request.getWorkingDate())
                .createdAt(LocalDateTime.now())
                .taskNoteStatus(TaskNoteStatus.PENDING_CONFIRMATION)
                .build();
        return toResponse(taskNoteRepo.save(taskNote));
    }

    @PreAuthorize("hasAuthority('UPDATE_TASK_NOTE')")
    public TaskNoteResponse updateTaskNote(Long noteId, UpdateTaskNoteRequest request) {
        User user = getUserByToken.get();
        TaskNote taskNote = taskNoteRepo.findByIdForUpdate(noteId)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOTE_NOT_FOUND).build());

        taskAssignmentHelper.checkBelongToTask(taskNote.getTask().getId(), taskNote.getAuthor());

        taskNoteHelper.checkAuthor(taskNote, user);
        taskNoteHelper.checkPendingConfirmation(taskNote);

        TimeHelper.checkWorkingDateInTask(
                request.getWorkingDate(), taskNote.getTask().getStartAt(), taskNote.getTask().getEndAt());

        taskNote.setContent(request.getContent());
        taskNote.setWorkingHours(request.getWorkingHours());
        taskNote.setWorkingDate(request.getWorkingDate());
        taskNote.setUpdatedAt(LocalDateTime.now());
        return toResponse(taskNoteRepo.save(taskNote));
    }

    @PreAuthorize("hasAuthority('DELETE_TASK_NOTE')")
    public void deleteTaskNote(Long noteId) {
        User employee = getUserByToken.get();
        TaskNote taskNote = taskNoteRepo.findByIdForUpdate(noteId)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOTE_NOT_FOUND).build());

        taskAssignmentHelper.checkBelongToTask(taskNote.getTask().getId(), taskNote.getAuthor());

        taskNoteHelper.checkAuthor(taskNote, employee);
        taskNoteHelper.checkPendingConfirmation(taskNote);
        taskNoteRepo.delete(taskNote);
    }

    @PreAuthorize("hasAuthority('GET_TASK_NOTES_BY_TASK')")
    @Transactional(readOnly = true)
    public List<TaskNoteResponse> getTaskNotesByTask(Long taskId) {
        User user = getUserByToken.get();
        return taskNoteHelper.getByTask(taskId, user).stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('CONFIRM_TASK_NOTES')")
    public TaskNoteResponse confirmTaskNotes(Long TaskNoteId) {
        User pm = getUserByToken.get();

        TaskNote taskNote = taskNoteRepo.findByIdForUpdate(TaskNoteId)
                .orElseThrow(() -> AppException.builder().appError(AppError.TASK_NOTE_NOT_FOUND).build());

        projectHelper.checkAccessToProject(taskNote.getTask().getPhase().getProject().getId(), pm);

        taskNoteHelper.checkPendingConfirmation(taskNote);
        taskNote.setTaskNoteStatus(TaskNoteStatus.CONFIRMED);
        taskNote.setConfirmedAt(LocalDateTime.now());

        return toResponse(taskNoteRepo.save(taskNote));
    }
    
    private TaskNoteResponse toResponse(TaskNote taskNote) {
        ProjectMember author = taskNote.getAuthor();
        return TaskNoteResponse.builder()
                .id(taskNote.getId())
                .taskId(taskNote.getTask().getId())
                .employeeId(author.getUser().getId())
                .employeeName(author.getUser().getUserName())
                .employeeAvatarUrl(author.getUser().getAvatarUrl())
                .content(taskNote.getContent())
                .workingHours(taskNote.getWorkingHours())
                .workingDate(taskNote.getWorkingDate())
                .createdAt(taskNote.getCreatedAt())
                .updatedAt(taskNote.getUpdatedAt())
                .taskNoteStatus(taskNote.getTaskNoteStatus())
                .build();
    }

}
