package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateProjectMemberRequest;
import com.example.project_pulse_backend.dto.response.ProjectMemberResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.ProjectMemberHelper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectHelper projectHelper;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;
    private final ProjectMemberHelper projectMemberHelper;

    @PreAuthorize("hasAuthority('GET_PROJECT_MEMBERS_BY_PROJECT')")
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMyProjectMembers(Long projectId, String keyword) {
        User user = getUserByToken.get();
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId, keyword);
        projectHelper.checkAccessToProject(projectId, user);
        return projectMembers.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('CREATE_PROJECT_MEMBER')")
    public ProjectMemberResponse createProjectMember(CreateProjectMemberRequest request) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(request.getProjectId(), user);

        projectHelper.checkProjectNotDone(project);

        User member = userRepo.findById(request.getUserId()).orElseThrow(
                () -> AppException.builder().appError(AppError.USER_NOT_FOUND).build());

        if (member.getRoles().stream()
                .noneMatch(role -> role.getName().equals("EMPLOYEE"))) {
            throw AppException.builder().appError(AppError.ONLY_EMPLOYEE).build();
        }

        if (projectMemberRepo.existsByProject_IdAndUser_Id(project.getId(), member.getId())) {
            throw AppException.builder().appError(AppError.PROJECT_MEMBER_ALREADY_EXISTS).build();
        }

        ProjectMember newProjectMember = projectMemberRepo.save(ProjectMember.builder()
                .project(project)
                .user(member)
                .build());

        return toResponse(newProjectMember);
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT_MEMBER')")
    public void deleteProjectMember(Long id) {
        User user = getUserByToken.get();
        ProjectMember projectMember = projectMemberHelper.getProjectMemberById(id, user);
        projectHelper.checkProjectNotDone(projectMember.getProject());

        // xử lý sau vì con nhiều case
        projectMemberRepo.delete(projectMember);
    }

    private ProjectMemberResponse toResponse(ProjectMember projectMember) {
        return ProjectMemberResponse.builder()
                .id(projectMember.getId())
                .projectId(projectMember.getProject() != null ? projectMember.getProject().getId() : null)
                .projectName(projectMember.getProject() != null ? projectMember.getProject().getProjectName() : null)
                .userId(projectMember.getUser() != null ? projectMember.getUser().getId() : null)
                .userName(projectMember.getUser() != null ? projectMember.getUser().getUserName() : null)
                .userResourceRate(projectMember.getUser() != null ? projectMember.getUser().getResourceRate() : null)
                .userPosition(projectMember.getUser() != null ? projectMember.getUser().getPosition() : null)
                .build();
    }
}
