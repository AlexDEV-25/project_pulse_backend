package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.dto.request.CreateProjectRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectRequest;
import com.example.project_pulse_backend.dto.response.ProjectResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.RoleHelper;
import com.example.project_pulse_backend.helper.TimeHelper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;
    private final ProjectHelper projectHelper;

    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ProjectResponse createProject(CreateProjectRequest request) {
        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        if (!RoleHelper.userHasRole(pm, "PM")) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
        TimeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        Project newProject = projectRepo.save(Project.builder()
                .projectName(request.getProjectName())
                .pm(pm)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .clientBudget(request.getClientBudget())
                .projectBudget(request.getProjectBudget())
                .description(request.getDescription())
                .projectStatus(ProjectStatus.DRAFT)
                .build());

        return toResponseForAdmin(newProject);
    }

    @PreAuthorize("hasAuthority('UPDATE_PROJECT')")
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project entity = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build());

        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        if (!RoleHelper.userHasRole(pm, "PM")) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
        TimeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        entity.setProjectName(request.getProjectName());
        entity.setPm(pm);
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setClientBudget(request.getClientBudget());
        entity.setProjectBudget(request.getProjectBudget());
        entity.setDescription(request.getDescription());
        entity.setProjectStatus(request.getProjectStatus());

        return toResponseForAdmin(projectRepo.save(entity));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    public void deleteProject(Long id) {
        Project project = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );
        /* chưa đủ điều kiện
         * - có thể đã có phase
         * - có thể đã có project member
         * */
        if (project.getProjectStatus() != ProjectStatus.DRAFT) {
            throw AppException.builder().appError(AppError.PROJECT_IN_PROGRESS).build();
        }
        projectRepo.deleteById(id);
    }

    @PreAuthorize("hasAuthority('GET_PROJECTS')")
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long pmId, String keyword, ProjectStatus status) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        User user = getUserByToken.get();

        Long filteredPmId;
        if (RoleHelper.checkIsAdmin(user)) {
            filteredPmId = pmId;
            List<Project> projects = projectRepo.searchProjects(filteredPmId, status, normalizedKeyword);
            return projects.stream().map(this::toResponseForAdmin).toList();

        } else if (RoleHelper.checkIsPm(user)) {
            filteredPmId = user.getId();
            List<Project> projects = projectRepo.searchProjects(filteredPmId, status, normalizedKeyword);
            return projects.stream().map(this::toResponseForPm).toList();

        } else if (RoleHelper.checkIsEmployee(user)) {
            return projectMemberRepo.searchByUserIdAndProjectNameAndProjectStatus(
                            user.getId(), normalizedKeyword, status).stream()
                    .map(projectMember -> toResponseForEmployee(projectMember.getProject()))
                    .toList();

        } else {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_BY_ID')")
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(id, user);

        if (RoleHelper.checkIsAdmin(user)) {
            return toResponseForAdmin(project);
        } else if (RoleHelper.checkIsPm(user)) {
            return toResponseForPm(project);
        } else {
            return toResponseForEmployee(project);
        }
    }

    private ProjectResponse toResponseForAdmin(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .pmId(project.getPm() != null ? project.getPm().getId() : null)
                .pmName(project.getPm() != null ? project.getPm().getUserName() : null)
                .startAt(project.getStartAt())
                .endAt(project.getEndAt())
                .clientBudget(project.getClientBudget())
                .projectBudget(project.getProjectBudget())
                .description(project.getDescription())
                .projectStatus(project.getProjectStatus())
                .build();
    }

    private ProjectResponse toResponseForPm(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .pmId(project.getPm() != null ? project.getPm().getId() : null)
                .pmName(project.getPm() != null ? project.getPm().getUserName() : null)
                .startAt(project.getStartAt())
                .endAt(project.getEndAt())
                .clientBudget(null)
                .projectBudget(project.getProjectBudget())
                .description(project.getDescription())
                .projectStatus(project.getProjectStatus())
                .build();
    }

    private ProjectResponse toResponseForEmployee(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .pmId(project.getPm() != null ? project.getPm().getId() : null)
                .pmName(project.getPm() != null ? project.getPm().getUserName() : null)
                .startAt(project.getStartAt())
                .endAt(project.getEndAt())
                .description(project.getDescription())
                .projectStatus(project.getProjectStatus())
                .build();
    }
}
