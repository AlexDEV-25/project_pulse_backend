package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.constant.EarningTransactionType;
import com.example.project_pulse_backend.constant.PhaseStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.dto.response.*;
import com.example.project_pulse_backend.entity.*;
import com.example.project_pulse_backend.exception.AppException;
import com.example.project_pulse_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class EvmHelper {
    private static final int DIVISION_SCALE = 4;
    private static final int FINANCIAL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final String TEMPLATE_PATH = "templates/report_example.csv";

    private final PhaseEvmSnapshotRepo snapshotRepo;
    private final AllocationRepo allocationRepo;
    private final EarningTransactionRepo earningTransactionRepo;
    private final TaskCostTransactionRepo taskCostTransactionRepo;
    private final PhaseRepo phaseRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final TaskRepo taskRepo;
    private final AllocationHelper allocationHelper;
    private final PhaseHelper phaseHelper;

    @Transactional
    public void createSnapshot(Phase phase) {
        if (snapshotRepo.findByPhase_Id(phase.getId()).isPresent()) {
            throw AppException.builder().appError(AppError.PHASE_EVM_SNAPSHOT_ALREADY_EXISTS).build();
        }

        LocalDateTime capturedAt = LocalDateTime.now();
        BigDecimal bac = bacForPhase(phase);
        BigDecimal pv = pvForPhase(phase, capturedAt.toLocalDate());
        BigDecimal ev = evForPhase(phase.getId());
        BigDecimal ac = acForPhase(phase.getId());

        snapshotRepo.save(PhaseEvmSnapshot.builder()
                .phase(phase)
                .capturedAt(capturedAt)
                .bac(bac).pv(pv).ev(ev).ac(ac).build());
    }

    public BigDecimal cpi(BigDecimal ev, BigDecimal ac) {
        return divideOrNull(ev, ac);
    }

    public BigDecimal spi(BigDecimal ev, BigDecimal pv) {
        return divideOrNull(ev, pv);
    }

    public EvmHealthResponse getProjectHealth(Project project) {
        LocalDateTime capturedAt = LocalDateTime.now();

        BigDecimal bac = project.getProjectBudget();
        BigDecimal pv = pv(project.getId(), capturedAt.toLocalDate());
        BigDecimal ev = earningTransactionRepo.sumNetEarnedPointsByProjectId(
                project.getId(), EarningTransactionType.EARN);
        BigDecimal ac = taskCostTransactionRepo.sumCostPointsByProjectId(
                project.getId());

        return toEvmHealthResponse(project, capturedAt, bac, pv, ev, ac);
    }

    public List<PhaseEvmDetailResponse> getProjectPhasesEvm(Project project) {
        List<Phase> phases = phaseRepo.findByProject_IdOrderByStartAtAscIdAsc(project.getId());
        List<PhaseEvmDetailResponse> result = new ArrayList<>();

        BigDecimal cumulativePv = BigDecimal.ZERO;
        BigDecimal cumulativeEv = BigDecimal.ZERO;
        BigDecimal cumulativeAc = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();

        for (Phase phase : phases) {
            PhaseEvmSnapshot snapshot;
            if (phase.getStatus() == PhaseStatus.DONE) {
                snapshot = snapshotRepo.findByPhase_Id(phase.getId()).orElseThrow(
                        () -> AppException.builder().appError(AppError.PHASE_EVM_SNAPSHOT_NOT_FOUND).build()
                );
            } else {
                snapshot = new PhaseEvmSnapshot(
                        null,
                        phase,
                        now,
                        bacForPhase(phase),
                        pvForPhase(phase, now.toLocalDate()),
                        evForPhase(phase.getId()),
                        acForPhase(phase.getId()));
            }

            BigDecimal bac = snapshot.getBac();
            BigDecimal pv = snapshot.getPv() != null ? snapshot.getPv() : BigDecimal.ZERO;
            BigDecimal ev = snapshot.getEv() != null ? snapshot.getEv() : BigDecimal.ZERO;
            BigDecimal ac = snapshot.getAc() != null ? snapshot.getAc() : BigDecimal.ZERO;

            cumulativePv = cumulativePv.add(pv);
            cumulativeEv = cumulativeEv.add(ev);
            cumulativeAc = cumulativeAc.add(ac);

            result.add(PhaseEvmDetailResponse.builder()
                    .phaseId(phase.getId())
                    .phaseName(phase.getPhaseName())
                    .startAt(phase.getStartAt())
                    .endAt(phase.getEndAt())
                    .status(phase.getStatus())
                    .progress(phaseHelper.calculateProgress(phase.getId()))
                    .bac(bac)
                    .pv(pv)
                    .ev(ev)
                    .ac(ac)
                    .cv(cv(ev, ac))
                    .sv(sv(ev, pv))
                    .cpi(cpi(ev, ac))
                    .spi(spi(ev, pv))
                    .cumulativePv(cumulativePv)
                    .cumulativeEv(cumulativeEv)
                    .cumulativeAc(cumulativeAc)
                    .build());
        }

        return result;
    }

    public List<EmployeeEvmPerformanceResponse> getProjectEmployeesEvm(Project project) {
        List<ProjectMember> members = projectMemberRepo.findByProject_Id(project.getId());

        Map<Long, BigDecimal> evMap = new HashMap<>();
        List<Object[]> evRows = earningTransactionRepo.sumEarnedPointsGroupedByEmployee(project.getId(), EarningTransactionType.EARN);
        for (Object[] row : evRows) {
            if (row[0] != null && row[1] != null) {
                evMap.put(((Number) row[0]).longValue(), (BigDecimal) row[1]);
            }
        }

        Map<Long, Object[]> costMap = new HashMap<>();
        List<Object[]> costRows = taskCostTransactionRepo.sumCostAndWorkdaysGroupedByEmployee(project.getId());
        for (Object[] row : costRows) {
            if (row[0] != null) {
                costMap.put(((Number) row[0]).longValue(), row);
            }
        }

        Map<Long, Long> completedTaskMap = new HashMap<>();
        List<Object[]> taskRows = taskRepo.countCompletedTasksGroupedByEmployee(project.getId(), TaskStatus.DONE);
        for (Object[] row : taskRows) {
            if (row[0] != null && row[1] != null) {
                completedTaskMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
            }
        }

        List<EmployeeEvmPerformanceResponse> responses = new ArrayList<>();
        for (ProjectMember member : members) {
            User empUser = member.getUser();
            if (empUser == null) continue;

            Long userId = empUser.getId();
            BigDecimal totalEv = evMap.getOrDefault(userId, BigDecimal.ZERO);

            Object[] costData = costMap.get(userId);
            BigDecimal totalAc = costData != null && costData[1] != null ? (BigDecimal) costData[1] : BigDecimal.ZERO;
            BigDecimal totalWorkdays = costData != null && costData[2] != null ? (BigDecimal) costData[2] : BigDecimal.ZERO;

            Long completedCount = completedTaskMap.getOrDefault(userId, 0L);
            BigDecimal netValue = totalEv.subtract(totalAc);

            BigDecimal roi;
            if (totalAc.compareTo(BigDecimal.ZERO) > 0) {
                roi = totalEv.divide(totalAc, FINANCIAL_SCALE, ROUNDING_MODE);
            } else if (totalEv.compareTo(BigDecimal.ZERO) > 0) {
                roi = BigDecimal.valueOf(9.99);
            } else {
                roi = BigDecimal.ONE.setScale(FINANCIAL_SCALE, ROUNDING_MODE);
            }

            String category = determinePerformanceCategory(totalEv, totalAc, roi);

            responses.add(EmployeeEvmPerformanceResponse.builder()
                    .userId(userId)
                    .userName(empUser.getUserName())
                    .position(empUser.getPosition())
                    .avatarUrl(empUser.getAvatarUrl())
                    .resourceRate(empUser.getResourceRate())
                    .totalWorkdays(totalWorkdays)
                    .totalAc(totalAc)
                    .totalEv(totalEv)
                    .netValue(netValue)
                    .roi(roi)
                    .completedTasksCount(completedCount)
                    .performanceCategory(category)
                    .build());
        }

        responses.sort((a, b) -> b.getTotalEv().compareTo(a.getTotalEv()));
        return responses;
    }

    public ProjectEvmFinancialResponse getProjectFinancialSummary(Project project, boolean isAdmin) {
        BigDecimal bac = project.getProjectBudget();
        LocalDateTime capturedAt = LocalDateTime.now();
        BigDecimal pv = pv(project.getId(), capturedAt.toLocalDate());
        BigDecimal ev = earningTransactionRepo.sumNetEarnedPointsByProjectId(project.getId(), EarningTransactionType.EARN);
        BigDecimal ac = taskCostTransactionRepo.sumCostPointsByProjectId(project.getId());

        BigDecimal cpi = cpi(ev, ac);
        BigDecimal spi = spi(ev, pv);
        BigDecimal eac = eac(bac, cpi);
        BigDecimal etc = etc(eac, ac);
        BigDecimal vac = vac(bac, eac);
        BigDecimal tcpi = tcpi(bac, ev, ac);

        BigDecimal evToBacRatio = bac.compareTo(BigDecimal.ZERO) > 0 && ev != null
                ? ev.multiply(BigDecimal.valueOf(100)).divide(bac, FINANCIAL_SCALE, ROUNDING_MODE)
                : null;

        BigDecimal clientBudget = isAdmin ? project.getClientBudget() : null;

        BigDecimal evToClientBudgetRatio = null;
        BigDecimal currentMargin = null;
        BigDecimal forecastedMargin = null;
        BigDecimal marginPercentage = null;

        if (clientBudget != null && clientBudget.compareTo(BigDecimal.ZERO) > 0) {
            if (ev != null) {
                evToClientBudgetRatio = ev.multiply(BigDecimal.valueOf(100)).divide(clientBudget, FINANCIAL_SCALE, ROUNDING_MODE);
            }
            if (ac != null) {
                currentMargin = clientBudget.subtract(ac);
                marginPercentage = currentMargin.multiply(BigDecimal.valueOf(100)).divide(clientBudget, FINANCIAL_SCALE, ROUNDING_MODE);
            }
            if (eac != null) {
                forecastedMargin = clientBudget.subtract(eac);
            }
        }

        return ProjectEvmFinancialResponse.builder()
                .projectId(project.getId())
                .projectName(project.getProjectName())
                .capturedAt(capturedAt)
                .projectBudget(bac)
                .clientBudget(clientBudget)
                .pv(pv)
                .ev(ev)
                .ac(ac)
                .cv(cv(ev, ac))
                .sv(sv(ev, pv))
                .cpi(cpi)
                .spi(spi)
                .eac(eac)
                .etc(etc)
                .vac(vac)
                .tcpi(tcpi)
                .evToBacRatio(evToBacRatio)
                .evToClientBudgetRatio(evToClientBudgetRatio)
                .currentMargin(currentMargin)
                .forecastedMargin(forecastedMargin)
                .marginPercentage(marginPercentage)
                .build();
    }


    private BigDecimal cv(BigDecimal ev, BigDecimal ac) {
        return ev.subtract(ac);
    }

    private BigDecimal sv(BigDecimal ev, BigDecimal pv) {
        return ev.subtract(pv);
    }

    private BigDecimal eac(BigDecimal bac, BigDecimal cpi) {
        return cpi == null ? null : divideOrNull(bac, cpi);
    }

    private BigDecimal etc(BigDecimal eac, BigDecimal ac) {
        return eac == null ? null : eac.subtract(ac);
    }

    private BigDecimal vac(BigDecimal bac, BigDecimal eac) {
        return eac == null ? null : bac.subtract(eac);
    }

    private BigDecimal tcpi(BigDecimal bac, BigDecimal ev, BigDecimal ac) {
        return divideOrNull(bac.subtract(ev), bac.subtract(ac));
    }

    private BigDecimal plannedValueAt(
            BigDecimal allocationPoint,
            LocalDate phaseStartAt,
            LocalDate phaseEndAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates,
            LocalDate at) {
        Objects.requireNonNull(allocationPoint, "allocationPoint must not be null");
        Objects.requireNonNull(phaseStartAt, "phaseStartAt must not be null");
        Objects.requireNonNull(phaseEndAt, "phaseEndAt must not be null");
        if (at.isBefore(phaseStartAt)) {
            return BigDecimal.ZERO;
        }
        if (!at.isBefore(phaseEndAt)) {
            return allocationPoint;
        }

        int phaseWorkdays = TimeHelper.workingDates(
                phaseStartAt, phaseEndAt, makeUpDates, offDates).size();
        if (phaseWorkdays == 0) {
            return BigDecimal.ZERO;
        }
        int elapsedWorkdays = Math.clamp(
                TimeHelper.elapsedWorkdays(
                        phaseStartAt, phaseEndAt, at, makeUpDates, offDates), 0, phaseWorkdays);

        return allocationPoint.multiply(BigDecimal.valueOf(elapsedWorkdays))
                .divide(BigDecimal.valueOf(phaseWorkdays), DIVISION_SCALE, ROUNDING_MODE);
    }

    public BigDecimal pv(Long projectId, LocalDate at) {
        BigDecimal total = BigDecimal.ZERO;
        for (var allocation : allocationRepo.filterAllocation(projectId, null, null, null)) {
            Phase phase = allocation.getPhase();
            total = total.add(plannedValueAt(
                    allocationHelper.calculateAllocationPoint(allocation),
                    phase.getStartAt(),
                    phase.getEndAt(),
                    phase.getMakeUpDates(),
                    phase.getOffDates(),
                    at));
        }
        return total;
    }


    /**
     * Xuất báo cáo EVM ra CSV chuẩn.
     * Cấu trúc báo cáo (tiêu đề section, tên cột, nhãn dòng) được đọc từ file template
     * tại classpath:templates/report_example.csv.
     * Dữ liệu thực tế được lấy từ các DTO đầu vào và điền vào đúng vị trí.
     * UTF-8 BOM được thêm để Excel hiển thị đúng Tiếng Việt.
     */
    public byte[] generateEvmCsvReport(
            ProjectEvmFinancialResponse summary,
            List<PhaseEvmDetailResponse> phases,
            List<EmployeeEvmPerformanceResponse> employees) {

        EvmReportTemplate template = loadEvmCsvTemplate();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String exportTime = LocalDateTime.now().format(dtf);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM để Excel nhận diện đúng encoding

        // ===== PHẦN 1: TỔNG QUAN DỰ ÁN & SỨC KHỎE EVM =====
        sb.append(template.summaryTitle()).append("\n");
        List<String> summaryLabels = template.summaryLabels();
        List<Object> summaryValues = buildSummaryValues(summary, exportTime);
        for (int i = 0; i < summaryLabels.size(); i++) {
            String label = summaryLabels.get(i);
            String value = i < summaryValues.size() ? (summaryValues.get(i) != null ? summaryValues.get(i).toString() : "") : "";
            appendCsvRow(sb, label, value);
        }
        sb.append("\n");

        // ===== PHẦN 2: BREAKDOWN THEO TỪNG PHASE =====
        sb.append(template.phasesTitle()).append("\n");
        sb.append(template.phasesColumns()).append("\n");
        for (PhaseEvmDetailResponse phase : phases) {
            sb.append(phase.getPhaseId()).append(",")
                    .append(escapeCsv(phase.getPhaseName())).append(",")
                    .append(phase.getStartAt() != null ? phase.getStartAt() : "").append(",")
                    .append(phase.getEndAt() != null ? phase.getEndAt() : "").append(",")
                    .append(phase.getStatus() != null ? phase.getStatus().name() : "").append(",")
                    .append(phase.getProgress() != null ? phase.getProgress() : 0).append(",")
                    .append(formatNumber(phase.getBac())).append(",")
                    .append(formatNumber(phase.getPv())).append(",")
                    .append(formatNumber(phase.getEv())).append(",")
                    .append(formatNumber(phase.getAc())).append(",")
                    .append(formatNumber(phase.getCv())).append(",")
                    .append(formatNumber(phase.getSv())).append(",")
                    .append(formatNumber(phase.getCpi())).append(",")
                    .append(formatNumber(phase.getSpi())).append(",")
                    .append(formatNumber(phase.getCumulativePv())).append(",")
                    .append(formatNumber(phase.getCumulativeEv())).append(",")
                    .append(formatNumber(phase.getCumulativeAc())).append("\n");
        }
        sb.append("\n");

        // ===== PHẦN 3: HIỆU SUẤT NHÂN SỰ =====
        sb.append(template.employeesTitle()).append("\n");
        sb.append(template.employeesColumns()).append("\n");
        for (EmployeeEvmPerformanceResponse emp : employees) {
            sb.append(emp.getUserId()).append(",")
                    .append(escapeCsv(emp.getUserName())).append(",")
                    .append(escapeCsv(emp.getPosition())).append(",")
                    .append(formatNumber(emp.getResourceRate())).append(",")
                    .append(emp.getCompletedTasksCount() != null ? emp.getCompletedTasksCount() : 0).append(",")
                    .append(formatNumber(emp.getTotalWorkdays())).append(",")
                    .append(formatNumber(emp.getTotalAc())).append(",")
                    .append(formatNumber(emp.getTotalEv())).append(",")
                    .append(formatNumber(emp.getNetValue())).append(",")
                    .append(formatNumber(emp.getRoi())).append(",")
                    .append(escapeCsv(emp.getPerformanceCategory())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Đọc cấu trúc template từ classpath (tương thích JAR deploy).
     * Template dùng marker dòng để định nghĩa từng section:
     * - "## SECTION: SUMMARY"  → bắt đầu phần tổng quan
     * - "## SECTION: PHASES"   → bắt đầu phần phase
     * - "## SECTION: EMPLOYEES" → bắt đầu phần nhân sự
     * - "## COLUMNS: ..."       → dòng header cột CSV của section hiện tại
     * - Dòng bắt đầu bằng "===" → tiêu đề hiển thị của section
     * - Dòng khác trong SUMMARY  → nhãn (label) của từng chỉ số
     */
    private EvmReportTemplate loadEvmCsvTemplate() {
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_PATH);
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_PATH);
        }
        if (is == null) {
            throw new RuntimeException(
                    "Không tìm thấy template báo cáo EVM tại classpath: " + TEMPLATE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String summaryTitle = "";
            List<String> summaryLabels = new ArrayList<>();
            String phasesTitle = "";
            String phasesColumns = "";
            String employeesTitle = "";
            String employeesColumns = "";

            String currentSection = "";
            String line;

            while ((line = reader.readLine()) != null) {
                // Bỏ qua BOM ký tự đầu nếu có
                if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }

                if (line.startsWith("## SECTION:")) {
                    currentSection = line.substring("## SECTION:".length()).trim();
                    continue;
                }

                if (line.startsWith("## COLUMNS:")) {
                    String cols = line.substring("## COLUMNS:".length()).trim();
                    if ("PHASES".equals(currentSection)) {
                        phasesColumns = cols;
                    } else if ("EMPLOYEES".equals(currentSection)) {
                        employeesColumns = cols;
                    }
                    continue;
                }

                if (line.startsWith("===")) {
                    switch (currentSection) {
                        case "SUMMARY" -> summaryTitle = line;
                        case "PHASES" -> phasesTitle = line;
                        case "EMPLOYEES" -> employeesTitle = line;
                    }
                    continue;
                }

                // Dòng trống hoặc dòng label trong SUMMARY
                if ("SUMMARY".equals(currentSection) && !line.isBlank()) {
                    summaryLabels.add(line.trim());
                }
            }

            if (summaryTitle.isBlank() || summaryLabels.isEmpty()
                    || phasesTitle.isBlank() || phasesColumns.isBlank()
                    || employeesTitle.isBlank() || employeesColumns.isBlank()) {
                throw new RuntimeException(
                        "File template báo cáo EVM có cấu trúc không hợp lệ: " + TEMPLATE_PATH);
            }

            return new EvmReportTemplate(
                    summaryTitle, summaryLabels,
                    phasesTitle, phasesColumns,
                    employeesTitle, employeesColumns);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Lỗi khi đọc template báo cáo EVM: " + TEMPLATE_PATH, e);
        }
    }

    /**
     * Xây dựng danh sách giá trị cho phần tổng quan, theo thứ tự khớp với summaryLabels trong template.
     */
    private List<Object> buildSummaryValues(ProjectEvmFinancialResponse s, String exportTime) {
        return List.of(
                s.getProjectName() != null ? s.getProjectName() : "",
                exportTime,
                formatNumber(s.getProjectBudget()),
                s.getClientBudget() != null ? formatNumber(s.getClientBudget()) : "Không công khai",
                formatNumber(s.getPv()),
                formatNumber(s.getEv()),
                formatNumber(s.getAc()),
                formatNumber(s.getCv()),
                formatNumber(s.getSv()),
                formatNumber(s.getCpi()),
                formatNumber(s.getSpi()),
                formatNumber(s.getEac()),
                formatNumber(s.getEtc()),
                formatNumber(s.getVac()),
                formatNumber(s.getTcpi()),
                s.getEvToBacRatio() != null ? s.getEvToBacRatio() + "%" : "N/A",
                s.getEvToClientBudgetRatio() != null ? s.getEvToClientBudgetRatio() + "%" : "N/A",
                s.getCurrentMargin() != null ? formatNumber(s.getCurrentMargin()) : "N/A",
                s.getMarginPercentage() != null ? s.getMarginPercentage() + "%" : "N/A",
                s.getForecastedMargin() != null ? formatNumber(s.getForecastedMargin()) : "N/A"
        );
    }


    private void appendCsvRow(StringBuilder sb, String label, Object value) {
        sb.append(escapeCsv(label)).append(",").append(escapeCsv(value != null ? value.toString() : "")).append("\n");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatNumber(BigDecimal number) {
        if (number == null) return "0";
        return number.stripTrailingZeros().toPlainString();
    }

    private String determinePerformanceCategory(BigDecimal totalEv, BigDecimal totalAc, BigDecimal roi) {
        if (totalEv == null) totalEv = BigDecimal.ZERO;
        if (totalAc == null) totalAc = BigDecimal.ZERO;

        if (roi.compareTo(new BigDecimal("1.20")) >= 0 && totalEv.compareTo(new BigDecimal("50")) >= 0) {
            return "STAR_PERFORMER";
        }
        if (totalEv.compareTo(new BigDecimal("150")) >= 0 && totalAc.compareTo(new BigDecimal("100")) >= 0) {
            return "HEAVY_LIFTER";
        }
        if (totalAc.compareTo(new BigDecimal("50")) >= 0 && roi.compareTo(new BigDecimal("0.85")) < 0) {
            return "NEEDS_IMPROVEMENT";
        }
        return "BALANCED";
    }

    private EvmHealthResponse toEvmHealthResponse(
            Project project,
            LocalDateTime capturedAt,
            BigDecimal bac,
            BigDecimal pv,
            BigDecimal ev,
            BigDecimal ac) {

        BigDecimal cpi = cpi(ev, ac);
        BigDecimal eac = eac(bac, cpi);

        return EvmHealthResponse.builder()
                .projectId(project.getId())
                .projectName(project.getProjectName())
                .capturedAt(capturedAt)
                .bac(bac)
                .pv(pv)
                .ev(ev)
                .ac(ac)
                .cv(cv(ev, ac))
                .sv(sv(ev, pv))
                .cpi(cpi)
                .spi(spi(ev, pv))
                .eac(eac)
                .etc(etc(eac, ac))
                .vac(vac(bac, eac))
                .tcpi(tcpi(bac, ev, ac))
                .build();
    }

    private BigDecimal bacForPhase(Phase phase) {
        return allocationRepo.findByPhase_Id(phase.getId()).stream()
                .map(allocationHelper::calculateAllocationPoint)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal pvForPhase(Phase phase, LocalDate at) {
        return allocationRepo.findByPhase_Id(phase.getId()).stream()
                .map(allocation -> plannedValueAt(
                        allocationHelper.calculateAllocationPoint(allocation),
                        phase.getStartAt(),
                        phase.getEndAt(),
                        phase.getMakeUpDates(),
                        phase.getOffDates(),
                        at))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal acForPhase(Long phaseId) {
        return taskCostTransactionRepo.sumCostPointsByPhaseId(
                phaseId);
    }

    private BigDecimal evForPhase(Long phaseId) {
        return earningTransactionRepo.sumNetEarnedPointsByPhaseId(
                phaseId, EarningTransactionType.EARN);
    }

    private BigDecimal divideOrNull(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.signum() == 0) {
            return null;
        }
        if (numerator == null) {
            return null;
        }
        return numerator.divide(denominator, DIVISION_SCALE, ROUNDING_MODE);
    }
}
