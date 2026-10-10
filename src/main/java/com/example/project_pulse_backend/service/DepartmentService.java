package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateDepartmentRequest;
import com.example.project_pulse_backend.dto.request.UpdateDepartmentRequest;
import com.example.project_pulse_backend.dto.response.DepartmentResponse;
import com.example.project_pulse_backend.dto.response.DepartmentStatisticsResponse;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.UserHelper;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepo departmentRepo;
    private final UserHelper userHelper;

    @PreAuthorize("hasAuthority('CREATE_DEPARTMENT')")
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        Department newDepartment = departmentRepo.save(Department.builder().departmentName(request.getDepartmentName()).hidden(false).build());
        return toResponse(newDepartment);
    }

    @PreAuthorize("hasAuthority('UPDATE_DEPARTMENT')")
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department entity = departmentRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());

        entity.setDepartmentName(request.getDepartmentName());
        entity.setHidden(request.isHidden());

        Department result = departmentRepo.save(entity);
        return toResponse(result);
    }
    
    @PreAuthorize("hasAuthority('DELETE_DEPARTMENT')")
    public void deleteDepartment(Long id) {
        userHelper.checkDepartmentEmpty(id);
        departmentRepo.deleteById(id);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepo.findAll()
                .stream().map(this::toResponse).toList();
    }

    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build());
        return toResponse(department);
    }

    @PreAuthorize("hasAuthority('GET_STATISTICS_DEPARTMENTS')")
    public DepartmentStatisticsResponse getDepartmentStatistics() {
        return DepartmentStatisticsResponse.builder()
                .totalDepartments(departmentRepo.count())
                .activeDepartments(departmentRepo.countByHidden(false))
                .hiddenDepartments(departmentRepo.countByHidden(true))
                .build();
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .hidden(department.isHidden())
                .employeeCount(userHelper.getUserCountByDepartment(department.getId()))
                .build();
    }
}
