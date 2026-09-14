package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateProjectRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectRequest;
import com.example.project_pulse_backend.dto.response.ProjectResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.TimeHelper;
import com.example.project_pulse_backend.repository.ProjectRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;
    private final TimeHelper timeHelper;

    @PreAuthorize("hasAuthority('CREATE_PROJECT')")
    public ProjectResponse createProject(CreateProjectRequest request) {
        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build()
        );
        checkIsPm(pm);

        timeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        Project newProject = projectRepo.save(Project.builder()
                .projectName(request.getProjectName())
                .pm(pm)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .clientBudget(request.getClientBudget())
                .projectBudget(request.getProjectBudget())
                .description(request.getDescription())
                .projectStatus(request.getProjectStatus())
                .build());

        return toResponse(newProject);
    }

    @PreAuthorize("hasAuthority('UPDATE_PROJECT')")
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project entity = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        User pm = userRepo.findById(request.getPmId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build()
        );

        checkIsPm(pm);

        timeHelper.checkStartBeforeEnd(request.getStartAt(), request.getEndAt());

        entity.setProjectName(request.getProjectName());
        entity.setPm(pm);
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setClientBudget(request.getClientBudget());
        entity.setProjectBudget(request.getProjectBudget());
        entity.setDescription(request.getDescription());
        entity.setProjectStatus(request.getProjectStatus());

        return toResponse(projectRepo.save(entity));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT')")
    public void deleteProject(Long id) {
        projectRepo.deleteById(id);
    }

    @PreAuthorize("hasAuthority('GET_ALL_PROJECTS')")
    public List<ProjectResponse> getAllProjects() {
        return projectRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_ALL_PROJECTS_FOR_PM')")
    public List<ProjectResponse> getAllProjectsForPM() {
        User pm = getUserByToken.get();
        checkIsPm(pm);
        return projectRepo.findByPm_Id(pm.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_BY_ID')")
    public ProjectResponse getProjectByIdForAdmin(Long id) {
        Project project = projectRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );
        return toResponse(project);
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_BY_ID_FOR_PM')")
    public ProjectResponse getProjectByIdForPm(Long id) {
        User pm = getUserByToken.get();
        checkIsPm(pm);
        Project project = projectRepo.findByIdAndPm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );
        return toResponse(project);
    }

    private ProjectResponse toResponse(Project project) {
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

    private void checkIsPm(User user) {
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("PM"))) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
    }
}
