package com.example.project_pulse_backend.helper;

import com.example.project_pulse_backend.constant.ProjectStatus;
import com.example.project_pulse_backend.constant.ScheduleStatus;
import com.example.project_pulse_backend.constant.TaskStatus;
import com.example.project_pulse_backend.entity.Project;
import com.example.project_pulse_backend.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StatisticsHelper {

    public static ScheduleStatus scheduleStatus(BigDecimal spi) {
        if (spi == null) {
            return ScheduleStatus.NO_DATA;
        }
        int comparison = spi.compareTo(BigDecimal.ONE);
        if (comparison > 0) {
            return ScheduleStatus.FAST;
        }
        if (comparison < 0) {
            return ScheduleStatus.SLOW;
        }
        return ScheduleStatus.ON_SCHEDULE;
    }


    public static boolean isInProgressOrNearingDeadline(
            Task task, LocalDate now, LocalDate nearingDeadline) {
        if (task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
            return true;
        }
        return task.getTaskStatus() != TaskStatus.DONE
                && task.getTaskStatus() != TaskStatus.CANCELED
                && task.getEndAt() != null
                && !task.getEndAt().isBefore(now)
                && !task.getEndAt().isAfter(nearingDeadline);
    }

    public static boolean isNotFinished(Project project) {
        return project.getProjectStatus() != ProjectStatus.DONE
                && project.getProjectStatus() != ProjectStatus.CANCELLED;
    }
}
