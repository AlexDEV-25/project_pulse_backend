package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatisticsServiceCoverageTest {
    private final UserRepo userRepo = mock(UserRepo.class);
    private final ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
    private final TaskRepo taskRepo = mock(TaskRepo.class);
    private final EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
    private final TaskCostTransactionRepo costRepo = mock(TaskCostTransactionRepo.class);
    private final EvmHelper evmHelper = mock(EvmHelper.class);
    private final GetUserByToken currentUser = mock(GetUserByToken.class);
    private final ProjectHelper projectHelper = mock(ProjectHelper.class);
    private final StatisticsService service = new StatisticsService(userRepo, memberRepo, taskRepo,
            earningRepo, costRepo, evmHelper, currentUser, projectHelper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void summaryAndProjectStatusStatisticsIncludeEmptyAndPopulatedStates() {
        TestAuthentication.selectRole("ADMIN");
        User admin = User.builder().id(1L).roles(List.of(Role.builder().name("ADMIN").build())).build();
        Project overdue = Project.builder().id(1L).endAt(LocalDate.now().minusDays(1))
                .projectStatus(ProjectStatus.IN_PROGRESS).build();
        Project soon = Project.builder().id(2L).endAt(LocalDate.now().plusDays(2))
                .projectStatus(ProjectStatus.DRAFT).build();
        when(currentUser.get()).thenReturn(admin);
        when(projectHelper.getProjectForAdminAndPm(admin)).thenReturn(List.of(overdue, soon));
        when(projectHelper.isNotFinished(any(Project.class))).thenReturn(true);
        when(userRepo.count()).thenReturn(12L);

        var summary = service.getSummary();
        var status = service.getProjectStatusStatistics();

        assertEquals(2, summary.getTotalProject());
        assertEquals(12L, summary.getTotalUser());
        assertEquals(1, summary.getTotalOverdueProject());
        assertEquals(1, summary.getTotalNearingDeadlineProject());
        assertEquals(ProjectStatus.values().length, status.size());
        assertTrue(status.containsKey(ProjectStatus.DONE));
        verify(userRepo).count();

        when(projectHelper.getProjectForAdminAndPm(admin)).thenReturn(List.of());
        assertTrue(service.getProjectStatusStatistics().values().stream()
                .allMatch(stat -> stat.getPercentage().equals(new BigDecimal("0.00"))));
    }

    @Test
    void projectOverviewEvAcAndInProgressStatisticsAreComputed() {
        TestAuthentication.selectRole("ADMIN");
        User admin = User.builder().id(1L).roles(List.of(Role.builder().name("ADMIN").build())).build();
        Project project = Project.builder().id(4L).projectName("Atlas")
                .projectStatus(ProjectStatus.IN_PROGRESS).endAt(LocalDate.now().plusDays(4))
                .pm(User.builder().userName("Morgan").build())
                .clientBudget(new BigDecimal("500")).projectBudget(new BigDecimal("400")).build();
        when(currentUser.get()).thenReturn(admin);
        when(projectHelper.getProjectForAdminAndPm(admin)).thenReturn(List.of(project));
        when(earningRepo.sumNetEarnedPointsByProjectId(4L, EarningTransactionType.EARN))
                .thenReturn(new BigDecimal("10"));
        when(costRepo.sumCostPointsByProjectId(4L)).thenReturn(new BigDecimal("5"));
        when(evmHelper.pv(eq(4L), any(LocalDate.class))).thenReturn(new BigDecimal("8"));
        when(evmHelper.spi(new BigDecimal("10"), new BigDecimal("8"))).thenReturn(new BigDecimal("1.25"));
        when(evmHelper.cpi(new BigDecimal("10"), new BigDecimal("5"))).thenReturn(new BigDecimal("2"));

        var overview = service.getProjectOverviewStatistics();
        Map<String, ?> evAc = service.getProjectEvAcStatistics();
        var inProgress = service.getInProgressProjects();

        assertEquals(1, overview.getTotalProject());
        assertEquals(1, evAc.size());
        assertEquals("Atlas", inProgress.getFirst().getProjectName());
        assertEquals("Morgan", inProgress.getFirst().getPmName());
        assertEquals(new BigDecimal("500"), inProgress.getFirst().getClientBudget());
        verify(evmHelper).pv(eq(4L), any(LocalDate.class));
    }

    @Test
    void employeeSummaryPointStatisticsAndTaskStatisticsUseCurrentUser() {
        TestAuthentication.selectRole("EMPLOYEE");
        User employee = User.builder().id(7L).build();
        Project project = Project.builder().id(3L).projectName("Orion").build();
        ProjectMember member = ProjectMember.builder().project(project).build();
        Task done = Task.builder().id(11L).taskName("Done").taskStatus(TaskStatus.DONE)
                .phase(Phase.builder().project(project).build()).endAt(LocalDate.now().plusDays(1))
                .taskPoint(new BigDecimal("4"))
                .assignments(List.of(TaskAssignment.builder().contributionPercentage(65).build())).build();
        Task inProgress = Task.builder().id(12L).taskName("Current").taskStatus(TaskStatus.IN_PROGRESS)
                .phase(Phase.builder().project(project).build()).endAt(LocalDate.now().plusDays(1))
                .taskPoint(new BigDecimal("6"))
                .assignments(List.of(TaskAssignment.builder().contributionPercentage(35).build())).build();
        when(currentUser.get()).thenReturn(employee);
        when(memberRepo.findByUser_Id(7L)).thenReturn(List.of(member));
        when(taskRepo.findAssignedTasksWithDetails(7L)).thenReturn(List.of(done, inProgress));
        when(earningRepo.sumNetEarnedPointsByUserId(eq(7L), any(), any())).thenReturn(new BigDecimal("9"));
        when(earningRepo.sumNetEarnedPointsByUserIdAndProjectId(eq(7L), eq(3L), any(), any()))
                .thenReturn(new BigDecimal("9"));

        var summary = service.getEmployeeSummary();
        Map<String, BigDecimal> points = service.getEmployeeProjectPointStatistics();
        var tasks = service.getEmployeeTasks();

        assertEquals(1, summary.getTotalProject());
        assertEquals(2, summary.getTotalAssignedTask());
        assertEquals(1, summary.getTotalCompletedTask());
        assertEquals(new BigDecimal("9"), points.get("Orion"));
        assertEquals(1, tasks.size());
        assertEquals(35, tasks.getFirst().getProgress());
        verify(taskRepo, times(2)).findAssignedTasksWithDetails(7L);
    }
}
