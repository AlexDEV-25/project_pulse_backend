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
import com.example.project_pulse_backend.mapper.ProjectMemberMapper;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectHelper projectHelper;
    private final UserRepo userRepo;
    private final GetUserByToken getUserByToken;
    private final ProjectMemberHelper projectMemberHelper;
    private final ProjectMemberMapper projectMemberMapper;

    @PreAuthorize("hasAuthority('GET_PROJECT_MEMBERS_BY_PROJECT')")
    public List<ProjectMemberResponse> getMyProjectMembers(Long projectId, String keyword) {
        User user = getUserByToken.get();
        List<ProjectMember> projectMembers = projectMemberRepo.filterProjectMember(projectId, keyword);
        projectHelper.checkAccessToProject(projectId, user);
        return projectMembers.stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('CREATE_PROJECT_MEMBER')")
    @Transactional
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

        ProjectMember projectMember = projectMemberMapper.toProjectMember(request);
        projectMember.setProject(project);
        projectMember.setUser(member);
        ProjectMember newProjectMember = projectMemberRepo.save(projectMember);

        return toResponse(newProjectMember);
    }

    @PreAuthorize("hasAuthority('DELETE_PROJECT_MEMBER')")
    @Transactional
    public void deleteProjectMember(Long id) {
        User user = getUserByToken.get();
        ProjectMember projectMember = projectMemberHelper.getProjectMemberById(id, user);
        projectHelper.checkProjectNotDone(projectMember.getProject());
        projectMemberHelper.checkBeforeDeleteProjectMember(projectMember.getId());
        projectMemberRepo.delete(projectMember);
    }

    private ProjectMemberResponse toResponse(ProjectMember projectMember) {
        return projectMemberMapper.toProjectMemberResponse(projectMember);
    }
}
