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
import com.example.project_pulse_backend.repository.AllocationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final AllocationRepo allocationRepo;
    private final GetUserByToken getUserByToken;
    private final PhaseHelper phaseHelper;
    private final ProjectMemberHelper projectMemberHelper;
    private final AllocationHelper allocationHelper;

    @PreAuthorize("hasAuthority('CREATE_ALLOCATION')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationResponse createAllocation(CreateAllocationRequest request) {
        User user = getUserByToken.get();

        Phase phase = phaseHelper.getPhaseByIdForUpdate(request.getPhaseId(), user);
        allocationHelper.checkDone(phase);

        ProjectMember member = projectMemberHelper.getProjectMemberById(request.getMemberId(), user);

        Long userId = member.getUser().getId();

        // LOCK toàn bộ ProjectMember có user_id trùng với user_id đang chọn để tránh race condition giữa 2 PM
        projectMemberHelper.lockAllProjectMembersByUserId(userId);

        if (allocationRepo.existsByPhase_IdAndMember_Id(phase.getId(), member.getId())) {
            throw AppException.builder().appError(AppError.ALLOCATION_ALREADY_EXISTS).build();
        }

        // Preserve the user's rate at the time the allocation is created.
        BigDecimal resourceRate = member.getUser().getResourceRate() != null
                ? member.getUser().getResourceRate()
                : BigDecimal.ZERO;

        Allocation allocation = Allocation.builder()
                .phase(phase)
                .member(member)
                .allocationPercentage(request.getAllocationPercentage())
                .resourceRateSnapshot(resourceRate)
                .build();

        Allocation saved = allocationRepo.save(allocation);
        allocationHelper.recalculateForUser(userId);
        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('UPDATE_ALLOCATION')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationResponse updateAllocation(Long id, UpdateAllocationRequest request) {
        User user = getUserByToken.get();

        Allocation allocation = allocationHelper.getAllocationById(id, user);

        Long userId = allocation.getMember().getUser().getId();
        Phase phase = allocation.getPhase();
        allocationHelper.checkDone(phase);

        // KHÓA NGAY: Lock toàn bộ ProjectMember của nhân sự này để tránh race condition giữa 2 PM
        projectMemberHelper.lockAllProjectMembersByUserId(userId);

        // 4. Cập nhật tỷ lệ phân bổ mới
        allocation.setAllocationPercentage(request.getAllocationPercentage());

        Allocation saved = allocationRepo.save(allocation);
        allocationHelper.recalculateForUser(userId);
        return toResponse(saved);
    }

    @PreAuthorize("hasAuthority('DELETE_ALLOCATION')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteAllocation(Long id) {
        User user = getUserByToken.get();

        Allocation allocation = allocationHelper.getAllocationById(id, user);
        allocationHelper.checkDone(allocation.getPhase());
        Long userId = allocation.getMember().getUser().getId();
        projectMemberHelper.lockAllProjectMembersByUserId(userId);
        allocationRepo.delete(allocation);
        allocationHelper.recalculateForUser(userId);
    }

    @PreAuthorize("hasAuthority('GET_ALLOCATIONS_BY_PROJECT')")
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByProjectId(Long projectId, Long phaseId, String keyword) {
        User user = getUserByToken.get();
        return allocationHelper.getAllocationByProject(projectId, phaseId, user, keyword).stream()
                .map(this::toResponse).toList();
    }

    @PreAuthorize("hasAuthority('GET_ALLOCATIONS_BY_ID')")
    @Transactional(readOnly = true)
    public AllocationResponse getAllocationById(Long id) {
        User user = getUserByToken.get();
        return toResponse(allocationHelper.getAllocationById(id, user));
    }

    private AllocationResponse toResponse(Allocation allocation) {
        return AllocationResponse.builder()
                .id(allocation.getId())
                .projectId(allocation.getPhase() != null ? allocation.getPhase().getProject().getId() : null)
                .projectName(allocation.getPhase() != null ? allocation.getPhase().getProject().getProjectName()
                        : null)
                .phaseId(allocation.getPhase() != null ? allocation.getPhase().getId() : null)
                .phaseName(allocation.getPhase() != null ? allocation.getPhase().getPhaseName() : null)
                .memberId(allocation.getMember() != null ? allocation.getMember().getId() : null)
                .userId(allocation.getMember() != null && allocation.getMember().getUser() != null
                        ? allocation.getMember().getUser().getId() : null)
                .userName(allocation.getMember() != null && allocation.getMember().getUser() != null
                        ? allocation.getMember().getUser().getUserName() : null)
                .allocationPercentage(allocation.getAllocationPercentage())
                .resourceRateSnapshot(allocation.getResourceRateSnapshot())
                .allocationPoint(allocationHelper.calculateAllocationPoint(allocation))
                .overloadedDates(allocation.getOverloadedDates() == null
                        ? List.of()
                        : List.copyOf(allocation.getOverloadedDates()))
                .build();
    }

}