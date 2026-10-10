package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.Task;
import com.example.project_pulse_backend.entity.TaskNote;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.TaskNoteRepo;
import com.example.project_pulse_backend.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskNoteHelper {
    private final TaskRepo taskRepo;
    private final TaskNoteRepo taskNoteRepo;
    private final ProjectHelper projectHelper;


    public void checkPendingConfirmation(TaskNote taskNote) {
        if (taskNote.getTaskNoteStatus() != TaskNoteStatus.PENDING_CONFIRMATION) {
            throw AppException.builder().appError(AppError.TASK_NOTE_NOT_PENDING).build();
        }
    }

    public List<TaskNote> getByTask(Long taskId, User user) {
        Task task = taskRepo.findById(taskId).orElseThrow(
                () -> AppException.builder().appError(AppError.TASK_NOT_FOUND).build());
        projectHelper.checkAccessToProject(task.getPhase().getProject().getId(), user);
        return taskNoteRepo.findByTask_IdOrderByCreatedAtAsc(task.getId());
    }

    public void checkAuthor(TaskNote taskNote, User user) {
        if (!taskNote.getAuthor().getUser().getId().equals(user.getId())) {
            throw AppException.builder().appError(AppError.TASK_NOTE_NOT_FOUND).build();
        }
    }

    public List<TaskNote> checkTaskNotesExist(Task task, ProjectMember employee) {
        List<TaskNote> confirmedNotes = taskNoteRepo
                .findByTask_IdAndAuthor_IdAndTaskNoteStatusOrderByWorkingDateAsc(
                        task.getId(), employee.getId(), TaskNoteStatus.CONFIRMED);
        if (confirmedNotes.isEmpty()) {
            throw AppException.builder().appError(AppError.TASK_NOTE_NOT_FOUND).build();
        }
        return confirmedNotes;
    }
}
