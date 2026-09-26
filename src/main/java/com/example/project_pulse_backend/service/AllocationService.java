package com.example.project_pulse_backend.service;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.dto.request.CreateAllocationRequest;
import com.example.project_pulse_backend.dto.request.UpdateAllocationRequest;
import com.example.project_pulse_backend.dto.response.AllocationResponse;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.helper.GetUserByToken;
import com.example.project_pulse_backend.repository.AllocationRepo;
import com.example.project_pulse_backend.repository.PhaseRepo;
import com.example.project_pulse_backend.repository.ProjectMemberRepo;
import com.example.project_pulse_backend.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepo allocationRepo;
    private final ProjectRepo projectRepo;
    private final PhaseRepo phaseRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final GetUserByToken getUserByToken;

    @PreAuthorize("hasAuthority('CREATE_ALLOCATION')")
    @Transactional
    public AllocationResponse createAllocation(CreateAllocationRequest request) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        Project project = projectRepo.findByIdAndPm_Id(request.getProjectId(), pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PROJECT_NOT_FOUND).build()
        );
        Phase phase = phaseRepo.findByIdAndProject_Id(request.getPhaseId(), project.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.PHASE_NOT_FOUND).build()
        );
        ProjectMember member = projectMemberRepo.findByIdAndProject_Id(request.getMemberId(), project.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.MEMBER_NOT_FOUND).build()
        );

        Long userId = member.getUser().getId();

        // 4. LOCK toàn bộ ProjectMember có user_id trùng với user_id đang chọn để tránh race condition giữa 2 PM
        projectMemberRepo.findAllByUserIdWithLock(userId);

        if (allocationRepo.existsByPhase_IdAndMember_Id(phase.getId(), member.getId())) {
            throw AppException.builder().appError(AppError.ALLOCATION_ALREADY_EXISTS).build();
        }

        // 6. Rule: Tổng phân bổ trong cùng 1 khoảng thời gian không vượt quá 100%
        validateTotalAllocationPercentage(userId, phase.getStartAt(), phase.getEndAt(), request.getAllocationPercentage(), null);

        // 7. Tính resourceRateSnapshot & allocationPoint
        BigDecimal resourceRate = member.getUser().getResourceRate() != null
                ? member.getUser().getResourceRate()
                : BigDecimal.ZERO;

        BigDecimal allocationPoint = calculateAllocationPoint(
                phase.getWorkdays(),
                resourceRate,
                request.getAllocationPercentage()
        );

        Allocation allocation = Allocation.builder()
                .project(project)
                .phase(phase)
                .member(member)
                .allocationPercentage(request.getAllocationPercentage())
                .resourceRateSnapshot(resourceRate)
                .allocationPoint(allocationPoint)
                .build();

        return toResponse(allocationRepo.save(allocation));
    }

    @PreAuthorize("hasAuthority('UPDATE_ALLOCATION')")
    @Transactional
    public AllocationResponse updateAllocation(Long id, UpdateAllocationRequest request) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        // 1. Kiểm tra quyền PM sở hữu allocation này
        Allocation allocation = allocationRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.ALLOCATION_NOT_FOUND).build()
        );

        Long userId = allocation.getMember().getUser().getId();
        Phase phase = allocation.getPhase();

        // 2. KHÓA NGAY: Lock toàn bộ ProjectMember của nhân sự này (SELECT ... FOR UPDATE)
        // Nếu có 1 PM khác đang đồng thời tạo mới hoặc sửa bất kỳ allocation nào của nhân sự này,
        // request đó (hoặc request này) sẽ bị block đợi transaction đầu tiên commit xong.
        projectMemberRepo.findAllByUserIdWithLock(userId);

        // 3. Rule: Kiểm tra tổng tải phân bổ sau khi đã có lock (đảm bảo đọc được trạng thái mới nhất)
        validateTotalAllocationPercentage(
                userId,
                phase.getStartAt(),
                phase.getEndAt(),
                request.getAllocationPercentage(),
                allocation.getId() // Loại trừ chính bản ghi này ra khỏi tổng cũ
        );

        // 4. Cập nhật tỷ lệ phân bổ mới
        allocation.setAllocationPercentage(request.getAllocationPercentage());

        // 5. Cập nhật lại điểm phân bổ (Point)
        BigDecimal allocationPoint = calculateAllocationPoint(
                phase.getWorkdays(),
                allocation.getResourceRateSnapshot(),
                request.getAllocationPercentage()
        );
        allocation.setAllocationPoint(allocationPoint);

        return toResponse(allocationRepo.save(allocation));
    }

    @PreAuthorize("hasAuthority('DELETE_ALLOCATION')")
    @Transactional
    public void deleteAllocation(Long id) {
        User pm = getUserByToken.get();
        checkIsPm(pm);

        Allocation allocation = allocationRepo.findByIdAndProject_Pm_Id(id, pm.getId()).orElseThrow(
                () -> AppException.builder().appError(AppError.ALLOCATION_NOT_FOUND).build()
        );

        allocationRepo.delete(allocation);
    }


    private void validateTotalAllocationPercentage(Long userId, LocalDateTime startAt, LocalDateTime endAt,
                                                   Integer newPercentage, Long currentAllocationId) {
        List<Allocation> overlappingAllocations = allocationRepo.findOverlappingAllocationsByUserId(userId, startAt, endAt);

        int currentTotal = overlappingAllocations.stream()
                .filter(a -> !a.getId().equals(currentAllocationId))
                .mapToInt(Allocation::getAllocationPercentage)
                .sum();

        if (currentTotal + newPercentage > 100) {
            throw AppException.builder().appError(AppError.TOTAL_ALLOCATION_EXCEEDS_100).build();
        }
    }

    private BigDecimal calculateAllocationPoint(Integer workdays, BigDecimal resourceRate, Integer percentage) {
        if (workdays == null || resourceRate == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(workdays)
                .multiply(resourceRate)
                .multiply(BigDecimal.valueOf(percentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private AllocationResponse toResponse(Allocation allocation) {
        return AllocationResponse.builder()
                .id(allocation.getId())
                .projectId(allocation.getProject() != null ? allocation.getProject().getId() : null)
                .projectName(allocation.getProject() != null ? allocation.getProject().getProjectName() : null)
                .phaseId(allocation.getPhase() != null ? allocation.getPhase().getId() : null)
                .phaseName(allocation.getPhase() != null ? allocation.getPhase().getPhaseName() : null)
                .memberId(allocation.getMember() != null ? allocation.getMember().getId() : null)
                .userId(allocation.getMember() != null && allocation.getMember().getUser() != null
                        ? allocation.getMember().getUser().getId() : null)
                .userName(allocation.getMember() != null && allocation.getMember().getUser() != null
                        ? allocation.getMember().getUser().getUserName() : null)
                .allocationPercentage(allocation.getAllocationPercentage())
                .resourceRateSnapshot(allocation.getResourceRateSnapshot())
                .allocationPoint(allocation.getAllocationPoint())
                .build();
    }

    private void checkIsPm(User user) {
        if (user.getRoles().stream().noneMatch(role -> role.getName().equals("PM"))) {
            throw AppException.builder().appError(AppError.USER_NOT_PM).build();
        }
    }
}