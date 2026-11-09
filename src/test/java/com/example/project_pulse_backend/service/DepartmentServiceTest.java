package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.dto.request.*;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.Department;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.UserHelper;
import com.example.project_pulse_backend.mapper.DepartmentMapper;
import com.example.project_pulse_backend.repository.DepartmentRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTest {
    private final DepartmentRepo repository = mock(DepartmentRepo.class);
    private final UserHelper userHelper = mock(UserHelper.class);
    private final DepartmentMapper mapper = mock(DepartmentMapper.class);
    private final DepartmentService service = new DepartmentService(repository, userHelper, mapper);

    @Test
    void createUpdateDeleteAndReadMethodsDelegateAndMap() {
        Department department = Department.builder().id(2L).departmentName("Engineering").hidden(false).build();
        DepartmentResponse mapped = DepartmentResponse.builder().departmentName("Engineering").build();
        when(mapper.toDepartment(any())).thenReturn(department);
        when(repository.save(department)).thenReturn(department);
        when(mapper.toDepartmentResponse(department, 4L)).thenReturn(mapped);
        when(userHelper.getUserCountByDepartment(2L)).thenReturn(4L);
        when(repository.findById(2L)).thenReturn(Optional.of(department));
        when(repository.findAll()).thenReturn(List.of(department));

        assertEquals(mapped, service.createDepartment(CreateDepartmentRequest.builder()
                .departmentName("Engineering").build()));
        assertEquals(mapped, service.updateDepartment(2L, UpdateDepartmentRequest.builder()
                .departmentName("Engineering").hidden(false).build()));
        assertEquals(List.of(mapped), service.getAllDepartments());
        assertEquals(mapped, service.getDepartmentById(2L));
        service.deleteDepartment(2L);
        verify(userHelper).checkDepartmentEmpty(2L);
        verify(repository).deleteById(2L);
    }

    @Test
    void missingDepartmentAndStatisticsAreHandled() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> service.getDepartmentById(99L));
        assertThrows(AppException.class, () -> service.updateDepartment(99L, new UpdateDepartmentRequest()));

        when(repository.count()).thenReturn(5L);
        when(repository.countByHidden(false)).thenReturn(3L);
        when(repository.countByHidden(true)).thenReturn(2L);
        DepartmentStatisticsResponse stats = service.getDepartmentStatistics();
        assertEquals(5L, stats.getTotalDepartments());
        assertEquals(3L, stats.getActiveDepartments());
        assertEquals(2L, stats.getHiddenDepartments());
    }
}
