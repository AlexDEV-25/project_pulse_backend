package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.PhaseRepo;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectHelperTest {
    private final ProjectRepo projectRepo = mock(ProjectRepo.class);
    private final ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
    private final PhaseRepo phaseRepo = mock(PhaseRepo.class);
    private final ProjectHelper helper = new ProjectHelper(projectRepo, memberRepo, phaseRepo);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getsAndChecksProjectAccessForAdminPmAndEmployee() {
        User user = User.builder().id(7L).build();
        Project project = Project.builder().id(4L).projectStatus(ProjectStatus.IN_PROGRESS).build();
        when(projectRepo.findById(4L)).thenReturn(Optional.of(project));

        TestAuthentication.selectRole("ADMIN");
        assertSame(project, helper.getProjectById(4L, user));
        helper.checkAccessToProject(4L, user);
        when(projectRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> helper.getProjectById(99L, user));

        TestAuthentication.selectRole("PM");
        when(projectRepo.existsByIdAndPm_Id(4L, 7L)).thenReturn(true);
        helper.checkAccessToProject(4L, user);
        when(projectRepo.findByPm_Id(7L)).thenReturn(List.of(project));
        assertEquals(List.of(project), helper.getProjectForAdminAndPm(user));

        TestAuthentication.selectRole("EMPLOYEE");
        when(memberRepo.existsByProject_IdAndUser_Id(4L, 7L)).thenReturn(true);
        helper.checkAccessToProject(4L, user);
        assertThrows(AppException.class, () -> helper.getProjectForAdminAndPm(user));
    }

    @Test
    void validatesProjectLifecycleAndDateRules() {
        Project active = Project.builder().id(4L).projectStatus(ProjectStatus.IN_PROGRESS)
                .startAt(LocalDate.of(2026, 9, 7)).endAt(LocalDate.of(2026, 9, 30)).build();
        helper.checkProjectNotDone(active);
        assertTrue(helper.isNotFinished(active));
        assertFalse(helper.isNotFinished(Project.builder().projectStatus(ProjectStatus.DONE).build()));
        assertFalse(helper.isNotFinished(Project.builder().projectStatus(ProjectStatus.CANCELLED).build()));
        assertThrows(AppException.class, () -> helper.checkProjectNotDone(
                Project.builder().projectStatus(ProjectStatus.DONE).build()));

        Phase phase = Phase.builder().startAt(LocalDate.of(2026, 9, 8))
                .endAt(LocalDate.of(2026, 9, 20)).build();
        when(phaseRepo.findByProject_Id(4L)).thenReturn(List.of(phase));
        helper.checkTime(active);
        when(phaseRepo.findByProject_Id(4L)).thenReturn(List.of(
                Phase.builder().startAt(LocalDate.of(2026, 9, 1))
                        .endAt(LocalDate.of(2026, 9, 10)).build()));
        assertThrows(AppException.class, () -> helper.checkTime(active));

        when(memberRepo.findByProject_Id(4L)).thenReturn(List.of());
        when(phaseRepo.findByProject_Id(4L)).thenReturn(List.of());
        helper.checkBeforeDelete(4L);
        when(phaseRepo.existsByProject_IdAndStatusNot(4L, PhaseStatus.DONE)).thenReturn(false);
        helper.checkInProgressPhases(active);
        when(phaseRepo.existsByProject_IdAndStatusNot(4L, PhaseStatus.DONE)).thenReturn(true);
        assertThrows(AppException.class, () -> helper.checkInProgressPhases(active));

        when(memberRepo.findByProject_Id(4L)).thenReturn(List.of(
                com.example.project_pulse_backend.entity.ProjectMember.builder().build()));
        assertThrows(AppException.class, () -> helper.checkBeforeDelete(4L));
    }
}
