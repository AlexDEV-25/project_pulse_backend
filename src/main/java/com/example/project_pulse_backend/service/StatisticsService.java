package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    private static final int NEARING_DEADLINE_DAYS = 7;
    private static final int PERCENTAGE_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private final UserRepo userRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final TaskRepo taskRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final EvmHelper evmHelper;
    private final GetUserByToken getUserByToken;
    private final ProjectHelper projectHelper;

    @PreAuthorize("hasAuthority('GET_STATISTICS_SUMMARY')")
    public StatisticsSummaryResponse getSummary() {
        User user = getUserByToken.get();

        List<Project> projects = projectHelper.getProjectForAdminAndPm(user);
        LocalDate now = LocalDate.now();
        LocalDate nearingDeadline = now.plusDays(NEARING_DEADLINE_DAYS);

        long overdue = projects.stream()
                .filter(StatisticsHelper::isNotFinished)
                .filter(project -> project.getEndAt() != null && project.getEndAt().isBefore(now))
                .count();
        long nearing = projects.stream()
                .filter(StatisticsHelper::isNotFinished)
                .filter(project -> project.getEndAt() != null
                        && !project.getEndAt().isBefore(now)
                        && !project.getEndAt().isAfter(nearingDeadline))
                .count();

        return StatisticsSummaryResponse.builder()
                .totalProject(projects.size())
                .totalUser(RoleHelper.checkIsAdmin(user) ? userRepo.count() : null)
                .totalOverdueProject(overdue)
                .totalNearingDeadlineProject(nearing)
                .build();
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_STATUS_STATISTICS')")
    public Map<ProjectStatus, ProjectStatusStatisticResponse> getProjectStatusStatistics() {
        User user = getUserByToken.get();

        List<Project> projects = projectHelper.getProjectForAdminAndPm(user);

        Map<ProjectStatus, Long> counts = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            counts.put(status, 0L);
        }
        for (Project project : projects) {
            counts.merge(project.getProjectStatus(), 1L, Long::sum);
        }

        Map<ProjectStatus, ProjectStatusStatisticResponse> statusStatistics =
                new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            long count = counts.get(status);
            BigDecimal percentage = projects.isEmpty()
                    ? BigDecimal.ZERO.setScale(PERCENTAGE_SCALE, ROUNDING_MODE)
                    : BigDecimal.valueOf(count * 100)
                      .divide(BigDecimal.valueOf(projects.size()), PERCENTAGE_SCALE, ROUNDING_MODE);
            statusStatistics.put(status, ProjectStatusStatisticResponse.builder()
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        return statusStatistics;
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_OVERVIEW_STATISTICS')")
    public ProjectOverviewStatisticsResponse getProjectOverviewStatistics() {
        User user = getUserByToken.get();

        List<Project> projects = projectHelper.getProjectForAdminAndPm(user);

        Map<ProjectStatus, Long> counts = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            counts.put(status, 0L);
        }
        for (Project project : projects) {
            counts.merge(project.getProjectStatus(), 1L, Long::sum);
        }

        return ProjectOverviewStatisticsResponse.builder()
                .totalProject(projects.size())
                .projectStatusCounts(counts)
                .build();
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_EV_AC_STATISTICS')")
    public Map<String, ProjectEvAcStatisticResponse> getProjectEvAcStatistics() {
        User user = getUserByToken.get();

        List<Project> projects = projectHelper.getProjectForAdminAndPm(user);
        Map<String, ProjectEvAcStatisticResponse> evAcByProject = new LinkedHashMap<>();

        for (Project project : projects) {
            evAcByProject.put(project.getProjectName(), ProjectEvAcStatisticResponse.builder()
                    .ev(earningTransactionRepo.sumNetEarnedPointsByProjectId(
                            project.getId(), EarningTransactionType.EARN))
                    .ac(taskCostTransactionRepo.sumCostPointsByProjectId(project.getId()))
                    .build());
        }
        return evAcByProject;
    }

    @PreAuthorize("hasAuthority('GET_IN_PROGRESS_PROJECT_STATISTICS')")
    public List<InProgressProjectStatisticsResponse> getInProgressProjects() {
        User user = getUserByToken.get();

        String role = RoleHelper.checkIsAdmin(user) ? "ADMIN" : "PM";

        List<Project> projects = projectHelper.getProjectForAdminAndPm(user).stream()
                .filter(project -> project.getProjectStatus() == ProjectStatus.IN_PROGRESS).toList();

        LocalDate now = LocalDate.now();

        return projects.stream()
                .map(project -> toInProgressResponse(project, role, now)).toList();
    }

    @PreAuthorize("hasAuthority('GET_EMPLOYEE_STATISTICS_SUMMARY')")
    public EmployeeStatisticsSummaryResponse getEmployeeSummary() {
        User user = getUserByToken.get();
        List<Task> assignedTasks = taskRepo.findAssignedTasksWithDetails(user.getId());

        return EmployeeStatisticsSummaryResponse.builder()
                .totalProject(projectMemberRepo.findByUser_Id(user.getId()).size())
                .totalEarnedPoint(earningTransactionRepo.sumNetEarnedPointsByUserId(
                        user.getId(),
                        EarningTransactionType.EARN,
                        EarningTransactionType.TRANSFER))
                .totalAssignedTask(assignedTasks.size())
                .totalCompletedTask(assignedTasks.stream()
                        .filter(task -> task.getTaskStatus() == TaskStatus.DONE)
                        .count())
                .build();
    }

    @PreAuthorize("hasAuthority('GET_EMPLOYEE_PROJECT_POINT_STATISTICS')")
    public Map<String, BigDecimal> getEmployeeProjectPointStatistics() {
        User user = getUserByToken.get();
        Map<String, BigDecimal> pointsByProject = new LinkedHashMap<>();

        for (ProjectMember member : projectMemberRepo.findByUser_Id(user.getId())) {
            Project project = member.getProject();
            pointsByProject.put(project.getProjectName(),
                    earningTransactionRepo.sumNetEarnedPointsByUserIdAndProjectId(
                            user.getId(),
                            project.getId(),
                            EarningTransactionType.EARN,
                            EarningTransactionType.TRANSFER));
        }
        return pointsByProject;
    }

    @PreAuthorize("hasAuthority('GET_EMPLOYEE_TASK_STATISTICS')")
    public List<EmployeeTaskStatisticsResponse> getEmployeeTasks() {
        User user = getUserByToken.get();
        LocalDate now = LocalDate.now();
        LocalDate nearingDeadline = now.plusDays(NEARING_DEADLINE_DAYS);

        return taskRepo.findAssignedTasksWithDetails(user.getId()).stream()
                .filter(task -> StatisticsHelper.isInProgressOrNearingDeadline(task, now, nearingDeadline))
                .map(task -> EmployeeTaskStatisticsResponse.builder()
                        .projectName(task.getPhase().getProject().getProjectName())
                        .taskName(task.getTaskName())
                        .endAt(task.getEndAt())
                        .taskPoint(task.getTaskPoint())
                        .progress(task.getAssignments() == null ?
                                0 : task.getAssignments().stream()
                                    .mapToInt(TaskAssignment::getContributionPercentage)
                                    .sum())
                        .build())
                .toList();
    }

    private InProgressProjectStatisticsResponse toInProgressResponse(
            Project project, String role, LocalDate now) {
        BigDecimal ev = earningTransactionRepo.sumNetEarnedPointsByProjectId(
                project.getId(), EarningTransactionType.EARN);
        BigDecimal ac = taskCostTransactionRepo.sumCostPointsByProjectId(project.getId());
        BigDecimal pv = evmHelper.pv(project.getId(), now);
        BigDecimal spi = evmHelper.spi(ev, pv);

        return InProgressProjectStatisticsResponse.builder()
                .projectName(project.getProjectName())
                .pmName(role.equals("ADMIN") ? project.getPm().getUserName() : null)
                .endAt(project.getEndAt())
                .clientBudget(role.equals("ADMIN") ? project.getClientBudget() : null)
                .bac(project.getProjectBudget())
                .cpi(evmHelper.cpi(ev, ac))
                .spi(spi)
                .scheduleStatus(StatisticsHelper.scheduleStatus(spi))
                .build();
    }
}
