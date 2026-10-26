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
import com.example.project_pulse_backend.mapper.ProjectMapper;
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
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;
    private final ProjectHelper projectHelper;
    private final ProjectMapper projectMapper;

    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        if (!RoleHelper.userHasRole(pm, "PM")) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
        TimeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        Project project = projectMapper.toProject(request);
        project.setPm(pm);
        project.setProjectStatus(ProjectStatus.DRAFT);
        Project newProject = projectRepo.save(project);

        return toResponseForAdmin(newProject);
    }

    @PreAuthorize("hasAuthority('UPDATE_PROJECT')")
    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project entity = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build());

        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        if (!RoleHelper.userHasRole(pm, "PM")) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
        TimeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        projectMapper.updateProject(request, entity);
        entity.setPm(pm);

        if (request.getProjectStatus() == ProjectStatus.DONE) {
            projectHelper.checkInProgressPhases(entity);
        }
        entity.setProjectStatus(request.getProjectStatus());

        projectHelper.checkTime(entity);

        return toResponseForAdmin(projectRepo.save(entity));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build());
        projectHelper.checkProjectNotDone(project);
        projectHelper.checkBeforeDelete(id);
        projectRepo.delete(project);
    }

    @PreAuthorize("hasAuthority('GET_PROJECTS')")
    public List<ProjectResponse> getAllProjects(Long pmId, String keyword, ProjectStatus status) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        User user = getUserByToken.get();

        Long filteredPmId;
        if (RoleHelper.checkIsAdmin(user)) {
            filteredPmId = pmId;
            List<Project> projects = projectRepo.filterProjects(filteredPmId, status, normalizedKeyword);
            return projects.stream().map(this::toResponseForAdmin).toList();

        } else if (RoleHelper.checkIsPm(user)) {
            filteredPmId = user.getId();
            List<Project> projects = projectRepo.filterProjects(filteredPmId, status, normalizedKeyword);
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
        return projectMapper.toProjectResponse(project);
    }

    private ProjectResponse toResponseForPm(Project project) {
        return projectMapper.toProjectResponseForPm(project);
    }

    private ProjectResponse toResponseForEmployee(Project project) {
        return projectMapper.toProjectResponseForEmployee(project);
    }
}
