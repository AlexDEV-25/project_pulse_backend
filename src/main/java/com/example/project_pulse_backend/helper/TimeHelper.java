package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.exception.AppException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class TimeHelper {
    public static WeekRange weekRange(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new WeekRange(weekStart, weekEnd);
    }

    public record WeekRange(LocalDate weekStart, LocalDate weekEnd) {
    }

    // tính ngày làm việc trong phase
    public static Set<LocalDate> workingDates(
            LocalDate startAt,
            LocalDate endAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        Set<LocalDate> workingDates = new TreeSet<>();
        if (startAt == null || endAt == null || startAt.isAfter(endAt)) {
            return workingDates;
        }

        for (LocalDate date = startAt; !date.isAfter(endAt); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDates.add(date);
            }
        }

        if (makeUpDates != null) {
            makeUpDates.stream()
                    .filter(date -> date != null
                            && !date.isBefore(startAt)
                            && !date.isAfter(endAt)
                            && isWeekend(date))
                    .forEach(workingDates::add);
        }
        if (offDates != null) {
            offDates.stream()
                    .filter(date -> date != null
                            && !date.isBefore(startAt)
                            && !date.isAfter(endAt)
                            && !isWeekend(date))
                    .forEach(workingDates::remove);
        }
        return workingDates;
    }

    // tính tiến độ ngày trong phase
    public static int elapsedWorkdays(
            LocalDate startAt, LocalDate endAt, LocalDate at, List<LocalDate> makeUpDates, List<LocalDate> offDates) {
        if (at == null || startAt == null || endAt == null || at.isBefore(startAt)) {
            return 0;
        }

        LocalDate calculationDate = at.isAfter(endAt) ? endAt : at;
        return (int) workingDates(startAt, endAt, makeUpDates, offDates).stream()
                .filter(date -> !date.isAfter(calculationDate))
                .count();
    }

    // kiểm tra ngày bắt đầu phải trước ngày kết thức
    public static void checkStartBeforeEnd(LocalDate startAt, LocalDate endAt) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw AppException.builder().appError(AppError.INVALID_DATE_RANGE).build();
        }
    }

    // kiểm tra ngày làm việc phải trong thời gian bắt đầu và kết thúc của task
    public static void checkWorkingDateInTask(
            LocalDate workingDate, LocalDate taskStartAt, LocalDate taskEndAt) {
        if (workingDate == null) {
            return;
        }
        if ((taskStartAt != null && workingDate.isBefore(taskStartAt))
                || (taskEndAt != null && workingDate.isAfter(taskEndAt))) {
            throw AppException.builder().appError(AppError.TASK_NOTE_WORKING_DATE_OUT_OF_RANGE).build();
        }
    }

    // kiểm tra thời gian bắt đầu và kết thúc task phải trong phase
    public static void checkTaskInPhase(
            LocalDate taskStartAt, LocalDate taskEndAt, LocalDate phaseStartAt, LocalDate phaseEndAt) {
        if (taskStartAt == null || taskEndAt == null
                || (phaseStartAt != null && taskStartAt.isBefore(phaseStartAt))
                || (phaseEndAt != null && taskEndAt.isAfter(phaseEndAt))) {
            throw AppException.builder()
                    .appError(AppError.TASK_OUT_OF_PHASE_RANGE)
                    .build();
        }
    }

    // kiểm tra thời gian bắt đầu và kết thúc phase phải trong project
    public static void checkPhaseInProject(
            LocalDate startAtPhase, LocalDate endAtPhase, LocalDate startAtProject, LocalDate endAtProject) {
        if (startAtPhase == null || endAtPhase == null) {
            return;
        }
        if ((startAtProject != null && startAtPhase.isBefore(startAtProject))
                || (endAtProject != null && endAtPhase.isAfter(endAtProject))) {
            throw AppException.builder().appError(AppError.PHASE_OUT_OF_PROJECT_RANGE).build();
        }
    }

    // kiểm tra có phải cuối tuần không
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
