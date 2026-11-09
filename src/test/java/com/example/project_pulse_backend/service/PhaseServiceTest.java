package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.PhaseMapper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhaseServiceTest {
    @Test
    void createsAndReadsPhasesWithMappedWorkdayAndProgress() {
        PhaseRepo repository = mock(PhaseRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        PhaseMapper mapper = mock(PhaseMapper.class);
        User user = User.builder().id(1L).build();
        Project project = Project.builder().id(2L).build();
        LocalDate start = LocalDate.of(2026, 11, 9);
        LocalDate end = LocalDate.of(2026, 11, 13);
        Phase phase = Phase.builder().id(3L).project(project).startAt(start).endAt(end)
                .makeUpDates(List.of()).offDates(List.of()).build();
        PhaseResponse response = PhaseResponse.builder().id(3L).build();
        when(token.get()).thenReturn(user);
        when(projectHelper.getProjectById(2L, user)).thenReturn(project);
        when(phaseHelper.normalizeDates(any())).thenReturn(List.of());
        when(mapper.toPhase(any())).thenReturn(phase);
        when(repository.save(phase)).thenReturn(phase);
        when(mapper.toPhaseResponse(phase)).thenReturn(response);
        when(phaseHelper.calculateProgress(3L)).thenReturn(java.math.BigDecimal.ZERO);
        when(phaseHelper.getPhasesByProject(2L, user, "analysis")).thenReturn(List.of(phase));
        when(phaseHelper.getPhaseById(3L, user)).thenReturn(phase);
        PhaseService service = new PhaseService(repository, token, phaseHelper, mock(AllocationHelper.class),
                projectHelper, mock(EvmHelper.class), mock(AllocationRepo.class), mapper);

        assertEquals(response, service.createPhase(CreatePhaseRequest.builder().projectId(2L)
                .startAt(start).endAt(end).build()));
        assertEquals(PhaseStatus.PLANNED, phase.getStatus());
        assertEquals(List.of(response), service.getAllPhasesByProject(2L, " analysis "));
        assertEquals(response, service.getPhaseById(3L));
        verify(phaseHelper).checkSpecialDates(start, end, List.of(), List.of());
    }

    @Test
    void updatesAndDeletesPhaseAndRecalculatesAffectedAllocations() {
        PhaseRepo repository = mock(PhaseRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        AllocationHelper allocationHelper = mock(AllocationHelper.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        PhaseMapper mapper = mock(PhaseMapper.class);
        User user = User.builder().id(1L).build();
        Project project = Project.builder().id(2L).build();
        LocalDate start = LocalDate.of(2026, 11, 9);
        LocalDate end = LocalDate.of(2026, 11, 13);
        Phase phase = Phase.builder().id(3L).project(project).startAt(start).endAt(end)
                .makeUpDates(List.of()).offDates(List.of()).status(PhaseStatus.PLANNED).build();
        PhaseResponse response = PhaseResponse.builder().id(3L).build();
        when(token.get()).thenReturn(user);
        when(phaseHelper.getPhaseById(3L, user)).thenReturn(phase);
        when(phaseHelper.normalizeDates(any())).thenReturn(List.of());
        when(repository.save(phase)).thenReturn(phase);
        when(mapper.toPhaseResponse(phase)).thenReturn(response);
        when(phaseHelper.calculateProgress(3L)).thenReturn(java.math.BigDecimal.ZERO);
        when(allocationRepo.findByPhase_Id(3L)).thenReturn(List.of());
        PhaseService service = new PhaseService(repository, token, phaseHelper, allocationHelper,
                projectHelper, mock(EvmHelper.class), allocationRepo, mapper);

        assertEquals(response, service.updatePhase(3L, UpdatePhaseRequest.builder().startAt(start)
                .endAt(end).phaseStatus(PhaseStatus.PLANNED).build()));
        service.deletePhase(3L);
        verify(projectHelper, times(2)).checkProjectNotDone(project);
        verify(phaseHelper).checkBeforeDelete(3L);
        verify(repository).delete(phase);
    }
}
