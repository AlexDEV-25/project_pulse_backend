package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.ProjectResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.ProjectMapper;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsUpdatesDeletesAndReadsProjects() {
        TestAuthentication.selectRole("PM");
        ProjectRepo repository = mock(ProjectRepo.class);
        ProjectMemberRepo members = mock(ProjectMemberRepo.class);
        UserRepo users = mock(UserRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectHelper helper = mock(ProjectHelper.class);
        ProjectMapper mapper = mock(ProjectMapper.class);
        User pm = User.builder().id(5L).roles(List.of(Role.builder().name("PM").build())).build();
        Project project = Project.builder().id(10L).projectName("Atlas").build();
        ProjectResponse response = ProjectResponse.builder().id(10L).projectName("Atlas").build();
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 30);
        CreateProjectRequest create = CreateProjectRequest.builder().projectName("Atlas").pmId(5L)
                .startAt(start).endAt(end).build();
        when(users.findById(5L)).thenReturn(Optional.of(pm));
        when(mapper.toProject(create)).thenReturn(project);
        when(repository.save(project)).thenReturn(project);
        when(mapper.toProjectResponse(project)).thenReturn(response);
        when(repository.findById(10L)).thenReturn(Optional.of(project));
        when(mapper.toProjectResponseForPm(project)).thenReturn(response);
        when(token.get()).thenReturn(pm);
        when(helper.getProjectById(10L, pm)).thenReturn(project);
        ProjectService service = new ProjectService(repository, members, users, token, helper, mapper);

        assertEquals(response, service.createProject(create));
        UpdateProjectRequest update = UpdateProjectRequest.builder().pmId(5L)
                .startAt(start).endAt(end).projectStatus(ProjectStatus.IN_PROGRESS).build();
        assertEquals(response, service.updateProject(10L, update));
        assertEquals(response, service.getProjectById(10L));
        service.deleteProject(10L);
        verify(helper).checkTime(project);
        verify(repository).delete(project);
        verify(repository, times(2)).save(project);
    }

    @Test
    void listProjectsForAdminAndPmUsesTheirOwnFilter() {
        ProjectRepo repository = mock(ProjectRepo.class);
        ProjectMemberRepo members = mock(ProjectMemberRepo.class);
        UserRepo users = mock(UserRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectMapper mapper = mock(ProjectMapper.class);
        Project project = Project.builder().id(2L).build();
        ProjectResponse response = ProjectResponse.builder().id(2L).build();
        when(repository.filterProjects(9L, ProjectStatus.DRAFT, "needle")).thenReturn(List.of(project));
        when(mapper.toProjectResponse(project)).thenReturn(response);
        when(mapper.toProjectResponseForPm(project)).thenReturn(response);
        ProjectService service = new ProjectService(repository, members, users, token,
                mock(ProjectHelper.class), mapper);

        User admin = User.builder().id(9L).roles(List.of(Role.builder().name("ADMIN").build())).build();
        TestAuthentication.selectRole("ADMIN");
        when(token.get()).thenReturn(admin);
        assertEquals(List.of(response), service.getAllProjects(9L, " needle ", ProjectStatus.DRAFT));
        verify(repository).filterProjects(9L, ProjectStatus.DRAFT, "needle");

        User pm = User.builder().id(4L).roles(List.of(Role.builder().name("PM").build())).build();
        TestAuthentication.selectRole("PM");
        when(token.get()).thenReturn(pm);
        when(repository.filterProjects(4L, null, null)).thenReturn(List.of(project));
        assertEquals(List.of(response), service.getAllProjects(99L, null, null));
        verify(repository).filterProjects(4L, null, null);
    }
}
