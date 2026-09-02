package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateDepartmentRequest;
import com.example.project_pulse_backend.dto.request.UpdateDepartmentRequest;
import com.example.project_pulse_backend.dto.response.DepartmentResponse;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepo departmentRepo;

    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        Department newDepartment = departmentRepo.save(Department.builder().departmentName(request.getDepartmentName()).hidden(false).build());
        return DepartmentResponse.builder().id(newDepartment.getId()).departmentName(newDepartment.getDepartmentName()).hidden(newDepartment.isHidden()).build();
    }

    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {

        Department entity = departmentRepo.findById(id).orElseThrow(
                () -> AppException.builder().appError(AppError.DEPARTMENT_NOT_FOUND).build()
        );

        entity.setDepartmentName(request.getDepartmentName());
        entity.setHidden(request.isHidden());

        Department result = departmentRepo.save(entity);
        return DepartmentResponse.builder().id(result.getId()).departmentName(result.getDepartmentName()).hidden(result.isHidden()).build();
    }

    public void deleteDepartment(Long id) {
        departmentRepo.deleteById(id);
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepo.findAll()
                .stream()
                .map(department ->
                        DepartmentResponse.builder().id(department.getId()).departmentName(department.getDepartmentName()).hidden(department.isHidden()).build())
                .toList();

    }
}
