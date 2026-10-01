package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.AppError;
import com.example.project_pulse_backend.exception.AppException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class TimeHelper {
    public int workdays(LocalDateTime startAt, LocalDateTime endAt) {
        int workingDays = 0;
        LocalDate start = startAt.toLocalDate();
        LocalDate end = endAt.toLocalDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();

            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }

    public void checkStartBeforeEnd(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw AppException.builder().appError(AppError.INVALID_DATE_RANGE).build();
        }
    }

    public void checkWorkingDateInTask(
            LocalDateTime workingDate, LocalDateTime taskStartAt, LocalDateTime taskEndAt) {
        LocalDate workingDay = workingDate.toLocalDate();
        if ((taskStartAt != null && workingDay.isBefore(taskStartAt.toLocalDate()))
                || (taskEndAt != null && workingDay.isAfter(taskEndAt.toLocalDate()))) {
            throw AppException.builder()
                    .appError(AppError.TASK_NOTE_WORKING_DATE_OUT_OF_RANGE)
                    .build();
        }
    }

    public void checkTaskInPhase(
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

    public void checkPhaseInProject(LocalDateTime startAtPhase, LocalDateTime endAtPhase
            , LocalDateTime startAtProject, LocalDateTime endAtProject) {
        if (startAtPhase.isBefore(endAtProject) || startAtProject.isBefore(endAtPhase)) {
            throw AppException.builder().appError(AppError.PHASE_OUT_OF_PROJECT_RANGE).build();

        }
    }

    public void checkPhaseInMonth(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt.getMonth() != endAt.getMonth() || startAt.getYear() != endAt.getYear()) {
            throw AppException.builder().appError(AppError.PHASE_NOT_SAME_MONTH).build();
        }
    }
}
