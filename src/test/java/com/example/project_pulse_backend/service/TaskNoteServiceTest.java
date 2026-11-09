package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.TaskNoteStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.TaskNoteResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.TaskNoteMapper;
import com.example.project_pulse_backend.repository.TaskNoteRepo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskNoteServiceTest {
    @Test
    void createsUpdatesDeletesListsAndConfirmsNotes() {
        TaskNoteRepo repository = mock(TaskNoteRepo.class);
        TaskHelper taskHelper = mock(TaskHelper.class);
        GetUserByToken token = mock(GetUserByToken.class);
        TaskNoteHelper noteHelper = mock(TaskNoteHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        ProjectMemberHelper memberHelper = mock(ProjectMemberHelper.class);
        TaskNoteMapper mapper = mock(TaskNoteMapper.class);
        User user = User.builder().id(1L).build();
        Project project = Project.builder().id(2L).build();
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);
        Phase phase = Phase.builder().project(project).build();
        Task task = Task.builder().id(3L).phase(phase).taskStatus(TaskStatus.IN_PROGRESS)
                .startAt(start).endAt(end).build();
        ProjectMember author = ProjectMember.builder().id(4L).build();
        TaskNote note = TaskNote.builder().id(5L).task(task).author(author)
                .workingDate(LocalDate.now()).build();
        TaskNoteResponse response = TaskNoteResponse.builder().id(5L).build();
        when(token.get()).thenReturn(user);
        when(taskHelper.getById(3L, user)).thenReturn(task);
        when(memberHelper.findByProjectAndUser(project, 1L, user)).thenReturn(author);
        when(mapper.toTaskNote(any())).thenReturn(note);
        when(repository.save(note)).thenReturn(note);
        when(mapper.toTaskNoteResponse(note)).thenReturn(response);
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(note));
        when(noteHelper.getByTask(3L, user)).thenReturn(List.of(note));
        TaskNoteService service = new TaskNoteService(repository, taskHelper, token, noteHelper,
                projectHelper, memberHelper, mapper);

        assertEquals(response, service.createTaskNote(CreateTaskNoteRequest.builder().taskId(3L)
                .workingDate(LocalDate.now()).build()));
        assertEquals(TaskNoteStatus.PENDING_CONFIRMATION, note.getTaskNoteStatus());
        assertEquals(response, service.updateTaskNote(5L, UpdateTaskNoteRequest.builder()
                .workingDate(LocalDate.now()).build()));
        assertEquals(List.of(response), service.getTaskNotesByTask(3L));
        service.deleteTaskNote(5L);
        assertEquals(response, service.confirmTaskNotes(5L));
        assertEquals(TaskNoteStatus.CONFIRMED, note.getTaskNoteStatus());
        verify(repository).delete(note);
        verify(projectHelper).checkAccessToProject(2L, user);
    }

    @Test
    void noteCreationRejectsTasksThatAreNotInProgress() {
        TaskHelper taskHelper = mock(TaskHelper.class);
        GetUserByToken token = mock(GetUserByToken.class);
        User user = User.builder().id(1L).build();
        Task task = Task.builder().taskStatus(TaskStatus.TODO).build();
        when(token.get()).thenReturn(user);
        when(taskHelper.getById(3L, user)).thenReturn(task);
        TaskNoteService service = new TaskNoteService(mock(TaskNoteRepo.class), taskHelper, token,
                mock(TaskNoteHelper.class), mock(ProjectHelper.class), mock(ProjectMemberHelper.class),
                mock(TaskNoteMapper.class));

        assertThrows(RuntimeException.class, () -> service.createTaskNote(
                CreateTaskNoteRequest.builder().taskId(3L).build()));
    }
}
