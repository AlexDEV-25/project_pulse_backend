package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.exception.AppException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class TimeHelper {

    public static int workdays(
            LocalDate startAt,
            LocalDate endAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        return workingDates(startAt, endAt, makeUpDates, offDates).size();
    }

    public static Set<LocalDate> workingDates(
            LocalDate startAt,
            LocalDate endAt,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        LocalDate start = startAt;
        LocalDate end = endAt;
        Set<LocalDate> workingDates = new TreeSet<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDates.add(date);
            }
        }

        if (makeUpDates != null) {
            makeUpDates.stream()
                    .filter(date -> date != null
                            && !date.isBefore(start)
                            && !date.isAfter(end)
                            && isWeekend(date))
                    .forEach(workingDates::add);
        }
        if (offDates != null) {
            offDates.stream()
                    .filter(date -> date != null && !isWeekend(date))
                    .forEach(workingDates::remove);
        }
        return workingDates;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public static int elapsedWorkdays(
            LocalDate startAt,
            LocalDate endAt,
            LocalDate at,
            List<LocalDate> makeUpDates,
            List<LocalDate> offDates) {
        if (at.isBefore(startAt)) {
            return 0;
        }

        LocalDate calculationDate = at.isAfter(endAt) ? endAt : at;
        return (int) workingDates(startAt, endAt, makeUpDates, offDates).stream()
                .filter(date -> !date.isAfter(calculationDate))
                .count();
    }

    public static void checkStartBeforeEnd(LocalDate startAt, LocalDate endAt) {
        if (startAt.isAfter(endAt)) {
            throw AppException.builder().appError(AppError.INVALID_DATE_RANGE).build();
        }
    }

    public static void checkWorkingDateInTask(
            LocalDate workingDate, LocalDate taskStartAt, LocalDate taskEndAt) {
        if ((taskStartAt != null && workingDate.isBefore(taskStartAt))
                || (taskEndAt != null && workingDate.isAfter(taskEndAt))) {
            throw AppException.builder()
                    .appError(AppError.TASK_NOTE_WORKING_DATE_OUT_OF_RANGE)
                    .build();
        }
    }

    public static void checkTaskInPhase(
            LocalDate taskStartAt,
            LocalDate taskEndAt,
            LocalDate phaseStartAt,
            LocalDate phaseEndAt) {
        if (taskStartAt == null || taskEndAt == null
                || taskStartAt.isBefore(phaseStartAt) || taskEndAt.isAfter(phaseEndAt)) {
            throw AppException.builder()
                    .appError(AppError.TASK_OUT_OF_PHASE_RANGE)
                    .build();
        }
    }

    public static void checkPhaseInProject(
            LocalDate startAtPhase, LocalDate endAtPhase, LocalDate startAtProject, LocalDate endAtProject) {
        if (startAtPhase.isBefore(startAtProject) || endAtPhase.isAfter(endAtProject)) {
            throw AppException.builder().appError(AppError.PHASE_OUT_OF_PROJECT_RANGE).build();

        }
    }
}
