package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.AllocationResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.helper.*;
import com.example.project_pulse_backend.mapper.AllocationMapper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AllocationServiceTest {
    @Test
    void listAllocationsForProjectAndByIdCalculateResponsePoints() {
        AllocationRepo repository = mock(AllocationRepo.class);
        GetUserByToken token = mock(GetUserByToken.class);
        AllocationHelper allocationHelper = mock(AllocationHelper.class);
        AllocationMapper mapper = mock(AllocationMapper.class);
        User user = User.builder().id(1L).build();
        Allocation allocation = Allocation.builder().id(3L).build();
        AllocationResponse mapped = AllocationResponse.builder().id(3L).build();
        when(token.get()).thenReturn(user);
        when(allocationHelper.getAllocationByProject(8L, 4L, user, " alpha ")).thenReturn(List.of(allocation));
        when(allocationHelper.getAllocationById(3L, user)).thenReturn(allocation);
        when(mapper.toAllocationResponse(allocation)).thenReturn(mapped);
        when(allocationHelper.calculateAllocationPoint(allocation)).thenReturn(new BigDecimal("2.50"));

        AllocationService service = new AllocationService(repository, token, mock(PhaseHelper.class),
                mock(ProjectMemberHelper.class), allocationHelper, mapper);

        assertEquals(List.of(mapped), service.getAllocationsByProjectId(8L, 4L, " alpha "));
        assertEquals(new BigDecimal("2.50"), mapped.getAllocationPoint());
        assertEquals(mapped, service.getAllocationById(3L));
        verify(allocationHelper).getAllocationByProject(8L, 4L, user, " alpha ");
        verify(allocationHelper, times(2)).calculateAllocationPoint(allocation);
    }
}
