package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.dto.response.PhaseWorkingDays;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.AllocationRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AllocationHelperTest {
    private final AllocationRepo allocationRepo = mock(AllocationRepo.class);
    private final ProjectHelper projectHelper = mock(ProjectHelper.class);
    private final PhaseHelper phaseHelper = mock(PhaseHelper.class);
    private final AllocationHelper helper = new AllocationHelper(allocationRepo, projectHelper, phaseHelper);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsProjectAndEmployeeAllocations() {
        User employee = User.builder().id(7L).build();
        Project project = Project.builder().id(10L).projectName("Pulse").build();
        Phase phase = Phase.builder().id(3L).phaseName("Sprint").project(project).build();
        Allocation allocation = Allocation.builder().id(8L).phase(phase).allocationPercentage(50).build();
        when(projectHelper.getProjectById(10L, employee)).thenReturn(project);
        when(allocationRepo.filterAllocation(10L, 3L, "api", 7L)).thenReturn(List.of(allocation));

        TestAuthentication.selectRole("EMPLOYEE");
        assertEquals(List.of(allocation), helper.getAllocationByProject(10L, 3L, employee, "api"));
        verify(allocationRepo).filterAllocation(10L, 3L, "api", 7L);

        PhaseWorkingDays phaseDays = new PhaseWorkingDays(phase, Set.of(LocalDate.of(2026, 9, 7)));
        when(allocationRepo.findAllByMember_User_IdAndPhase_IdIn(7L, Set.of(3L)))
                .thenReturn(List.of(allocation));
        var scheduled = helper.getEmployeeAllocations(employee, List.of(phaseDays));
        assertEquals(1, scheduled.size());
        assertEquals("Pulse", scheduled.getFirst().projectName());
        assertEquals(Set.of(LocalDate.of(2026, 9, 7)), scheduled.getFirst().workingDates());
        assertTrue(helper.getEmployeeAllocations(employee, List.of()).isEmpty());
    }

    @Test
    void getsAllocationCalculatesPointsAndChecksLifecycle() {
        User admin = User.builder().id(1L).build();
        TestAuthentication.selectRole("ADMIN");
        Project project = Project.builder().id(4L).build();
        Phase phase = Phase.builder().project(project)
                .startAt(LocalDate.of(2026, 9, 7)).endAt(LocalDate.of(2026, 9, 8)).build();
        Allocation allocation = Allocation.builder().phase(phase)
                .resourceRateSnapshot(new BigDecimal("3.00")).allocationPercentage(50).build();
        when(allocationRepo.findById(5L)).thenReturn(Optional.of(allocation));
        assertSame(allocation, helper.getAllocationById(5L, admin));
        assertEquals(new BigDecimal("3.00"), helper.calculateAllocationPoint(allocation));
        assertEquals(BigDecimal.ZERO, helper.calculateAllocationPoint(null));
        assertEquals(BigDecimal.ZERO, helper.calculateAllocationPoint(
                Allocation.builder().resourceRateSnapshot(null).allocationPercentage(10).build()));

        Phase sameDay = Phase.builder().startAt(LocalDate.of(2026, 9, 7))
                .endAt(LocalDate.of(2026, 9, 7)).build();
        Allocation oneDay = Allocation.builder().phase(sameDay).allocationPercentage(70)
                .resourceRateSnapshot(BigDecimal.ONE).build();
        Allocation anotherOneDay = Allocation.builder().phase(sameDay).allocationPercentage(70)
                .resourceRateSnapshot(BigDecimal.ONE).build();
        when(allocationRepo.findAllByMember_User_Id(7L)).thenReturn(List.of(oneDay, anotherOneDay));
        helper.recalculateForUser(7L);
        assertEquals(1, oneDay.getOverloadedDates().size());
        assertEquals(140, oneDay.getOverloadedDates().getFirst().getTotalAllocationPercentage());

        helper.checkDone(phase);
        verify(projectHelper).checkProjectNotDone(project);
        verify(phaseHelper).checkPhaseNotDone(phase);
    }

    @Test
    void reportsMissingAllocation() {
        when(allocationRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AppException.class,
                () -> helper.getAllocationById(99L, User.builder().id(1L).build()));
    }
}
