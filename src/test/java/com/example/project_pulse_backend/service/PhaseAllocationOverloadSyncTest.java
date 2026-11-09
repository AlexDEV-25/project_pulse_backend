package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.dto.request.UpdatePhaseRequest;
import com.example.project_pulse_backend.dto.response.PhaseResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.PhaseMapper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class PhaseAllocationOverloadSyncTest {
    @Test
    void updatingPhaseRecalculatesDistinctAffectedUsersInSortedOrder() {
        PhaseRepo phaseRepo = mock(PhaseRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        PhaseHelper phaseHelper = mock(PhaseHelper.class);
        AllocationHelper allocationHelper = mock(AllocationHelper.class);
        ProjectHelper projectHelper = mock(ProjectHelper.class);
        AllocationRepo allocationRepo = mock(AllocationRepo.class);
        PhaseMapper mapper = mock(PhaseMapper.class);
        User requester = User.builder().id(1L).build();
        Project project = Project.builder().id(2L).build();
        LocalDate start = LocalDate.of(2026, 11, 9);
        LocalDate end = LocalDate.of(2026, 11, 13);
        Phase phase = Phase.builder().id(3L).project(project).startAt(start).endAt(end)
                .makeUpDates(List.of()).offDates(List.of()).status(PhaseStatus.PLANNED).build();
        Allocation user22 = allocation(22L, phase);
        Allocation user11 = allocation(11L, phase);
        Allocation duplicateUser11 = allocation(11L, phase);
        when(token.get()).thenReturn(requester);
        when(phaseHelper.getPhaseById(3L, requester)).thenReturn(phase);
        when(phaseHelper.normalizeDates(null)).thenReturn(List.of());
        when(allocationRepo.findByPhase_Id(3L)).thenReturn(List.of(user22, user11, duplicateUser11));
        when(phaseRepo.save(phase)).thenReturn(phase);
        when(mapper.toPhaseResponse(phase)).thenReturn(PhaseResponse.builder().build());
        when(phaseHelper.calculateProgress(3L)).thenReturn(java.math.BigDecimal.ZERO);
        PhaseService service = new PhaseService(phaseRepo, token, phaseHelper, allocationHelper,
                projectHelper, mock(EvmHelper.class), allocationRepo, mapper);

        service.updatePhase(3L, UpdatePhaseRequest.builder().startAt(start).endAt(end)
                .phaseStatus(PhaseStatus.PLANNED).build());

        InOrder order = inOrder(allocationHelper);
        order.verify(allocationHelper).recalculateForUser(11L);
        order.verify(allocationHelper).recalculateForUser(22L);
        verify(allocationHelper, times(1)).recalculateForUser(11L);
    }

    private static Allocation allocation(Long userId, Phase phase) {
        User employee = User.builder().id(userId).build();
        return Allocation.builder().phase(phase)
                .member(ProjectMember.builder().user(employee).build())
                .allocationPercentage(50).build();
    }
}
