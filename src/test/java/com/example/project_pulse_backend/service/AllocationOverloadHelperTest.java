package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.request.CreateAllocationRequest;
import com.example.project_pulse_backend.entity.Allocation;
import com.example.project_pulse_backend.entity.AllocationOverloadedDate;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.AllocationHelper;
import com.example.project_pulse_backend.mapper.AllocationMapper;
import org.mapstruct.factory.Mappers;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.PhaseHelper;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.ProjectMemberHelper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationOverloadHelperTest {
    private static final Long USER_ID = 11L;

    @Mock
    private AllocationRepo allocationRepo;

    @Test
    void marksEveryAllocationOnDaysWhereCombinedLoadExceedsOneHundred() {
        Allocation first = allocation(40, date(5), date(8));
        Allocation second = allocation(35, date(7), date(8));
        Allocation third = allocation(30, date(7), date(9));
        Allocation exactlyOneHundred = allocation(60, date(12), date(12));
        Allocation nonOverlapping = allocation(80, date(13), date(14));
        when(allocationRepo.findAllByMember_User_Id(USER_ID))
                .thenReturn(List.of(first, second, third, exactlyOneHundred, nonOverlapping));

        allocationHelper().recalculateForUser(USER_ID);

        assertEquals(List.of(overloadedDate(7, 105), overloadedDate(8, 105)), first.getOverloadedDates());
        assertEquals(List.of(overloadedDate(7, 105), overloadedDate(8, 105)), second.getOverloadedDates());
        assertEquals(List.of(overloadedDate(7, 105), overloadedDate(8, 105)), third.getOverloadedDates());
        assertEquals(List.of(), exactlyOneHundred.getOverloadedDates());
        assertEquals(List.of(), nonOverlapping.getOverloadedDates());
    }

    @Test
    void ignoresDaysOffAndSupportsMakeUpDays() {
        Allocation weekdayAllocation = allocation(80, date(7), date(7));
        Allocation overlappingAllocation = allocation(50, date(7), date(7));
        weekdayAllocation.getPhase().setOffDates(List.of(date(7)));
        when(allocationRepo.findAllByMember_User_Id(USER_ID))
                .thenReturn(List.of(weekdayAllocation, overlappingAllocation));

        AllocationHelper service = allocationHelper();
        service.recalculateForUser(USER_ID);

        assertEquals(List.of(), weekdayAllocation.getOverloadedDates());
        assertEquals(List.of(), overlappingAllocation.getOverloadedDates());

        overlappingAllocation.getPhase().setOffDates(List.of(date(7)));
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), weekdayAllocation.getOverloadedDates());
        assertEquals(List.of(), overlappingAllocation.getOverloadedDates());

        Allocation weekendMakeUpA = allocation(70, date(10), date(10));
        Allocation weekendMakeUpB = allocation(40, date(10), date(10));
        weekendMakeUpA.getPhase().setMakeUpDates(List.of(date(10)));
        weekendMakeUpB.getPhase().setMakeUpDates(List.of(date(10)));
        when(allocationRepo.findAllByMember_User_Id(USER_ID))
                .thenReturn(List.of(weekendMakeUpA, weekendMakeUpB));

        service.recalculateForUser(USER_ID);

        assertEquals(List.of(overloadedDate(10, 110)), weekendMakeUpA.getOverloadedDates());
        assertEquals(List.of(overloadedDate(10, 110)), weekendMakeUpB.getOverloadedDates());
    }

    @Test
    void clearsStaleWarningsAfterPercentageDeletionOrScheduleChanges() {
        Allocation first = allocation(70, date(5), date(7));
        Allocation second = allocation(30, date(7), date(8));
        List<Allocation> currentAllocations = new ArrayList<>(List.of(first, second));
        when(allocationRepo.findAllByMember_User_Id(USER_ID)).thenReturn(currentAllocations);
        AllocationHelper service = allocationHelper();

        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), first.getOverloadedDates());
        assertEquals(List.of(), second.getOverloadedDates());

        second.setAllocationPercentage(40);
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(overloadedDate(7, 110)), first.getOverloadedDates());
        assertEquals(List.of(overloadedDate(7, 110)), second.getOverloadedDates());

        second.setAllocationPercentage(30);
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), first.getOverloadedDates());
        assertEquals(List.of(), second.getOverloadedDates());

        second.setAllocationPercentage(40);
        service.recalculateForUser(USER_ID);
        first.getPhase().setEndAt(date(6));
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), first.getOverloadedDates());
        assertEquals(List.of(), second.getOverloadedDates());

        first.getPhase().setEndAt(date(7));
        service.recalculateForUser(USER_ID);
        first.getPhase().setOffDates(List.of(date(7)));
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), first.getOverloadedDates());
        assertEquals(List.of(), second.getOverloadedDates());

        first.getPhase().setOffDates(List.of());
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(overloadedDate(7, 110)), first.getOverloadedDates());
        currentAllocations.remove(second);
        service.recalculateForUser(USER_ID);
        assertEquals(List.of(), first.getOverloadedDates());
    }

    @Test
    void marksMakeUpDateOverloadWithoutDuplicatingDates() {
        Allocation first = allocation(60, date(10), date(10));
        Allocation second = allocation(50, date(10), date(10));
        first.getPhase().setMakeUpDates(List.of(date(10), date(10)));
        second.getPhase().setMakeUpDates(List.of(date(10)));
        when(allocationRepo.findAllByMember_User_Id(USER_ID)).thenReturn(List.of(first, second));

        allocationHelper().recalculateForUser(USER_ID);

        assertEquals(List.of(overloadedDate(10, 110)), first.getOverloadedDates());
        assertEquals(List.of(overloadedDate(10, 110)), second.getOverloadedDates());
    }

    @Test
    void recalculatesOnlyAllocationsForRequestedUser() {
        Allocation allocation = allocation(50, date(5), date(5));
        when(allocationRepo.findAllByMember_User_Id(USER_ID)).thenReturn(List.of(allocation));

        allocationHelper().recalculateForUser(USER_ID);

        verify(allocationRepo).findAllByMember_User_Id(USER_ID);
        assertEquals(List.of(), allocation.getOverloadedDates());
    }

    @Test
    void creatingAllocationAllowsOverloadAndRecalculatesTheWholeUser() {
        AllocationRepo repository = mock(AllocationRepo.class);
        var tokenHelper = mock(GetUserByToken.class);
        var phaseHelper = mock(PhaseHelper.class);
        var memberHelper = mock(ProjectMemberHelper.class);
        var allocationHelper = mock(AllocationHelper.class);

        User requester = new User();
        requester.setId(2L);
        User employee = new User();
        employee.setId(USER_ID);
        Project project = new Project();
        project.setId(20L);
        project.setProjectName("Project");
        Phase phase = allocation(100, date(5), date(5)).getPhase();
        phase.setId(30L);
        phase.setProject(project);
        ProjectMember member = ProjectMember.builder().id(40L).user(employee).build();

        when(tokenHelper.get()).thenReturn(requester);
        when(phaseHelper.getPhaseById(phase.getId(), requester)).thenReturn(phase);
        when(memberHelper.getProjectMemberById(member.getId(), requester)).thenReturn(member);
        when(repository.existsByPhase_IdAndMember_Id(phase.getId(), member.getId())).thenReturn(false);
        when(repository.save(any(Allocation.class))).thenAnswer(invocation -> {
            Allocation saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });
        when(allocationHelper.calculateAllocationPoint(any())).thenReturn(java.math.BigDecimal.ZERO);

        AllocationService service = new AllocationService(
                repository,
                tokenHelper,
                phaseHelper,
                memberHelper,
                allocationHelper,
                Mappers.getMapper(AllocationMapper.class));
        CreateAllocationRequest request = new CreateAllocationRequest();
        request.setPhaseId(phase.getId());
        request.setMemberId(member.getId());
        request.setAllocationPercentage(100);

        var response = service.createAllocation(request);

        assertEquals(100, response.getAllocationPercentage());
        assertNotNull(response.getOverloadedDates());
        verify(allocationHelper).recalculateForUser(USER_ID);
    }

    @Test
    void updatingAndDeletingAllocationRecalculateTheUser() {
        AllocationRepo repository = mock(AllocationRepo.class);
        var tokenHelper = mock(GetUserByToken.class);
        var phaseHelper = mock(PhaseHelper.class);
        var memberHelper = mock(ProjectMemberHelper.class);
        var allocationHelper = mock(AllocationHelper.class);

        User requester = new User();
        requester.setId(2L);
        User employee = new User();
        employee.setId(USER_ID);
        Project project = new Project();
        project.setId(20L);
        project.setProjectName("Project");
        Phase phase = allocation(70, date(5), date(7)).getPhase();
        phase.setProject(project);
        ProjectMember member = ProjectMember.builder().id(40L).user(employee).build();
        Allocation allocation = Allocation.builder()
                .id(50L)
                .phase(phase)
                .member(member)
                .allocationPercentage(70)
                .build();

        when(tokenHelper.get()).thenReturn(requester);
        when(allocationHelper.getAllocationById(50L, requester)).thenReturn(allocation);
        when(repository.save(allocation)).thenReturn(allocation);
        when(allocationHelper.calculateAllocationPoint(allocation)).thenReturn(java.math.BigDecimal.ZERO);

        AllocationService service = new AllocationService(
                repository,
                tokenHelper,
                phaseHelper,
                memberHelper,
                allocationHelper,
                Mappers.getMapper(AllocationMapper.class));
        var updateRequest = new com.example.project_pulse_backend.dto.request.UpdateAllocationRequest();
        updateRequest.setAllocationPercentage(50);

        service.updateAllocation(50L, updateRequest);
        service.deleteAllocation(50L);

        verify(allocationHelper, times(2)).recalculateForUser(USER_ID);
    }

    @Test
    void newAllocationsStartWithAnEmptyWarningList() {
        assertNotNull(Allocation.builder().build().getOverloadedDates());
        assertEquals(List.of(), Allocation.builder().build().getOverloadedDates());
    }

    private static Allocation allocation(int percentage, LocalDate start, LocalDate end) {
        Phase phase = Phase.builder()
                .startAt(start)
                .endAt(end)
                .makeUpDates(List.of())
                .offDates(List.of())
                .build();
        return Allocation.builder()
                .phase(phase)
                .allocationPercentage(percentage)
                .overloadedDates(new ArrayList<>())
                .build();
    }

    private static LocalDate date(int day) {
        return LocalDate.of(2026, 10, day);
    }

    private static AllocationOverloadedDate overloadedDate(int day, int total) {
        return new AllocationOverloadedDate(date(day), total);
    }

    private AllocationHelper allocationHelper() {
        return new AllocationHelper(
                allocationRepo,
                mock(ProjectHelper.class),
                mock(PhaseHelper.class));
    }
}
