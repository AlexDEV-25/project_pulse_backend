package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.request.CreateProjectMemberRequest;
import com.example.project_pulse_backend.dto.response.ProjectMemberResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.ProjectMemberMapper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectMemberServiceTest {
    @Test
    void listsMembersAfterAccessCheck() {
        ProjectMemberRepo repository = mock(ProjectMemberRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        GetUserByToken token = mock(GetUserByToken.class);
        User user = User.builder().id(1L).build();
        ProjectMember member = ProjectMember.builder().id(2L).build();
        ProjectMemberResponse response = ProjectMemberResponse.builder().id(2L).build();
        when(repository.filterProjectMember(8L, "design")).thenReturn(List.of(member));
        when(token.get()).thenReturn(user);
        ProjectMemberMapper mapper = mock(ProjectMemberMapper.class);
        when(mapper.toProjectMemberResponse(member)).thenReturn(response);
        ProjectMemberService service = new ProjectMemberService(repository, projectHelper,
                mock(UserRepo.class), token, mock(ProjectMemberHelper.class), mapper);

        assertEquals(List.of(response), service.getMyProjectMembers(8L, "design"));
        verify(projectHelper).checkAccessToProject(8L, user);
    }

    @Test
    void createsAndDeletesProjectMembership() {
        ProjectMemberRepo repository = mock(ProjectMemberRepo.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        UserRepo users = mock(UserRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        ProjectMemberHelper memberHelper = mock(ProjectMemberHelper.class);
        ProjectMemberMapper mapper = mock(ProjectMemberMapper.class);
        User pm = User.builder().id(1L).build();
        User employee = User.builder().id(3L).roles(List.of(Role.builder().name("EMPLOYEE").build())).build();
        Project project = Project.builder().id(8L).build();
        ProjectMember member = ProjectMember.builder().id(4L).build();
        ProjectMemberResponse response = ProjectMemberResponse.builder().id(4L).build();
        when(token.get()).thenReturn(pm);
        when(projectHelper.getProjectById(8L, pm)).thenReturn(project);
        when(users.findById(3L)).thenReturn(Optional.of(employee));
        when(repository.existsByProject_IdAndUser_Id(8L, 3L)).thenReturn(false);
        when(mapper.toProjectMember(any())).thenReturn(member);
        when(repository.save(member)).thenReturn(member);
        when(mapper.toProjectMemberResponse(member)).thenReturn(response);
        when(memberHelper.getProjectMemberById(4L, pm)).thenReturn(member);
        ProjectMemberService service = new ProjectMemberService(repository, projectHelper, users,
                token, memberHelper, mapper);

        assertEquals(response, service.createProjectMember(CreateProjectMemberRequest.builder()
                .projectId(8L).userId(3L).build()));
        service.deleteProjectMember(4L);
        verify(projectHelper, times(2)).checkProjectNotDone(project);
        verify(memberHelper).checkBeforeDeleteProjectMember(4L);
        verify(repository).delete(member);
    }

}
