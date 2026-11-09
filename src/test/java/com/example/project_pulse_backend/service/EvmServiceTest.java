package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.EmployeeEvmPerformanceResponse;
import com.example.project_pulse_backend.dto.response.EvmHealthResponse;
import com.example.project_pulse_backend.dto.response.PhaseEvmDetailResponse;
import com.example.project_pulse_backend.dto.response.ProjectEvmFinancialResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Role;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.EvmHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.TestAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvmServiceTest {

    @Mock
    private GetUserByToken getUserByToken;

    @Mock
    private EvmHelper evmHelper;

    @Mock
    private ProjectHelper projectHelper;

    @InjectMocks
    private EvmService evmService;

    private User admin;
    private Project project;
    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .userName("Admin User")
                .roles(List.of(Role.builder().name("ADMIN").build()))
                .build();

        project = Project.builder()
                .id(10L)
                .projectName("Project Alpha")
                .projectBudget(new BigDecimal("1000"))
                .clientBudget(new BigDecimal("1500"))
                .build();

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProjectHealth_delegatesToHelper() {
        when(getUserByToken.get()).thenReturn(admin);
        when(projectHelper.getProjectById(10L, admin)).thenReturn(project);
        EvmHealthResponse expected = EvmHealthResponse.builder().projectId(10L).build();
        when(evmHelper.getProjectHealth(project)).thenReturn(expected);

        EvmHealthResponse actual = evmService.getProjectHealth(10L);

        assertEquals(expected, actual);
        verify(evmHelper).getProjectHealth(project);
    }

    @Test
    void getProjectPhasesEvm_delegatesToHelper() {
        when(getUserByToken.get()).thenReturn(admin);
        when(projectHelper.getProjectById(10L, admin)).thenReturn(project);

        List<PhaseEvmDetailResponse> expected = List.of(
                PhaseEvmDetailResponse.builder().phaseId(101L).cumulativePv(new BigDecimal("500")).build()
        );
        when(evmHelper.getProjectPhasesEvm(project)).thenReturn(expected);

        List<PhaseEvmDetailResponse> actual = evmService.getProjectPhasesEvm(10L);

        assertEquals(expected, actual);
        verify(evmHelper).getProjectPhasesEvm(project);
    }

    @Test
    void getProjectEmployeesEvm_delegatesToHelper() {
        when(getUserByToken.get()).thenReturn(admin);
        when(projectHelper.getProjectById(10L, admin)).thenReturn(project);

        List<EmployeeEvmPerformanceResponse> expected = List.of(
                EmployeeEvmPerformanceResponse.builder().userId(20L).userName("Dev John").build()
        );
        when(evmHelper.getProjectEmployeesEvm(project)).thenReturn(expected);

        List<EmployeeEvmPerformanceResponse> actual = evmService.getProjectEmployeesEvm(10L);

        assertEquals(expected, actual);
        verify(evmHelper).getProjectEmployeesEvm(project);
    }

    @Test
    void getProjectFinancialSummary_passesIsAdminTrueForAdmin() {
        TestAuthentication.selectRole("ADMIN");
        when(getUserByToken.get()).thenReturn(admin);
        when(projectHelper.getProjectById(10L, admin)).thenReturn(project);

        ProjectEvmFinancialResponse expected = ProjectEvmFinancialResponse.builder()
                .projectId(10L)
                .projectBudget(new BigDecimal("1000"))
                .clientBudget(new BigDecimal("1500"))
                .build();
        when(evmHelper.getProjectFinancialSummary(project, true)).thenReturn(expected);

        ProjectEvmFinancialResponse actual = evmService.getProjectFinancialSummary(10L);

        assertEquals(expected, actual);
        verify(evmHelper).getProjectFinancialSummary(project, true);
    }

    @Test
    void getProjectFinancialSummary_passesIsAdminFalseForPm() {
        TestAuthentication.selectRole("PM");
        User pm = User.builder().id(2L).userName("PM User").roles(List.of(Role.builder().name("PM").build())).build();
        when(getUserByToken.get()).thenReturn(pm);
        when(projectHelper.getProjectById(10L, pm)).thenReturn(project);

        ProjectEvmFinancialResponse expected = ProjectEvmFinancialResponse.builder()
                .projectId(10L)
                .projectBudget(new BigDecimal("1000"))
                .clientBudget(null)
                .build();
        when(evmHelper.getProjectFinancialSummary(project, false)).thenReturn(expected);

        ProjectEvmFinancialResponse actual = evmService.getProjectFinancialSummary(10L);

        assertEquals(expected, actual);
        verify(evmHelper).getProjectFinancialSummary(project, false);
    }

    @Test
    void exportEvmCsvReport_generatesCsvFromHelper() {
        TestAuthentication.selectRole("ADMIN");
        when(getUserByToken.get()).thenReturn(admin);
        when(projectHelper.getProjectById(10L, admin)).thenReturn(project);

        ProjectEvmFinancialResponse summary = ProjectEvmFinancialResponse.builder()
                .projectId(10L)
                .projectName("Project Alpha")
                .build();
        List<PhaseEvmDetailResponse> phases = List.of();
        List<EmployeeEvmPerformanceResponse> employees = List.of();

        when(evmHelper.getProjectFinancialSummary(project, true)).thenReturn(summary);
        when(evmHelper.getProjectPhasesEvm(project)).thenReturn(phases);
        when(evmHelper.getProjectEmployeesEvm(project)).thenReturn(employees);

        byte[] fakeCsv = "\uFEFFProject Alpha".getBytes(StandardCharsets.UTF_8);
        when(evmHelper.generateEvmCsvReport(summary, phases, employees)).thenReturn(fakeCsv);

        byte[] csv = evmService.exportEvmCsvReport(10L);

        assertNotNull(csv);
        assertEquals(fakeCsv, csv);
        verify(evmHelper).generateEvmCsvReport(summary, phases, employees);
    }
}
