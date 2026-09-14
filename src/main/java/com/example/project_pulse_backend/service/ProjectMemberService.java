package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.ProjectMemberStatus;
import com.example.project_pulse_backend.dto.request.CreateProjectMemberRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectMemberRequest;
import com.example.project_pulse_backend.dto.request.UpdateProjectMemberStatusRequest;
import com.example.project_pulse_backend.dto.response.ProjectMemberResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;

    @PreAuthorize("hasAuthority('CREATE_PROJECT_MEMBER')")
    public ProjectMemberResponse createProjectMember(CreateProjectMemberRequest request) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        Project project = projectRepo.findByIdAndPm_Id(request.getProjectId(), pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        User user = userRepo.findById(request.getUserId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build()
        );

        if (projectMemberRepo.existsByProject_IdAndUser_Id(project.getId(), user.getId())) {
            throw AppException.builder().appError(AppError.PROJECT_MEMBER_ALREADY_EXISTS).build();
        }

        ProjectMember newProjectMember = projectMemberRepo.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .status(ProjectMemberStatus.PENDING)
                .build());

        return toResponse(newProjectMember);
    }

    @PreAuthorize("hasAuthority('UPDATE_PROJECT_MEMBER')")
    public ProjectMemberResponse updateProjectMember(Long id, UpdateProjectMemberRequest request) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        ProjectMember entity = projectMemberRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_MEMBER_NOT_FOUND).build()
        );

        Project project = projectRepo.findByIdAndPm_Id(request.getProjectId(), pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );

        User user = userRepo.findById(request.getUserId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build()
        );

        if (projectMemberRepo.existsByProject_IdAndUser_IdAndIdNot(project.getId(), user.getId(), id)) {
            throw AppException.builder().appError(AppError.PROJECT_MEMBER_ALREADY_EXISTS).build();
        }

        entity.setProject(project);
        entity.setUser(user);
        entity.setStatus(ProjectMemberStatus.PENDING);

        return toResponse(projectMemberRepo.save(entity));
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT_MEMBER')")
    public void deleteProjectMember(Long id) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        ProjectMember projectMember = projectMemberRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_MEMBER_NOT_FOUND).build()
        );
        projectMemberRepo.delete(projectMember);
    }

    @PreAuthorize("hasAuthority('UPDATE_PROJECT_MEMBER_STATUS')")
    public ProjectMemberResponse updateProjectMemberStatus(Long id, UpdateProjectMemberStatusRequest request) {

        ProjectMember projectMember = projectMemberRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_MEMBER_NOT_FOUND).build()
        );
        projectMember.setStatus(request.getStatus());
        return toResponse(projectMemberRepo.save(projectMember));
    }

    private ProjectMemberResponse toResponse(ProjectMember projectMember) {
        return ProjectMemberResponse.builder()
                .id(projectMember.getId())
                .projectId(projectMember.getProject() != null ? projectMember.getProject().getId() : null)
                .projectName(projectMember.getProject() != null ? projectMember.getProject().getProjectName() : null)
                .userId(projectMember.getUser() != null ? projectMember.getUser().getId() : null)
                .userName(projectMember.getUser() != null ? projectMember.getUser().getUserName() : null)
                .status(projectMember.getStatus())
                .build();
    }

    private void checkIsPm(User user) {
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("PM"))) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
    }
}
