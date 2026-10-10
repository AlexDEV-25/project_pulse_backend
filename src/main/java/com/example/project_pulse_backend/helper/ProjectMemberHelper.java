package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberHelper {
    private final ProjectMemberRepo projectMemberRepo;
    private final ProjectHelper projectHelper;


    public ProjectMember getProjectMemberById(Long id, User user) {
        ProjectMember projectMember = projectMemberRepo.findById(id).orElseThrow(
                () -> new AppException(AppError.PROJECT_MEMBER_NOT_FOUND));
        projectHelper.checkAccessToProject(projectMember.getProject().getId(), user);
        return projectMember;
    }

    public ProjectMember findByProjectAndUser(Project project, Long employeeId, User user) {
        ProjectMember projectMember = projectMemberRepo.findByProject_IdAndUser_Id(
                        project.getId(), employeeId)
                .orElseThrow(() -> AppException.builder().appError(AppError.MEMBER_NOT_FOUND).build());
        projectHelper.checkAccessToProject(projectMember.getProject().getId(), user);
        return projectMember;
    }


    public void lockAllProjectMembersByUserId(Long userId) {
        projectMemberRepo.findAllByUserIdWithLock(userId);
    }
}
