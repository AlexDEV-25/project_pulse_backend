package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.TestAuthentication;
import com.example.project_pulse_backend.mapper.ProjectMapper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceEmployeeProjectsTest {
    @Mock
    private ProjectRepo projectRepo;
    @Mock
    private ProjectMemberRepo projectMemberRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private GetUserByToken getUserByToken;
    @Mock
    private ProjectHelper projectHelper;

    private ProjectService projectService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(
                projectRepo, projectMemberRepo, userRepo, getUserByToken, projectHelper,
                Mappers.getMapper(ProjectMapper.class));
    }

    @Test
    void getAllProjects_shouldReturnOnlyProjectsOfAuthenticatedEmployeeWithoutBudgetsAndFilterByStatus() {
        TestAuthentication.selectRole("EMPLOYEE");
        User employee = User.builder().id(42L)
                .roles(List.of(Role.builder().name("EMPLOYEE").build())).build();
        User pm = User.builder().id(7L).userName("Project Manager").build();
        Project project = Project.builder().id(10L).projectName("Project A").pm(pm)
                .projectStatus(ProjectStatus.IN_PROGRESS)
                .clientBudget(new BigDecimal("1000"))
                .projectBudget(new BigDecimal("800"))
                .build();

        when(getUserByToken.get()).thenReturn(employee);
        when(projectMemberRepo.searchByUserIdAndProjectNameAndProjectStatus(
                42L, null, ProjectStatus.IN_PROGRESS))
                .thenReturn(List.of(ProjectMember.builder().project(project).user(employee).build()));

        var projects = projectService.getAllProjects(null, null, ProjectStatus.IN_PROGRESS);

        assertEquals(1, projects.size());
        assertEquals(10L, projects.get(0).getId());
        assertEquals("Project A", projects.get(0).getProjectName());
        assertEquals(7L, projects.get(0).getPmId());
        assertEquals("Project Manager", projects.get(0).getPmName());
        assertNull(projects.get(0).getClientBudget());
        assertNull(projects.get(0).getProjectBudget());
        verify(projectMemberRepo).searchByUserIdAndProjectNameAndProjectStatus(
                42L, null, ProjectStatus.IN_PROGRESS);
    }

    @Test
    void getAllProjects_shouldTrimKeywordAndSearchProjectNameForEmployee() {
        TestAuthentication.selectRole("EMPLOYEE");
        User employee = User.builder().id(42L)
                .roles(List.of(Role.builder().name("EMPLOYEE").build())).build();
        when(getUserByToken.get()).thenReturn(employee);
        when(projectMemberRepo.searchByUserIdAndProjectNameAndProjectStatus(42L, "Apollo", null))
                .thenReturn(List.of());

        var projects = projectService.getAllProjects(null, "  Apollo  ", null);

        assertTrue(projects.isEmpty());
        verify(projectMemberRepo).searchByUserIdAndProjectNameAndProjectStatus(42L, "Apollo", null);
    }

    @Test
    void getAllProjects_shouldDenyUnsupportedRole() {
        TestAuthentication.selectRole("GUEST");
        User guest = User.builder().id(7L)
                .roles(List.of(Role.builder().name("GUEST").build())).build();
        when(getUserByToken.get()).thenReturn(guest);

        AppException exception = assertThrows(
                AppException.class, () -> projectService.getAllProjects(null, null, null));

        assertEquals(AppError.ACCESS_DENIED, exception.getAppError());
        verifyNoInteractions(projectMemberRepo);
    }

    @Test
    void getProjectById_shouldReturnProjectForEmployeeWithoutBudgets() {
        TestAuthentication.selectRole("EMPLOYEE");
        User employee = User.builder().id(42L)
                .roles(List.of(Role.builder().name("EMPLOYEE").build())).build();
        Project project = Project.builder().id(10L).projectName("Project A")
                .projectStatus(ProjectStatus.IN_PROGRESS)
                .clientBudget(new BigDecimal("1000"))
                .projectBudget(new BigDecimal("800"))
                .build();
        when(getUserByToken.get()).thenReturn(employee);
        when(projectHelper.getProjectById(10L, employee)).thenReturn(project);

        var response = projectService.getProjectById(10L);

        assertEquals(10L, response.getId());
        assertEquals("Project A", response.getProjectName());
        assertNull(response.getClientBudget());
        assertNull(response.getProjectBudget());
        verify(projectHelper).getProjectById(10L, employee);
    }
}
