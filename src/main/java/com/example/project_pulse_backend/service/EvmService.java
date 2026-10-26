package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.response.EmployeeEvmPerformanceResponse;
import com.example.project_pulse_backend.dto.response.EvmHealthResponse;
import com.example.project_pulse_backend.dto.response.PhaseEvmDetailResponse;
import com.example.project_pulse_backend.dto.response.ProjectEvmFinancialResponse;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.helper.EvmHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.ProjectHelper;
import com.example.project_pulse_backend.helper.RoleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvmService {

    private final GetUserByToken getUserByToken;
    private final EvmHelper evmHelper;
    private final ProjectHelper projectHelper;

    @PreAuthorize("hasAuthority('GET_PROJECT_EVM_HEALTH')")
    public EvmHealthResponse getProjectHealth(Long projectId) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);
        return evmHelper.getProjectHealth(project);
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_PHASES_EVM')")
    public List<PhaseEvmDetailResponse> getProjectPhasesEvm(Long projectId) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);
        return evmHelper.getProjectPhasesEvm(project);
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_EMPLOYEES_EVM')")
    public List<EmployeeEvmPerformanceResponse> getProjectEmployeesEvm(Long projectId) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);
        return evmHelper.getProjectEmployeesEvm(project);
    }

    @PreAuthorize("hasAuthority('GET_PROJECT_EVM_FINANCIAL_SUMMARY')")
    public ProjectEvmFinancialResponse getProjectFinancialSummary(Long projectId) {
        User user = getUserByToken.get();
        Project project = projectHelper.getProjectById(projectId, user);
        boolean isAdmin = RoleHelper.checkIsAdmin(user);
        return evmHelper.getProjectFinancialSummary(project, isAdmin);
    }

    @PreAuthorize("hasAuthority('EXPORT_PROJECT_EVM_CSV')")
    public byte[] exportEvmCsvReport(Long projectId) {
        ProjectEvmFinancialResponse summary = getProjectFinancialSummary(projectId);
        List<PhaseEvmDetailResponse> phases = getProjectPhasesEvm(projectId);
        List<EmployeeEvmPerformanceResponse> employees = getProjectEmployeesEvm(projectId);
        return evmHelper.generateEvmCsvReport(summary, phases, employees);
    }
}
