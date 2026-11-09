package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.TaskNoteRepo;
import com.example.project_pulse_backend.repository.TaskRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskNoteHelperTest {
    private final TaskRepo taskRepo = mock(TaskRepo.class);
    private final TaskNoteRepo taskNoteRepo = mock(TaskNoteRepo.class);
    private final ProjectHelper projectHelper = mock(ProjectHelper.class);
    private final NotificationHelper notificationHelper = mock(NotificationHelper.class);
    private final TaskNoteHelper helper =
            new TaskNoteHelper(taskRepo, taskNoteRepo, projectHelper, notificationHelper);

    @Test
    void checksStatusOwnershipAndConfirmedNotes() {
        TaskNote pending = TaskNote.builder().taskNoteStatus(TaskNoteStatus.PENDING_CONFIRMATION).build();
        helper.checkPendingConfirmation(pending);
        assertThrows(AppException.class, () -> helper.checkPendingConfirmation(
                TaskNote.builder().taskNoteStatus(TaskNoteStatus.CONFIRMED).build()));

        User author = User.builder().id(1L).build();
        ProjectMember member = ProjectMember.builder().user(author).build();
        helper.checkAuthor(TaskNote.builder().author(member).build(), author);
        assertThrows(AppException.class, () -> helper.checkAuthor(
                TaskNote.builder().author(member).build(), User.builder().id(2L).build()));

        Task task = Task.builder().id(9L).build();
        TaskNote confirmed = TaskNote.builder().taskNoteStatus(TaskNoteStatus.CONFIRMED).build();
        when(taskNoteRepo.findByTask_IdAndAuthor_IdAndTaskNoteStatusOrderByWorkingDateAsc(
                9L, 3L, TaskNoteStatus.CONFIRMED)).thenReturn(List.of(confirmed));
        assertEquals(List.of(confirmed), helper.checkTaskNotesExist(
                task, ProjectMember.builder().id(3L).build()));
        when(taskNoteRepo.findByTask_IdAndAuthor_IdAndTaskNoteStatusOrderByWorkingDateAsc(
                9L, 3L, TaskNoteStatus.CONFIRMED)).thenReturn(List.of());
        assertThrows(AppException.class, () -> helper.checkTaskNotesExist(
                task, ProjectMember.builder().id(3L).build()));
    }

    @Test
    void retrievesNotesAfterProjectAuthorization() {
        User user = User.builder().id(5L).build();
        Task task = Task.builder().id(9L).phase(Phase.builder()
                .project(Project.builder().id(4L).build()).build()).build();
        TaskNote note = TaskNote.builder().build();
        when(taskRepo.findById(9L)).thenReturn(Optional.of(task));
        when(taskNoteRepo.findByTask_IdOrderByCreatedAtAsc(9L)).thenReturn(List.of(note));

        assertEquals(List.of(note), helper.getByTask(9L, user));
        verify(projectHelper).checkAccessToProject(4L, user);
        when(taskRepo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getByTask(9L, user));
    }

    @Test
    void createsNoteNotificationAndNotifiesProjectManager() {
        User pm = User.builder().id(8L).build();
        User employee = User.builder().id(7L).userName("Employee").build();
        Task task = Task.builder().id(9L).taskName("Review").phase(Phase.builder()
                .project(Project.builder().id(4L).pm(pm).build()).build()).build();
        TaskNote note = TaskNote.builder().task(task)
                .author(ProjectMember.builder().user(employee).build()).build();
        Notification notification = Notification.builder().build();
        when(notificationHelper.createNotification(any())).thenReturn(notification);

        helper.createTaskNoteNotification(note, employee);

        verify(notificationHelper).createNotification(any());
        verify(notificationHelper).createUserNotification(argThat(request ->
                request.getSender() == employee && request.getReceiver() == pm
                        && request.getNotification() == notification));
    }
}
