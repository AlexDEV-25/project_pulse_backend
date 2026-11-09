package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EvmHelperTest {
    private final PhaseEvmSnapshotRepo snapshotRepo = mock(PhaseEvmSnapshotRepo.class);
    private final AllocationRepo allocationRepo = mock(AllocationRepo.class);
    private final EarningTransactionRepo earningRepo = mock(EarningTransactionRepo.class);
    private final TaskCostTransactionRepo costRepo = mock(TaskCostTransactionRepo.class);
    private final PhaseRepo phaseRepo = mock(PhaseRepo.class);
    private final ProjectMemberRepo memberRepo = mock(ProjectMemberRepo.class);
    private final TaskRepo taskRepo = mock(TaskRepo.class);
    private final AllocationHelper allocationHelper = mock(AllocationHelper.class);
    private final PhaseHelper phaseHelper = mock(PhaseHelper.class);
    private final EvmHelper helper = new EvmHelper(snapshotRepo, allocationRepo, earningRepo, costRepo,
            phaseRepo, memberRepo, taskRepo, allocationHelper, phaseHelper);

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder().id(10L).projectName("Pulse")
                .projectBudget(new BigDecimal("1000")).clientBudget(new BigDecimal("1500")).build();
        when(allocationRepo.filterAllocation(eq(10L), isNull(), isNull(), isNull())).thenReturn(List.of());
        when(earningRepo.sumNetEarnedPointsByProjectId(10L, EarningTransactionType.EARN))
                .thenReturn(new BigDecimal("600"));
        when(costRepo.sumCostPointsByProjectId(10L)).thenReturn(new BigDecimal("500"));
    }

    @Test
    void calculatesIndexesAndProjectLevelResponses() {
        assertEquals(new BigDecimal("1.2000"), helper.cpi(new BigDecimal("600"), new BigDecimal("500")));
        assertNull(helper.cpi(BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.7500"), helper.spi(new BigDecimal("600"), new BigDecimal("800")));
        assertNull(helper.spi(BigDecimal.TEN, BigDecimal.ZERO));

        EvmHealthResponse health = helper.getProjectHealth(project);
        assertEquals(new BigDecimal("1000"), health.getBac());
        assertEquals(new BigDecimal("600"), health.getEv());
        assertEquals(new BigDecimal("500"), health.getAc());

        ProjectEvmFinancialResponse admin = helper.getProjectFinancialSummary(project, true);
        assertEquals(new BigDecimal("1500"), admin.getClientBudget());
        assertEquals(new BigDecimal("1000"), admin.getCurrentMargin());
        assertNotNull(admin.getForecastedMargin());
        ProjectEvmFinancialResponse employeeView = helper.getProjectFinancialSummary(project, false);
        assertNull(employeeView.getClientBudget());
        assertNull(employeeView.getCurrentMargin());
    }

    @Test
    void snapshotsAndCalculatesProjectPlannedValue() {
        Phase phase = Phase.builder().id(2L).startAt(LocalDate.of(2026, 9, 7))
                .endAt(LocalDate.of(2026, 9, 11)).build();
        Allocation allocation = Allocation.builder().id(4L).phase(phase).build();
        when(allocationRepo.findByPhase_Id(2L)).thenReturn(List.of(allocation));
        when(allocationRepo.filterAllocation(10L, null, null, null)).thenReturn(List.of(allocation));
        when(allocationHelper.calculateAllocationPoint(allocation)).thenReturn(new BigDecimal("10.00"));
        when(costRepo.sumCostPointsByPhaseId(2L)).thenReturn(new BigDecimal("3.00"));
        when(earningRepo.sumNetEarnedPointsByPhaseId(2L, EarningTransactionType.EARN))
                .thenReturn(new BigDecimal("4.00"));

        assertEquals(new BigDecimal("10.00"), helper.pv(10L, LocalDate.of(2026, 9, 11)));
        helper.createSnapshot(phase);
        verify(snapshotRepo).save(argThat(snapshot ->
                snapshot.getPhase() == phase && snapshot.getBac().equals(new BigDecimal("10.00"))
                        && snapshot.getEv().equals(new BigDecimal("4.00"))
                        && snapshot.getAc().equals(new BigDecimal("3.00"))));

        when(snapshotRepo.findByPhase_Id(2L)).thenReturn(Optional.of(PhaseEvmSnapshot.builder()
                .phase(phase).bac(new BigDecimal("10")).pv(new BigDecimal("8"))
                .ev(new BigDecimal("4")).ac(new BigDecimal("3")).build()));
        phase.setStatus(PhaseStatus.DONE);
        when(phaseRepo.findByProject_IdOrderByStartAtAscIdAsc(10L)).thenReturn(List.of(phase));
        when(phaseHelper.calculateProgress(2L)).thenReturn(new BigDecimal("40.00"));
        List<PhaseEvmDetailResponse> rows = helper.getProjectPhasesEvm(project);
        assertEquals(1, rows.size());
        assertEquals(new BigDecimal("8"), rows.getFirst().getPv());

        assertThrows(AppException.class, () -> helper.createSnapshot(phase));
        when(snapshotRepo.findByPhase_Id(2L)).thenReturn(Optional.empty());
        Phase done = Phase.builder().id(2L).status(PhaseStatus.DONE).build();
        when(phaseRepo.findByProject_IdOrderByStartAtAscIdAsc(10L)).thenReturn(List.of(done));
        assertThrows(AppException.class, () -> helper.getProjectPhasesEvm(project));
    }

    @Test
    void summarizesEmployeesAndExportsCsv() {
        User user = User.builder().id(20L).userName("Dev").position("Engineer")
                .resourceRate(new BigDecimal("20")).build();
        User secondUser = User.builder().id(19L).userName("Analyst").position("Analyst")
                .resourceRate(new BigDecimal("10")).build();
        when(memberRepo.findByProject_Id(10L)).thenReturn(List.of(
                ProjectMember.builder().user(user).build(),
                ProjectMember.builder().user(secondUser).build(),
                ProjectMember.builder().user(null).build()));
        when(earningRepo.sumEarnedPointsGroupedByEmployee(10L, EarningTransactionType.EARN))
                .thenReturn(List.<Object[]>of(new Object[]{20L, new BigDecimal("60")},
                        new Object[]{19L, new BigDecimal("30")}));
        when(costRepo.sumCostAndWorkdaysGroupedByEmployee(10L))
                .thenReturn(List.<Object[]>of(new Object[]{20L, new BigDecimal("40"), new BigDecimal("2")},
                        new Object[]{19L, new BigDecimal("20"), new BigDecimal("1")}));
        when(taskRepo.countCompletedTasksGroupedByEmployee(10L, TaskStatus.DONE))
                .thenReturn(List.<Object[]>of(new Object[]{20L, 3L}, new Object[]{19L, 1L}));

        List<EmployeeEvmPerformanceResponse> employees = helper.getProjectEmployeesEvm(project);
        assertEquals(2, employees.size());
        assertEquals(20L, employees.getFirst().getUserId());
        assertEquals("STAR_PERFORMER", employees.getFirst().getPerformanceCategory());

        ProjectEvmFinancialResponse summary = helper.getProjectFinancialSummary(project, false);
        byte[] report = helper.generateEvmCsvReport(summary,
                List.of(PhaseEvmDetailResponse.builder().phaseId(1L).phaseName("Sprint").build()),
                employees);
        String csv = new String(report, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("Pulse"));
        assertTrue(csv.contains("Sprint"));
        assertTrue(csv.contains("Dev"));
    }
}
