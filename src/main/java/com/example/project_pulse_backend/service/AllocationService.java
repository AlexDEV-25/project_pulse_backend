package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAllocationRequest;
import com.example.project_pulse_backend.dto.request.UpdateAllocationRequest;
import com.example.project_pulse_backend.dto.response.AllocationResponse;
import com.example.project_pulse_backend.entity.Allocation;
import com.example.project_pulse_backend.entity.Phase;
import com.example.project_pulse_backend.entity.ProjectMember;
import com.example.project_pulse_backend.entity.User;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.AllocationHelper;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.helper.PhaseHelper;
import com.example.project_pulse_backend.helper.ProjectMemberHelper;
import com.example.project_pulse_backend.mapper.AllocationMapper;
import com.example.project_pulse_backend.repository.AllocationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllocationService {
    private final AllocationRepo allocationRepo;
    private final GetUserByToken getUserByToken;
    private final PhaseHelper phaseHelper;
    private final ProjectMemberHelper projectMemberHelper;
    private final AllocationHelper allocationHelper;
    private final AllocationMapper allocationMapper;

    @PreAuthorize("hasAuthority('CREATE_ALLOCATION')")
    @Transactional
    public AllocationResponse createAllocation(CreateAllocationRequest request) {
        User user = getUserByToken.get();
        Phase phase = phaseHelper.getPhaseById(request.getPhaseId(), user);
        allocationHelper.checkDone(phase);

        ProjectMember member = projectMemberHelper.getProjectMemberById(request.getMemberId(), user);
        Long userId = member.getUser().getId();

        if (allocationRepo.existsByPhase_IdAndMember_Id(phase.getId(), member.getId())) {
            throw AppException.builder().appError(AppError.ALLOCATION_ALREADY_EXISTS).build();
        }

        BigDecimal resourceRate = member.getUser().getResourceRate() != null
                ? member.getUser().getResourceRate() : BigDecimal.ZERO;

        Allocation allocation = allocationMapper.toAllocation(request);
        allocation.setPhase(phase);
        allocation.setMember(member);
        allocation.setResourceRateSnapshot(resourceRate);

        Allocation saved = allocationRepo.save(allocation);
        allocationHelper.recalculateForUser(userId);
        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('UPDATE_ALLOCATION')")
    @Transactional
    public AllocationResponse updateAllocation(Long id, UpdateAllocationRequest request) {
        User user = getUserByToken.get();

        Allocation allocation = allocationHelper.getAllocationById(id, user);

        Long userId = allocation.getMember().getUser().getId();
        Phase phase = allocation.getPhase();
        allocationHelper.checkDone(phase);

        allocationMapper.updateAllocation(request, allocation);

        Allocation saved = allocationRepo.save(allocation);
        allocationHelper.recalculateForUser(userId);
        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('DELETE_ALLOCATION')")
    @Transactional
    public void deleteAllocation(Long id) {
        User user = getUserByToken.get();

        Allocation allocation = allocationHelper.getAllocationById(id, user);
        allocationHelper.checkDone(allocation.getPhase());
        Long userId = allocation.getMember().getUser().getId();
        allocationRepo.delete(allocation);
        allocationHelper.recalculateForUser(userId);
    }

    @PreAuthorize("hasAuthority('GET_ALLOCATIONS_BY_PROJECT')")
    public List<AllocationResponse> getAllocationsByProjectId(Long projectId, Long phaseId, String keyword) {
        User user = getUserByToken.get();
        return allocationHelper.getAllocationByProject(projectId, phaseId, user, keyword).stream()
                .map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_ALLOCATIONS_BY_ID')")
    public AllocationResponse getAllocationById(Long id) {
        User user = getUserByToken.get();
        return toResponse(allocationHelper.getAllocationById(id, user));
    }

    private AllocationResponse toResponse(Allocation allocation) {
        AllocationResponse response = allocationMapper.toAllocationResponse(allocation);
        response.setAllocationPoint(allocationHelper.calculateAllocationPoint(allocation));
        return response;
    }

}