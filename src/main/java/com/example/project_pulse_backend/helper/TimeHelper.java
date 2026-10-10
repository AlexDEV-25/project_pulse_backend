package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.exception.AppException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TimeHelper {

    public static int workdays(
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        int workingDays = 0;
        LocalDate start = startAt.toLocalDate();
        LocalDate end = endAt.toLocalDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();

            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays + safeSize(makeUpDates) - safeSize(offDates);
    }

    public static int elapsedWorkdays(
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime at,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        if (!at.isAfter(startAt)) {
            return 0;
        }

        LocalDateTime calculationTime = at.isAfter(endAt) ? endAt : at;
        LocalDate startDate = startAt.toLocalDate();
        LocalDate calculationDate = calculationTime.toLocalDate();
        int elapsedWorkdays = 0;

        for (LocalDate date = startDate; !date.isAfter(calculationDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                elapsedWorkdays++;
            }
        }

        elapsedWorkdays += completedSpecialDates(makeUpDates, startDate, calculationTime);
        elapsedWorkdays -= completedSpecialDates(offDates, startDate, calculationTime);
        return elapsedWorkdays;
    }

    private static int safeSize(List<LocalDate> dates) {
        return dates == null ? 0 : dates.size();
    }

    private static int completedSpecialDates(
            List<LocalDate> dates, LocalDate startDate, LocalDateTime calculationTime) {
        if (dates == null) {
            return 0;
        }
        LocalDate calculationDate = calculationTime.toLocalDate();
        return (int) dates.stream()
                .filter(date -> date != null
                        && !date.isBefore(startDate)
                        && !date.isAfter(calculationDate)
                        && calculationTime.isAfter(date.atStartOfDay()))
                .count();
    }

    public static void checkStartBeforeEnd(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw AppException.builder().appError(AppError.INVALID_DATE_RANGE).build();
        }
    }

    public static void checkWorkingDateInTask(
            LocalDateTime workingDate, LocalDateTime taskStartAt, LocalDateTime taskEndAt) {
        LocalDate workingDay = workingDate.toLocalDate();
        if ((taskStartAt != null && workingDay.isBefore(taskStartAt.toLocalDate()))
                || (taskEndAt != null && workingDay.isAfter(taskEndAt.toLocalDate()))) {
            throw AppException.builder()
                    .appError(AppError.TASK_NOTE_WORKING_DATE_OUT_OF_RANGE)
                    .build();
        }
    }

    public static void checkTaskInPhase(
            LocalDateTime taskStartAt,
            LocalDateTime taskEndAt,
            LocalDateTime phaseStartAt,
            LocalDateTime phaseEndAt) {
        if (taskStartAt == null || taskEndAt == null
                || taskStartAt.isBefore(phaseStartAt) || taskEndAt.isAfter(phaseEndAt)) {
            throw AppException.builder()
                    .appError(AppError.TASK_OUT_OF_PHASE_RANGE)
                    .build();
        }
    }

    public static void checkPhaseInProject(LocalDateTime startAtPhase, LocalDateTime endAtPhase
            , LocalDateTime startAtProject, LocalDateTime endAtProject) {
        if (startAtPhase.isBefore(startAtProject) || endAtPhase.isAfter(endAtProject)) {
            throw AppException.builder().appError(AppError.PHASE_OUT_OF_PROJECT_RANGE).build();

        }
    }
}
