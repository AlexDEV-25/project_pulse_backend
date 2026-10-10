package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectHelper {
    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo projectMemberRepo;

    public Project getProjectById(Long projectId, User user) {
        checkAccessToProject(projectId, user);
        return findProjectById(projectId);
    }

    public boolean hasAccessToProject(Long projectId, User user) {
        if (RoleHelper.checkIsAdmin(user)) {
            return true;
        }
        if (RoleHelper.checkIsPm(user)) {
            return user.getId() != null && projectRepo.existsByIdAndPm_Id(projectId, user.getId());
        }
        if (RoleHelper.checkIsEmployee(user)) {
            return user.getId() != null && projectMemberRepo.existsByProject_IdAndUser_Id(projectId, user.getId());
        }
        return false;
    }

    public void checkAccessToProject(Long projectId, User user) {
        if (!hasAccessToProject(projectId, user)) {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }
    }

    public List<Project> getProjectForAdminAndPm(User user) {
        if (RoleHelper.checkIsAdmin(user)) {
            return projectRepo.findAll();
        } else if (RoleHelper.checkIsPm(user)) {
            return projectRepo.findByPm_Id(user.getId());
        } else {
            throw AppException.builder().appError(AppError.ACCESS_DENIED).build();
        }
    }

    public void checkProjectNotDone(Project project) {
        if (project.getProjectStatus() == ProjectStatus.DONE) {
            throw AppException.builder().appError(AppError.PROJECT_ALREADY_DONE).build();
        }
    }

    private Project findProjectById(Long projectId) {
        return projectRepo.findById(projectId).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build());
    }
}
